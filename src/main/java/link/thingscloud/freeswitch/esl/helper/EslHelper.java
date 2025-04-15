package link.thingscloud.freeswitch.esl.helper;

import link.thingscloud.freeswitch.esl.transport.event.EslEvent;
import link.thingscloud.freeswitch.esl.transport.message.EslHeaders;
import link.thingscloud.freeswitch.esl.transport.message.EslMessage;

import java.util.List;
import java.util.Map;

/**
 * <p>EslHelper class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public class EslHelper {

    /**
     * private constructor
     */
    private EslHelper() {
    }

    /**
     * <p>formatEslEvent.</p>
     *
     * @param event a {@link EslEvent} object.
     * @return a {@link String} object.
     */
    public static String formatEslEvent(EslEvent event) {

        StringBuilder sb = new StringBuilder();

        sb.append(System.lineSeparator());
        sb.append("#").append(System.lineSeparator());
        sb.append("## message header : ").append(System.lineSeparator());
        Map<EslHeaders.Name, String> messageHeaders =
                event.getMessageHeaders();
        for (Map.Entry<EslHeaders.Name, String> entry : messageHeaders.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(System.lineSeparator());
        }

        sb.append("## event header : ").append(System.lineSeparator());
        Map<String, String> eventHeaders = event.getEventHeaders();
        for (Map.Entry<String, String> entry : eventHeaders.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(System.lineSeparator());

        }

        sb.append("## event body lines : ").append(System.lineSeparator());
        List<String> eventBodyLines = event.getEventBodyLines();
        for (String eventBodyLine : eventBodyLines) {
            sb.append(eventBodyLine).append(System.lineSeparator());
        }
        sb.append("#").append(System.lineSeparator());

        return sb.toString();
    }


    /**
     * <p>formatEslMessage.</p>
     *
     * @param message a {@link EslMessage} object.
     * @return a {@link String} object.
     */
    public static String formatEslMessage(EslMessage message) {
        StringBuilder sb = new StringBuilder();

        sb.append(System.lineSeparator());
        sb.append("#").append(System.lineSeparator());
        sb.append("## message header : ").append(System.lineSeparator());

        Map<EslHeaders.Name, String> headers =
                message.getHeaders();
        for (Map.Entry<EslHeaders.Name, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(System.lineSeparator());
        }

        sb.append("## event body lines : ").append(System.lineSeparator());
        List<String> eventBodyLines = message.getBodyLines();
        for (String eventBodyLine : eventBodyLines) {
            sb.append(eventBodyLine).append(System.lineSeparator());
        }
        sb.append("#").append(System.lineSeparator());

        return sb.toString();
    }
}
