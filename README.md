## FreeSWITCH event socket connection pool

> author:  easycallcenter365@126.com

### About Event Socket 

 Event Socket is a communication protocol provided by FreeSWITCH that offers a mechanism
 for real-time event notifications and interaction with FreeSWITCH. 
 Event Socket operates in two modes, Inbound and Outbound, distinguished as follows:

```txt
 Inbound Mode:    FreeSWITCH is the server  <=>  External application is the client
 Outbound Mode:   FreeSWITCH is the client  <=>  External application is the server
``` 
 
 Through extensive work experience, the author believes that Inbound mode is more user-friendly
 and relatively easier to program. Since all application scenarios can be addressed using Inbound mode,
 this document focuses on explaining the Inbound mode.

 The Event Socket protocol is a TCP-based application layer text protocol that can use plain text
 or XML format. Any client supporting TCP Socket can interact with FreeSWITCH. With Event Socket,
 external applications can listen to internal events within FreeSWITCH, such as call establishment,
 hang-up, DTMF input, etc. External programs can send commands and control the behavior of the
 FreeSWITCH, which can be used to create custom telephony applications, call center solutions,
 conferencing systems, etc.

 Although FreeSWITCH officially provides Event Socket Libraries (ESL) in various languages,
 the author has found these existing projects to be either not user-friendly or having various
 performance issues. The project introduced here implements an Event Socket protocol client
 based on the Java Netty framework.

### Why a Connection Pool is Needed ？

 A connection pool is necessary for high-performance considerations. Let's recall the scenario
 in Java projects over a decade ago where we directly used the JDBC Connector to connect to MySQL databases:

```txt
 Request comes -> Establish database connection -> Send query or modification request -> Get response -> Close connection
```

 Establishing and closing database connections are time-consuming operations, especially under high concurrency.
 Using a connection pool allows database connections to be created in advance and maintained in the pool,
 rather than creating a new connection each time the database needs to be accessed.
 This reduces the number of connection creations and closures, thereby improving performance.
 Over the years, technology has rapidly evolved, and Java database connection pool projects have continually emerged,
 from early C3P0 to Druid, and today’s HikariCP. The motivation behind constantly reinventing the wheel largely stems
 from the continuous pursuit of performance.

 The development history of the Java MySQL connection pool is provided to facilitate understanding and comparison.
 We face similar issues in the interaction between the Java client and FreeSWITCH. Additionally,
 Netty is chosen for its foundation in implementing high-performance java applications.

### Challenges in Implementing a Connection Pool
 
 The challenge in implementing a FreeSWITCH connection pool lies in the fact that various events in FreeSWITCH’s Event Socket
 communication are asynchronously generated. For example, after a call starts, we need to receive these event messages
 through the Event Socket protocol: call answer events, DTMF key-presses, speech recognition results, call hang-up events.
 
 At this point, we need to maintain a long connection to receive various event messages in real-time, while also using
 this Socket connection to send various commands. This seemingly means that each call requires a long connection.
 What if there are 100 calls? Or 1000 calls? Clearly, this is not feasible. Let’s analyze further.

 If there is a 1:1 correspondence between calls and Event Socket connections, most of the time these socket connections are idle,
 leading to resource wastage. 


### The thoughts of  implement an esl connection pool

 In a call, we may have the following requirements and characteristics:
 
 1. Each call needs a unique identifier, uuid, which is generated by our client program to control the entire call;
 2. During a call, using the Event Socket protocol, some method calls return results immediately, such as execute app,
 while others are blocking and take a long time, such as api originate. To achieve Socket connection reuse,
 it is necessary to minimize Socket occupancy time, ideally completing within a few milliseconds or tens of milliseconds.
 
 3. During a call, we can use the asynchronous method bgapi and subscribe to the backgroundId to receive subsequent messages,
 significantly reducing Socket connection occupancy time.

 4. By using above methods, we have greatly reduced the Socket connection occupancy time, making it possible to
 receive FreeSWITCH command responses within milliseconds after sending the command.
 
 The next issue is how to asynchronously receive various events. In the analysis of point 1, we noticed that
 each call has a unique uuid identifier. We can use a default connection to specifically handle receiving all
 asynchronous event messages for all calls. 
 
 This default connection object only handles receiving messages and
 does not send FreeSWITCH commands. Upon receiving various asynchronous events, it distributes messages
 based on uuid to the corresponding consumers. Before establishing the original call, we create a callback function object,
 then bind the uuid and the callback function for the call, and register it with the default connection object.
 
 This solution perfectly addresses the issue! Thus, the internal structure of the connection pool includes two parts:
 the default connection object and the connection pool. Their roles are briefly described as follows:

* Default connection object: Subscribes to asynchronous event messages for all calls and distributes them based on
   uuid to different calls, i.e., calls the callback functions registered at the time of each call’s establishment.
   There is only one default connection object.

* Connection pool: A global connection pool internally storing 10 connection objects. Whenever a call thread needs to
 send a command to FreeSWITCH, it borrows a connection object from the pool. After sending the command and receiving the response,
 the connection is immediately returned to the pool. The connection objects in the pool do not subscribe to any messages and
 are only used for sending commands.
 
### Acknowledgments

Thanks for this open source project https://github.com/zhouhailin/freeswitch-externals . Our project based on it to do the Event Socket connection pool implementation. 
 
### Usage

First initialize the event socket connection pool:

```java
   List<String> eventSubscriptions = new ArrayList<>();
        eventSubscriptions.add(EventNames.CHANNEL_HANGUP);
        eventSubscriptions.add(EventNames.CHANNEL_ANSWER);
        eventSubscriptions.add(EventNames.CHANNEL_PROGRESS_MEDIA);
        eventSubscriptions.add(EventNames.HEARTBEAT);
        eventSubscriptions.add(EventNames.BACKGROUND_JOB);
        eventSubscriptions.add(EventNames.DETECTED_SPEECH);
        eventSubscriptions.add(EventNames.CHANNEL_PARK);
        eventSubscriptions.add(EventNames.RECORD_START);
        eventSubscriptions.add(EventNames.RECORD_STOP);
        eventSubscriptions.add(EventNames.PLAYBACK_STOP);
        eventSubscriptions.add(EventNames.PLAYBACK_START);
        eventSubscriptions.add(EventNames.DTMF);
        eventSubscriptions.add("CUSTOM AsrEvent");
        eventSubscriptions.add("CUSTOM TtsEvent");

        EslConnectionDetail.setEventSubscriptions(eventSubscriptions);
        List<FreeswitchNodeInfo> nodeList = new ArrayList<>(8);
        
        FreeswitchNodeInfo nodeInfo = new FreeswitchNodeInfo();
        nodeInfo.setHost("127.0.0.1");
        nodeInfo.setPort(8021);
        nodeInfo.setPass("ClueCon");
        nodeInfo.setPoolSize(10);
        nodeList.add(nodeInfo);
        // Multiple nodes can be added, but only one node is added in this demonstration
        EslConnectionUtil.initConnPool(nodeList);
```

a. define a listenter:

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
            // Time-consuming operation, put a new thread pool;
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

execute single `event socket` command:

```java
EslConnectionUtil.sendExecuteCommand(
	 "hangup",
	 "recvHangupSignal",
	 uuid
);
```

Execute api synchronously. It is suitable for short operations that can get the return result immediately：

```java
EslMessage apiResponseMsg = EslConnectionUtil.sendSyncApiCommand(
	"uuid_exists",
	uuid
);
```


For time-consuming operations, such as the originate command, 
it is recommended to use an asynchronous api and obtain the result through a callback:

```java
EslConnectionPool eslConnectionPool = EslConnectionUtil.getEslConnectionPool("127.0.0.1", 8021);
String uuid =  UUID.randomUUID().toString();
StringBuilder callPrefix = new StringBuilder();
callPrefix.append(String.format(
		"hangup_after_bridge=true,origination_uuid=%s,absolute_codec_string=pcma,ignore_early_media=false,",
		uuid
));
String callParameter = String.format("{%s}sofia/gateway/%s/15005600327  &park()",
		callPrefix.toString(),
		"mygateway"
);
IEslEventListener eslListener  = new CallListener("", uuid);
String response = EslConnectionUtil.sendAsyncApiCommand("originate", callParameter);
if(!StringUtils.isEmpty(response)){
	eslConnectionPool.getDefaultEslConn().addListener(response, eslListener);
}
```
 
 
 
 
 
 
 
 