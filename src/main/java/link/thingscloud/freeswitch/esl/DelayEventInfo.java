package link.thingscloud.freeswitch.esl;

import link.thingscloud.freeswitch.esl.transport.event.EslEvent;


/**
 *
 * @author  easycallcenter365@gmail.com
 */
public class DelayEventInfo {
    private EslEvent event;
    private long createTime;
    private String jobUuid;

    public DelayEventInfo(EslEvent event, long createTime, String jobUuid) {
        this.event = event;
        this.createTime = createTime;
        this.jobUuid = jobUuid;
    }

    public EslEvent getEvent() {
        return event;
    }

    public void setEvent(EslEvent event) {
        this.event = event;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getJobUuid() {
        return jobUuid;
    }

    public void setJobUuid(String jobUuid) {
        this.jobUuid = jobUuid;
    }
}
