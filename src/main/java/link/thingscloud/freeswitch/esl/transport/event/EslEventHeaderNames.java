

package link.thingscloud.freeswitch.esl.transport.event;

/**
 * <p>EslEventHeaderNames class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public class EslEventHeaderNames {
    /**
     * {@code "Event-Name"}
     */
    public static final String EVENT_NAME = "Event-Name";

    public static final  String EVENT_HEARTBEAT = "HEARTBEAT";

    /**
     * {@code "Event-Date-Local"}
     */
    public static final String EVENT_DATE_LOCAL = "Event-Date-Local";
    /**
     * {@code "Event-Date-GMT"}
     */
    public static final String EVENT_DATE_GMT = "Event-Date-GMT";
    /**
     * {@code "Event-Date-Timestamp"}
     */
    public static final String EVENT_DATE_TIMESTAMP = "Event-Date-Timestamp";
    /**
     * {@code "Event-Calling-File"}
     */
    public static final String EVENT_CALLING_FILE = "Event-Calling-File";
    /**
     * {@code "Event-Calling-Function"}
     */
    public static final String EVENT_CALLING_FUNCTION = "Event-Calling-Function";
    /**
     * {@code "Event-Calling-Line-Number"}
     */
    public static final String EVENT_CALLING_LINE_NUMBER = "Event-Calling-Line-Number";
    /**
     * {@code "FreeSWITCH-Hostname"}
     */
    public static final String FREESWITCH_HOSTNAME = "FreeSWITCH-Hostname";
    /**
     * {@code "FreeSWITCH-IPv4"}
     */
    public static final String FREESWITCH_IPV4 = "FreeSWITCH-IPv4";
    /**
     * {@code "FreeSWITCH-IPv6"}
     */
    public static final String FREESWITCH_IPV6 = "FreeSWITCH-IPv6";
    /**
     * {@code "Core-UUID"}
     */
    public static final String CORE_UUID = "Core-UUID";
    /**
     * {@code "Content-Length"}
     */
    public static final String CONTENT_LENGTH = "Content-Length";
    /**
     * {@code "Job-Command"}
     */
    public static final String JOB_COMMAND = "Job-Command";
    /**
     * {@code "Job-UUID"}
     */
    public static final String JOB_UUID = "Job-UUID";

    private EslEventHeaderNames() {
        /* private class */
    }
}
