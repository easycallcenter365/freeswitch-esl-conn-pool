

package link.thingscloud.freeswitch.esl.inbound.option;

import link.thingscloud.freeswitch.esl.inbound.listener.EventListener;
import link.thingscloud.freeswitch.esl.inbound.listener.ServerOptionListener;
import link.thingscloud.freeswitch.esl.IEslEventListener;
import link.thingscloud.freeswitch.esl.ServerConnectionListener;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>InboundClientOption class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
@ToString
public class InboundClientOption {

    private int sndBufSize = 65535;
    private int rcvBufSize = 65535;

    //设置线程的个数=Ncpu*2
    //    private int workerGroupThread = Runtime.getRuntime().availableProcessors() * 2;
    //    private int publicExecutorThread = Runtime.getRuntime().availableProcessors() * 2;
    //    private int callbackExecutorThread = Runtime.getRuntime().availableProcessors() * 2;

    private int workerGroupThread =  2;
    private int publicExecutorThread = 2;
    private int callbackExecutorThread = 2;

    private int defaultTimeoutSeconds = 5;
    private String defaultPassword = "ClueCon";

    private int readTimeoutSeconds = 30;
    private int readerIdleTimeSeconds = 25;

    private boolean disablePublicExecutor = false;
    private boolean performance = false;
    private long performanceCostTime = 200;
    private boolean eventPerformance = false;
    private long eventPerformanceCostTime = 200;

    private ServerOptionListener serverOptionListener = null;
    private ServerConnectionListener serverConnectionListener = null;
    private final List<ServerOption> serverOptions = new ArrayList<>();
    private final ServerAddrOption serverAddrOption = new ServerAddrOption(serverOptions);

    private final List<IEslEventListener> listeners = new ArrayList<>();

    private EventListener eventListener = null;
    private final List<String> events = new ArrayList<>();

    /**
     * <p>sndBufSize.</p>
     *
     * @return a int.
     */
    public int sndBufSize() {
        return sndBufSize;
    }

    /**
     * <p>sndBufSize.</p>
     *
     * @param sndBufSize a int.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption sndBufSize(int sndBufSize) {
        this.sndBufSize = sndBufSize;
        return this;
    }

    /**
     * <p>rcvBufSize.</p>
     *
     * @return a int.
     */
    public int rcvBufSize() {
        return rcvBufSize;
    }

    /**
     * <p>rcvBufSize.</p>
     *
     * @param rcvBufSize a int.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption rcvBufSize(int rcvBufSize) {
        this.rcvBufSize = rcvBufSize;
        return this;
    }

    /**
     * <p>workerGroupThread.</p>
     *
     * @return a int.
     */
    public int workerGroupThread() {
        return workerGroupThread;
    }

    /**
     * <p>workerGroupThread.</p>
     *
     * @param workerGroupThread a int.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption workerGroupThread(int workerGroupThread) {
        this.workerGroupThread = workerGroupThread;
        return this;
    }

    /**
     * <p>publicExecutorThread.</p>
     *
     * @return a int.
     */
    public int publicExecutorThread() {
        return publicExecutorThread;
    }

    /**
     * <p>publicExecutorThread.</p>
     *
     * @param publicExecutorThread a int.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption publicExecutorThread(int publicExecutorThread) {
        this.publicExecutorThread = publicExecutorThread;
        return this;
    }

    /**
     * <p>callbackExecutorThread.</p>
     *
     * @return a int.
     */
    public int callbackExecutorThread() {
        return callbackExecutorThread;
    }

    /**
     * <p>callbackExecutorThread.</p>
     *
     * @param callbackExecutorThread a int.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption callbackExecutorThread(int callbackExecutorThread) {
        this.callbackExecutorThread = callbackExecutorThread;
        return this;
    }

    /**
     * <p>defaultTimeoutSeconds.</p>
     *
     * @return a int.
     */
    public int defaultTimeoutSeconds() {
        return defaultTimeoutSeconds;
    }

    /**
     * <p>defaultTimeoutSeconds.</p>
     *
     * @param defaultTimeoutSeconds a int.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption defaultTimeoutSeconds(int defaultTimeoutSeconds) {
        this.defaultTimeoutSeconds = defaultTimeoutSeconds;
        return this;
    }

    /**
     * <p>defaultPassword.</p>
     *
     * @return a {@link String} object.
     */
    public String defaultPassword() {
        return defaultPassword;
    }

    /**
     * <p>defaultPassword.</p>
     *
     * @param defaultPassword a {@link String} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption defaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
        return this;
    }


    /**
     * <p>readTimeoutSeconds.</p>
     *
     * @return a {@link Integer} object.
     */
    public int readTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    /**
     * <p>readTimeoutSeconds.</p>
     *
     * @param readTimeoutSeconds a {@link Integer} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption readTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
        return this;
    }

    /**
     * <p>readerIdleTimeSeconds.</p>
     *
     * @return a {@link Integer} object.
     */
    public int readerIdleTimeSeconds() {
        return readerIdleTimeSeconds;
    }

    /**
     * <p>readerIdleTimeSeconds.</p>
     * <p>
     * 读空闲时长
     *
     * @param readerIdleTimeSeconds a {@link Integer} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption readerIdleTimeSeconds(int readerIdleTimeSeconds) {
        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
        return this;
    }


    /**
     * <p>
     * disable public executor thread pool then message thread safety.
     * 处理事件消息时不使用独立线程池，可以使得消息线程安全
     * </p>
     *
     * @return a boolean.
     */
    public boolean disablePublicExecutor() {
        return disablePublicExecutor;
    }

    /**
     * <p>
     * disable public executor thread pool then message thread safety.
     * 处理事件消息时不使用独立线程池，可以使得消息线程安全
     * </p>
     *
     * @param disablePublicExecutor a boolean.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption disablePublicExecutor(boolean disablePublicExecutor) {
        this.disablePublicExecutor = disablePublicExecutor;
        return this;
    }

    /**
     * <p>performance.</p>
     *
     * @return a boolean.
     */
    public boolean performance() {
        return performance;
    }

    /**
     * <p>performance.</p>
     *
     * @param performance a boolean.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption performance(boolean performance) {
        this.performance = performance;
        return this;
    }

    /**
     * <p>performanceCostTime.</p>
     *
     * @return a long.
     */
    public long performanceCostTime() {
        return performanceCostTime;
    }

    /**
     * <p>performanceCostTime.</p>
     *
     * @param performanceCostTime a long.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption performanceCostTime(long performanceCostTime) {
        this.performanceCostTime = performanceCostTime;
        return this;
    }

    /**
     * <p>eventPerformance.</p>
     *
     * @return a boolean.
     */
    public boolean eventPerformance() {
        return eventPerformance;
    }

    /**
     * <p>eventPerformance.</p>
     *
     * @param eventPerformance a boolean.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption eventPerformance(boolean eventPerformance) {
        this.eventPerformance = eventPerformance;
        return this;
    }

    /**
     * <p>eventPerformanceCostTime.</p>
     *
     * @return a long.
     */
    public long eventPerformanceCostTime() {
        return eventPerformanceCostTime;
    }

    /**
     * <p>eventPerformanceCostTime.</p>
     *
     * @param eventPerformanceCostTime a long.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption eventPerformanceCostTime(long eventPerformanceCostTime) {
        this.eventPerformanceCostTime = eventPerformanceCostTime;
        return this;
    }

    /**
     * <p>serverOptionListener.</p>
     *
     * @return a {@link ServerOptionListener} object.
     */
    public ServerOptionListener serverOptionListener() {
        return serverOptionListener;
    }

    /**
     * <p>serverOptionListener.</p>
     *
     * @param serverOptionListener a {@link ServerOptionListener} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption serverOptionListener(ServerOptionListener serverOptionListener) {
        this.serverOptionListener = serverOptionListener;
        return this;
    }

    /**
     * <p>serverConnectionListener.</p>
     *
     * @return a {@link ServerOptionListener} object.
     */
    public ServerConnectionListener serverConnectionListener() {
        return serverConnectionListener;
    }

    /**
     * <p>serverConnectionListener.</p>
     *
     * @param serverConnectionListener a {@link ServerConnectionListener} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption serverConnectionListener(ServerConnectionListener serverConnectionListener) {
        this.serverConnectionListener = serverConnectionListener;
        return this;
    }

    /**
     * <p>serverAddrOption.</p>
     *
     * @return a {@link ServerAddrOption} object.
     */
    public ServerAddrOption serverAddrOption() {
        return serverAddrOption;
    }

    /**
     * <p>serverOptions.</p>
     *
     * @return a {@link List} object.
     */
    public List<ServerOption> serverOptions() {
        return serverOptions;
    }

    /**
     * <p>addServerOption.</p>
     *
     * @param serverOption a {@link ServerOption} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption addServerOption(ServerOption serverOption) {
        for (ServerOption option : serverOptions) {
            if (StringUtils.equals(option.addr(), serverOption.addr())) {
                return this;
            }
        }
        serverOptions.add(serverOption);
        if (serverOptionListener != null) {
            serverOptionListener.onAdded(serverOption);
        }
        return this;
    }

    /**
     * <p>removeServerOption.</p>
     *
     * @param serverOption a {@link ServerOption} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption removeServerOption(ServerOption serverOption) {
        serverOptions.remove(serverOption);
        if (serverOptionListener != null) {
            serverOptionListener.onRemoved(serverOption);
        }
        return this;
    }

    /**
     * <p>addListener.</p>
     *
     * @param listener a {@link IEslEventListener} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption addListener(IEslEventListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * <p>removeListener.</p>
     *
     * @param listener a {@link IEslEventListener} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption removeListener(IEslEventListener listener) {
        listeners.remove(listener);
        return this;
    }

    /**
     * <p>listeners.</p>
     *
     * @return a {@link List} object.
     */
    public List<IEslEventListener> listeners() {
        return listeners;
    }

    /**
     * <p>eventListener.</p>
     *
     * @return a {@link EventListener} object.
     */
    public EventListener eventListener() {
        return eventListener;
    }

    /**
     * <p>eventListener.</p>
     *
     * @param eventListener a {@link EventListener} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption eventListener(EventListener eventListener) {
        this.eventListener = eventListener;
        return this;
    }

    /**
     * <p>events.</p>
     *
     * @return a {@link List} object.
     */
    public List<String> events() {
        return events;
    }

    /**
     * <p>addEvents.</p>
     *
     * @param addEvents a {@link String} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption addEvents(String... addEvents) {
        if (addEvents == null) {
            return this;
        }

        List<String> list = new ArrayList<>();
        for (String addEvent : addEvents) {
            if (!events().contains(addEvent)) {
                list.add(addEvent);
            }
        }
        if (!list.isEmpty()) {
            events.addAll(list);
            if (eventListener != null) {
                eventListener.addEvents(list);
            }
        }
        return this;
    }

    /**
     * <p>addEvents.</p>
     *
     * @param list a {@link String} object.
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption addEvents(List<String> list) {
        if (!list.isEmpty()) {
            events.addAll(list);
            if (eventListener != null) {
                eventListener.addEvents(list);
            }
        }
        return this;
    }

    /**
     * <p>cancelEvents.</p>
     *
     * @return a {@link InboundClientOption} object.
     */
    public InboundClientOption cancelEvents() {
        if (!events.isEmpty()) {
            if (eventListener != null) {
                eventListener.cancelEvents();
            }
            events.clear();
        }
        return this;
    }


}
