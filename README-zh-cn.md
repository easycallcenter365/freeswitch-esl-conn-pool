## 基于Java实现FreeSWITCH Event Socket连接池 

> author:  easycallcenter365@126.com

### 背景及概述

Event Socket是 FreeSWITCH 的提供一种通信协议，它提供了一种用于实时事件通知和与 FreeSWITCH 进行交互的机制。Event Socket有两种模式，Inbound 和 Outbound，两种模式的区别如下：

```txt
Inbound 模式:    FreeSWITCH 是服务端  <=>  外部应用程序是客户端
Outbound 模式:   FreeSWITCH 是客户端  <=>  外部应用程序是服务端
```

笔者在长期的工作摸索中，认为 Inbound 模式更易用，编程难度相对低些。由于 Inbound 模式下，就可以解决全部的应用场景，因此本文的讲解基于 Inbound 模式。

Event Socket协议是基于 TCP 的应用层文本协议，可以使用纯文本传输，也可以使用 XML 格式。任何支持 TCP Socket 的客户端都可以和 FreeSWITCH 进行交互。通过 Event Socket，外部应用程序可以监听 FreeSWITCH 内部的事件，如呼叫建立、挂断、DTMF 输入等，外部程序可以发送命令和控制电话交换的行为，进而可以用于创建自定义的电话应用、呼叫中心解决方案、电话会议系统等。

虽然 FreeSWITCH 官方也提供了各种语言版本的 Event Socket Library(简称ESL)，但是经过笔者的测试发现这些现有的项目并不易用或者有各种性能问题。 今天介绍的这个项目是基于Java语言的Netty框架实现 Event Socket 协议客户端。

### 为何需要连接池

当然是出于高性能的考虑，所以才需要连接池。大家回忆下，在十几年前的Java项目中，我们直接使用 JDBC Connector直接连接MySQL数据库的情景：

```txt
请求来了 -> 建立数据库连接 -> 发送查询或修改请求 -> 获取响应 -> 关闭连接
```

数据库连接的建立和关闭是比较耗时的操作，尤其是在高并发的情况下。使用连接池可以将数据库连接提前创建好并保持在池中，而不是每次需要连接数据库时都重新创建一个连接。这样可以减少连接的创建和关闭次数，从而提高性能。
这些年技术飞速发展，Java数据库的连接池项目也不断推陈出新，从早期的 C3P0 到 Druid，又到今天的 HikariCP，大家不断造轮子的动力，很大程度上来源于对性能的不断追求。
这里举例 Java MySQL 连接池的发展历史，是为了便于理解和对比。我们在 Java 客户端和 FreeSWITCH 的交互中同样也面临相同的问题。另外选择使用 Netty 是因为它是 Java 实现高性能的基础底座。

### 实现连接池的难点

实现 FreeSWITCH 连接池的难点在于， FreeSWITCH 的 Event Socket 通信中，各种事件都是异步产生的，比如通话开始后，我们需要通过 Event Socket 协议接收这些事件消息： 通话应答事件、dtmf按键、语音识别结果、通话挂断事件。此时我们需要一个保持长链接，以便实时接收各种事件消息，同时还需要使用这个 Socket 连接通道来发送各种指令。这样一来，好像一个通话就需要建立一个长链接？ 如果有100个通话就要建立100个连接？那1000个通话呢。显然这样不行，那我们分析下。

如果通话和 Event Socket 连接按照1:1的话，这时候大部分时间内这个 Socket 连接都是空闲，连接没有充分利用，浪费了资源。我们看到在一个通话中可能有以下需求和特点：

1. 通话需要有一个唯一标识uuid，它是我们自己的客户端程序生成的，我们通过这个uuid来控制整个通话;
2. 通话中，使用 Event Socket 协议，有些方法调用是立即返回结果的，比如execute app，有些是阻塞的、等待时间较长的，比如api originate等， 要实现 Socket 连接的复用，必须要尽可能的降低 Socket 的占用时长，尽可能的在几毫秒或者几十毫秒内完成;
3. 通话中，我们可以使用异步方法bgapi，然后通过订阅 backgroundId 去接收后续消息，这样可以极大降低对 Socket 连接的占用时间;
4. 通过上述2和3的办法，我们已经极大降低了 Socket 连接的占用时长，基本可以做到在指令发送后的几毫秒内接收到 FreeSWITCH 的指令响应 。

接下来的问题是如何异步接收各种事件。在上述第1条分析的时候，我们注意到每个通话都有一个唯一的uuid标识，我们可以通过使用一个默认连接去专门负责接收系统全部通话的异步事件消息，这个默认连接对象只负责接收消息，不用于传送 FreeSWITCH 指令。这个连接在收到各种异步事件后，然后根据uuid去分发消息，分发给uuid对应的消费者。我们在原始通话建立之前，创建一个回调函数对象，然后把uuid和该通话的回调函数绑定，注册到该默认连接对象上。这样问题就可以完美的解决了！ 这样连接池的内部结构包含2部分，默认连接对象和连接池。他们的作用简述如下：

```txt
默认连接对象： 订阅全部通话的异步事件消息，然后根据uuid分发给不同的通话，也就是调用各个通话建立时候注册的回调函数。默认连接对象只有一个。
连接池： 一个全局连接池，内部存储了10个连接对象。每当有通话线程需要向 FreeSWITCH 发送指令的时候，就从连接池借用一个连接对象，发送完指令并获取到响应后，立即把连接归还到连接池。连接池中的连接对象，不订阅任何消息，仅用作发送指令。
```

### 使用方法

定义一个listenter:

```java
  private static class CallListener implements IEslEventListener {
        private static final Logger logger = LoggerFactory.getLogger(CallListener.class);
        private String uuidCaller;
        private String uuidCallee;
        private String backgroundJobUuid = "";
        private volatile  boolean callerAnswered;

        public void setBackgroundJobUuid(String backgroundJobUuid) {
            this.backgroundJobUuid = backgroundJobUuid;
        }

        public CallListener(String innerId, String outerId) {
            this.uuidCaller = innerId;
            this.uuidCallee = outerId;
        }

        @Override
        public void eventReceived(String addr, EslEvent event) {
            // call back function executes in threadPool, avoiding blocking FreeSWITCH esl worker thread.
            // 耗时操作，放入新的线程池;
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Map<String, String> headers = event.getEventHeaders();
                    String uniqueId = headers.get("Unique-ID");
                    String eventName = headers.get("Event-Name");
                    if (EventNames.CHANNEL_ANSWER.equalsIgnoreCase(eventName)) {
                        logger.info("recv answer event, uuid={}...", uniqueId);
                        if(uniqueId.equals(uuidCaller)) {
                            callerAnswered = true;
                        }
                        if(uniqueId.equals(uuidCallee)) {
                          EslMessage response =  EslConnectionUtil.sendSyncApiCommand("uuid_bridge", uuidCaller + " " + uuidCallee);
                          logger.info("call bridge executed: {}", JSON.toJSONString(response));
                        }
                    } else if (EventNames.CHANNEL_HANGUP.equalsIgnoreCase(eventName)) {
                        logger.info("recv hangup event, uuid={}...", uniqueId);
                    } else if (EventNames.CHANNEL_PROGRESS_MEDIA.equalsIgnoreCase(eventName)) {
                        logger.info("recv ringing event...");
                    }
                }
            });
        }

        @Override
        public void backgroundJobResultReceived(String addr, EslEvent event) {
            EslConnectionUtil.getDefaultEslConnectionPool().getDefaultEslConn().removeListener(this.backgroundJobUuid);
            String response = event.toString();
            if(response.contains("-ERR")) {
                logger.warn("error occurs: {}", response);
            }

        }
    }
```	







