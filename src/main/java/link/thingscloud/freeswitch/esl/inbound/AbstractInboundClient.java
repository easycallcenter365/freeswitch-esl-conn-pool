 

package link.thingscloud.freeswitch.esl.inbound;

import link.thingscloud.freeswitch.esl.InboundClient;
import link.thingscloud.freeswitch.esl.constant.EslConstant;
import link.thingscloud.freeswitch.esl.inbound.handler.InboundChannelHandler;
import link.thingscloud.freeswitch.esl.inbound.listener.EventListener;
import link.thingscloud.freeswitch.esl.inbound.listener.ServerOptionListener;
import link.thingscloud.freeswitch.esl.inbound.option.ConnectState;
import link.thingscloud.freeswitch.esl.inbound.option.InboundClientOption;
import link.thingscloud.freeswitch.esl.inbound.option.ServerOption;
import link.thingscloud.freeswitch.esl.transport.CommandResponse;
import link.thingscloud.freeswitch.esl.transport.message.EslHeaders;
import link.thingscloud.freeswitch.esl.transport.message.EslMessage;
import io.netty.channel.ChannelFutureListener;
import link.thingscloud.freeswitch.esl.exception.InboundClientException;
import link.thingscloud.freeswitch.esl.transport.event.EslEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>flow :</p>
 * |------------------------------------|
 * |                                    |
 * \|            |---» CONNECTED  ---» CLOSED  ---» SHUTDOWN
 * INIT ----» CONNECTING -----|
 * /|            |---» FAILED
 * |                     |
 * ----------------------|
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 */
abstract class AbstractInboundClient extends AbstractNettyInboundClient implements InboundClient {

    private final ScheduledThreadPoolExecutor scheduledPoolExecutor = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("scheduled-pool-%d").daemon(true).build());

    private  InboundChannelHandler inboundChannelHandler = null;

    AbstractInboundClient(InboundClientOption option) {
        super(option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InboundClientOption option() {
        return option;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        log.info("inbound client will start ...");
        // 针对发起连接进行监听
        addServerOptionListener();
        // 针对订阅事件进行监听
        addEventListener();

        option().serverOptions().forEach(serverOption -> {
            if (serverOption.state() == ConnectState.INIT) {
                serverOption.state(ConnectState.CONNECTING);
                doConnect(serverOption);
            }
        });
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        log.info("inbound client will shutdown ...");
        option().serverOptions().forEach(serverOption -> {
            serverOption.state(ConnectState.SHUTDOWN);
            if (inboundChannelHandler != null) {
                inboundChannelHandler.close().addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("shutdown inbound client remote server [{}:{}] success.", serverOption.host(), serverOption.port());
                    } else {
                        log.info("shutdown inbound client remote server [{}:{}] failed, cause : ", serverOption.host(), serverOption.port(), future.cause());
                    }
                });
            }
        });
        workerGroup.shutdownGracefully();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChannelActive(String remoteAddr, InboundChannelHandler inboundChannelHandler) {
        this.inboundChannelHandler = inboundChannelHandler;
        // 连接监听
        // 由于连接池中的多个连接是相同的remoteAddr， 这里的事件订阅会有问题！
//        option().serverOptions().forEach(serverOption -> {
//            if (StringUtils.equals(serverOption.addr(), remoteAddr)) {
//                if (option().serverConnectionListener() != null) {
//                    option().serverConnectionListener().onOpened(serverOption);
//                }
//            }
//        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onChannelClosed(String remoteAddr) {
        option().serverOptions().forEach(serverOption -> {
            if (StringUtils.equals(serverOption.addr(), remoteAddr)) {
                // 连接监听
                //if (option().serverConnectionListener() != null) {
                //    option().serverConnectionListener().onClosed(serverOption);
                //}

                if (serverOption.state() != ConnectState.SHUTDOWN) {
                    serverOption.state(ConnectState.CLOSED);
                    scheduledPoolExecutor.schedule(() -> doConnect(serverOption), getTimeoutSeconds(serverOption), TimeUnit.SECONDS);

                    // 这里需要处理fs节点宕机后的情况； 如果指定的fs节点断开后，需要通过请求 fsnodes api接口，判断当前fs节点是否仍然有效， 如果无效则放弃连接尝试;
                    // TODO  实现fs节点可用性检测逻辑;

                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAuthRequest(String addr, InboundChannelHandler inboundChannelHandler) {
        log.info("Auth requested[{}], sending [auth {}]", addr, "*****");
        for (ServerOption serverOption : option().serverOptions()) {
            String password = serverOption.password();
            if (password == null) {
                password = option().defaultPassword();
            }
            if (StringUtils.equals(addr, serverOption.addr())) {
                EslMessage response = inboundChannelHandler.sendSyncSingleLineCommand("auth " + password);
                log.debug("Auth response [{}]", response);
                if (response.getContentType().equals(EslHeaders.Value.COMMAND_REPLY)) {
                    CommandResponse reply = new CommandResponse("auth " + password, response);
                    serverOption.state(ConnectState.AUTHED);
                    // 订阅事件成功后
                    synchronized (addr.intern()){
                        addr.intern().notify();
                    }
                    log.info("Auth response success={}, message=[{}]", reply.isOk(), reply.getReplyText());
                    if (!option().events().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (String event : option().events()) {
                            sb.append(event).append(" ");
                        }
                        setEventSubscriptions(addr, "plain", sb.toString());
                    }
                } else {
                    serverOption.state(ConnectState.AUTHED_FAILED);
                    log.error("Bad auth response message [{}]", response);
                    throw new IllegalStateException("Incorrect auth response");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEslEvent(String addr, EslEvent event) {
        option().listeners().forEach(listener -> {
            long start = 0L;
            if (option().performance()) {
                start = System.currentTimeMillis();
            }
            if (option().eventPerformance()) {
                long cost = 0L;
                if (start > 0L) {
                    cost = start - (event.getEventDateTimestamp() / 1000);
                } else {
                    cost = System.currentTimeMillis() - (event.getEventDateTimestamp() / 1000);
                }
                if (cost > option().eventPerformanceCostTime()) {
                    log.warn("[event performance] received esl event diff time : {}ms, event is blocked.", cost);
                }
            }
            log.debug("Event addr[{}] received [{}]", addr, event);
            /*
             *  Notify listeners in a different thread in order to:
             *    - not to block the IO threads with potentially long-running listeners
             *    - generally be defensive running other people's code
             *  Use a different worker thread pool for async job results than for event driven
             *  events to keep the latency as low as possible.
             */
            if (StringUtils.equals(event.getEventName(), EslConstant.BACKGROUND_JOB)) {
                try {
                    listener.backgroundJobResultReceived(addr, event);
                } catch (Throwable t) {
                    log.error("Error caught notifying listener of job result [{}], remote address [{}]", event, addr, t);
                }
            } else {
                try {
                    listener.eventReceived(addr, event);
                } catch (Throwable t) {
                    log.error("Error caught notifying listener of event [{}], remote address [{}]", event, addr, t);
                }
            }
            if (option().performance()) {
                long cost = System.currentTimeMillis() - start;
                if (cost >= option().performanceCostTime()) {
                    log.warn("[performance] handle esl event cost time : {}ms", cost);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleDisconnectNotice(String addr) {
        log.info("Disconnected[{}] ...", addr);
    }

    /**
     * <p>getAuthedHandler.</p>
     *
     * @param addr a {@link String} object.
     * @return a {@link InboundChannelHandler} object.
     */
    public InboundChannelHandler getAuthedHandler(String addr) {
        if (inboundChannelHandler == null) {
            throw new InboundClientException("not found inbound handler for addr : " + addr);
        }
        List<ServerOption> serverOptions = option().serverOptions();
        for (ServerOption serverOption : serverOptions) {
            if (StringUtils.equals(serverOption.addr(), addr)) {
                if (serverOption.state() != ConnectState.AUTHED) {
                    throw new InboundClientException("inbound handler is not authed for addr : " + addr);
                }
                break;
            }
        }
        return inboundChannelHandler;
    }

    private void addServerOptionListener() {
        option().serverOptionListener(new ServerOptionListener() {
            @Override
            public void onAdded(ServerOption serverOption) {
                if (serverOption.state() == ConnectState.INIT) {
                    doConnect(serverOption);
                }
            }

            @Override
            public void onRemoved(ServerOption serverOption) {
                if (serverOption.state() == ConnectState.CONNECTED || serverOption.state() == ConnectState.AUTHED) {
                    doClose(serverOption);
                }
            }
        });
    }

    private void addEventListener() {
        log.info("add event listener ...");
        option().eventListener(new EventListener() {
            @Override
            public void addEvents(List<String> list) {
                StringBuilder sb = new StringBuilder();
                for (String event : list) {
                    sb.append(event).append(" ");
                }
                option().serverOptions().forEach(serverOption -> publicExecutor.execute(() -> setEventSubscriptions(serverOption.addr(), "plain", sb.toString())));
            }

            @Override
            public void cancelEvents() {
                option().serverOptions().forEach(serverOption -> publicExecutor.execute(() -> cancelEventSubscriptions(serverOption.addr())));

            }
        });
    }

    private void doConnect(final ServerOption serverOption) {
        log.info("connect remote server [{}:{}] ...", serverOption.host(), serverOption.port());
        serverOption.addConnectTimes();
        serverOption.state(ConnectState.CONNECTING);
        // 通过netty的bootstrap去连接
        bootstrap.connect(serverOption.host(), serverOption.port()).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                serverOption.state(ConnectState.CONNECTED);
                log.info("connect remote server [{}:{}] success.", serverOption.host(), serverOption.port());
            } else {
                // 如果连接失败，更改连接状态为失败，开启一个线程5s尝试进行连接
                serverOption.state(ConnectState.FAILED);
                log.warn("connect remote server [{}:{}] failed, will try again, cause : ", serverOption.host(), serverOption.port(), future.cause());
                scheduledPoolExecutor.schedule(() -> doConnect(serverOption), getTimeoutSeconds(serverOption), TimeUnit.SECONDS);
            }
        });
    }

    private void doClose(ServerOption serverOption) {
        log.info("doClose remote server [{}:{}] success.", serverOption.host(), serverOption.port());
        serverOption.state(ConnectState.CLOSING);
        option().serverOptions().remove(serverOption);
        if (inboundChannelHandler != null) {
            inboundChannelHandler.close().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("close remote server [{}:{}] success.", serverOption.host(), serverOption.port());
                } else {
                    log.info("close remote server [{}:{}] failed, cause : ", serverOption.host(), serverOption.port(), future.cause());
                }
            });
        }
    }

    private int getTimeoutSeconds(ServerOption serverOption) {
        return serverOption.timeoutSeconds() == 0 ? option().defaultTimeoutSeconds() : serverOption.timeoutSeconds();
    }
}
