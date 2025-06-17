package link.thingscloud.freeswitch.esl;

import link.thingscloud.freeswitch.esl.constant.EventNames;
import link.thingscloud.freeswitch.esl.inbound.option.InboundClientOption;
import link.thingscloud.freeswitch.esl.inbound.option.ServerOption;
import link.thingscloud.freeswitch.esl.transport.event.EslEvent;
import link.thingscloud.freeswitch.esl.transport.event.EslEventHeaderNames;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * InboundClient的包装类，提供了指令发送，异步消息接收等能力。
 * @author  easycallcenter365@gmail.com
 */
public class EslConnectionDetail  implements IEslEventListener {
    private final static Logger logger = LoggerFactory.getLogger(EslConnectionDetail.class);
    private InboundClientOption inboundOption = null;
    private ServerOption serverOption  = null;
    private InboundClient conn  = null;
    private  final static ConcurrentHashMap<String, IEslEventListener> LISTENTERS = new ConcurrentHashMap<>(3000);
    private  final static ConcurrentHashMap<String, DelayCallInfo> DELAY_REMOVE_CALLINFO_LIST = new ConcurrentHashMap<>(3000);
    private   IEslEventListener defaultListener = null;
    private static List<String> eventSubscriptions = new ArrayList<>();
    private LinkedBlockingQueue<DelayEventInfo> delayEventInfoList;
    /**
     *  上次心跳时间
     */
    private volatile Long lastBeatTime = System.currentTimeMillis();
    /**
     * 当前通话数;
     */
    private  int callSessionCount = 0;
    /**
     * 当前CPU使用情况
     */
    private   String cpuIdle = "";
    /**
     * 每秒允许的呼叫数
     */
    private   int sessionPerSec = 30;
    /**
     * Freeswitch启动至今的毫秒数;
     */
    private   Long uptimeMsec = 0L;

    public EslConnectionDetail(InboundClientOption inboundOption,
                               ServerOption serverOption,
                               InboundClient conn
                               ){
        this.inboundOption = inboundOption;
        this.serverOption = serverOption;
        this.conn = conn;

        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                logger.info(" DELAY_REMOVE_CALLINFO_LIST cleaner thread is running.");
                while (true){
                    try{
                        Iterator<Map.Entry<String, DelayCallInfo>> it = DELAY_REMOVE_CALLINFO_LIST.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, DelayCallInfo> entry = it.next();
                            DelayCallInfo task = entry.getValue();
                            long timePassed = System.currentTimeMillis() -  task.getHangupTime();
                            if(timePassed > 11000){
                                LISTENTERS.remove(task.getUuid());
                                LISTENTERS.remove(task.getUuid() + "-ex");
                                it.remove();
                                logger.info("******* remove call uuid from delayCallInfo list, uuid={}, hangupTime={}.", task.getUuid(), task.getHangupTime());
                            }
                        }
                    }catch (Throwable e){
                       logger.error("DELAY_REMOVE_CALLINFO_LIST thread error: {} {}", e.toString(), CommonUtils.getStackTraceString(e.getStackTrace()));
                    }
                    Thread.sleep(3000);
                }
            }
        }).start();

        // 如果订阅了相关事件; 需要开启 delayEventInfoList 消息清理线程;
        if(inboundOption.events().size() > 0){
            delayEventInfoList = new LinkedBlockingQueue<>();
            new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    do {
                        long now = System.currentTimeMillis();
                        Iterator<DelayEventInfo> iterator = delayEventInfoList.iterator();
                        while (iterator.hasNext()) {
                            DelayEventInfo eventInfo = iterator.next();
                            long timePassed = now - eventInfo.getCreateTime();
                            if(timePassed > 1000) {
                                IEslEventListener msgHandle = LISTENTERS.get(eventInfo.getJobUuid());
                                if (null != msgHandle) {
                                    //找到了订阅者，则投递消息并删除;
                                    msgHandle.backgroundJobResultReceived("", eventInfo.getEvent());
                                    delayEventInfoList.remove(eventInfo);
                                }else {
                                    //没找到订阅者且时间超过9秒则直接删除消息;
                                    if (timePassed > 9000) {
                                        delayEventInfoList.remove(eventInfo);
                                    }
                                }
                            }
                        }
                        Thread.sleep(2000);
                    }while (true);
                }
            }, "Esl-BackGround-Job-Msg-Cleaner").start();
        }
    }

    public void addListener(String uuid, IEslEventListener listener ){
        LISTENTERS.put(uuid, listener);
    }

    /**
     *  增加默认的消息接收器，用于接收订阅的全部的esl消息;
     * @param listener
     */
    public void addDefaultListener( IEslEventListener listener){
        defaultListener = listener;
    }

    public  void removeListener(String uuid) {
        LISTENTERS.remove(uuid);
    }

    private static String[] subscriberKeyList = {
            "-acd",
            "-batchcall",
            "-robot",
            "-ex",
            "-ex1",
            "-ex2"
    };

    /**
     *  获取真正的esl的socket连接对象;
     * @return
     */
    public InboundClient getRealConn(){
        return  conn;
    }

    @Override
    public void eventReceived(String addr, EslEvent event) {
        String uuid = event.getEventHeaders().get("Unique-ID");
        String heartbeat = EslEventHeaderNames.EVENT_HEARTBEAT;
        String eventName = event.getEventName();
        Map<String, String> headers = event.getEventHeaders();
        if (eventName.equalsIgnoreCase(heartbeat)) {
            callSessionCount = Integer.parseInt(headers.get("Session-Count"));
            cpuIdle = headers.get("Idle-CPU");
            sessionPerSec = Integer.parseInt(headers.get("Session-Per-Sec"));
            uptimeMsec = Long.parseLong(headers.get("Uptime-msec"));
            lastBeatTime = System.currentTimeMillis();
            logger.debug("RECV fs EVENT_HEARTBEAT, callSessionCount:{},cpuIdle:{},sessionPerSec:{},uptimeMsec:{},lastBeatTime:{}",
                    callSessionCount,
                    cpuIdle,
                    sessionPerSec,
                    uptimeMsec,
                    lastBeatTime
            );
            return;
        }
        logger.info("eventReceived:{}, uuid:{}", event.getEventName(), uuid);
        IEslEventListener msgHandle = LISTENTERS.get(uuid);
        if(null != msgHandle){
            msgHandle.eventReceived(addr, event);
        }else{
            logger.debug("eventReceived 放弃esl消息, 找不到指定的  handle.  callUuid:{}, msg: {}...",
                    uuid, event.toString());
        }

        // 解决同一个uuid注册多个监听器的问题; 增加多个扩展;
        for (String key : subscriberKeyList) {
            String uuidEx = uuid + key;
            msgHandle = LISTENTERS.get(uuidEx);
            if(null != msgHandle){
                msgHandle.eventReceived(addr, event);
            }
        }


        // 如果是挂机，则自动移除listener对象;
        if(EventNames.CHANNEL_HANGUP.equalsIgnoreCase(eventName)){
            DELAY_REMOVE_CALLINFO_LIST.put(uuid, new DelayCallInfo(uuid));
        }

        if(null != defaultListener) {
            defaultListener.eventReceived(addr, event);
        }
    }

    @Override
    public void backgroundJobResultReceived(String addr, EslEvent event) {
        String jobUuid = event.getEventHeaders().get("Job-UUID");
        IEslEventListener msgHandle = LISTENTERS.get(jobUuid);
        if(null != msgHandle){
            msgHandle.backgroundJobResultReceived(addr, event);
            LISTENTERS.remove(jobUuid);
        }else{
            logger.info("backgroundJobResultReceived event msg 放入延迟队列，稍后再尝试投递, 找不到指定的  handle.  jobUUID:{}, msg: {}...",
                    jobUuid, event.toString());
            delayEventInfoList.add(new DelayEventInfo(event, System.currentTimeMillis(), jobUuid));
        }
        if(null != defaultListener) {
            defaultListener.backgroundJobResultReceived(addr, event);
        }
    }

    public Long getLastBeatTime() {
        return lastBeatTime;
    }

    public void setLastBeatTime(Long lastBeatTime) {
        this.lastBeatTime = lastBeatTime;
    }

    public int getCallSessionCount() {
        return callSessionCount;
    }

    public void setCallSessionCount(int callSessionCount) {
        this.callSessionCount = callSessionCount;
    }

    public String getCpuIdle() {
        return cpuIdle;
    }

    public void setCpuIdle(String cpuIdle) {
        this.cpuIdle = cpuIdle;
    }

    public int getSessionPerSec() {
        return sessionPerSec;
    }

    public void setSessionPerSec(int sessionPerSec) {
        this.sessionPerSec = sessionPerSec;
    }

    public Long getUptimeMsec() {
        return uptimeMsec;
    }

    public void setUptimeMsec(Long uptimeMsec) {
        this.uptimeMsec = uptimeMsec;
    }

    public static List<String> getEventSubscriptions() {
        return eventSubscriptions;
    }

    public static void setEventSubscriptions(List<String> eventSubscriptions) {
        EslConnectionDetail.eventSubscriptions = eventSubscriptions;
    }
}
