

package link.thingscloud.freeswitch.esl.transport;


import link.thingscloud.freeswitch.esl.transport.message.EslHeaders;
import link.thingscloud.freeswitch.esl.transport.message.EslMessage;

/**
 * <p>CommandResponse class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public class CommandResponse {

    private final String command;
    private final String replyText;
    private final EslMessage response;
    private final boolean success;

    /**
     * <p>Constructor for CommandResponse.</p>
     *
     * @param command  a {@link String} object.
     * @param response a {@link EslMessage} object.
     */
    public CommandResponse(String command, EslMessage response) {
        this.command = command;
        this.response = response;
        this.replyText = response == null ? "" : response.getHeaderValue(EslHeaders.Name.REPLY_TEXT);
        this.success = replyText.startsWith("+OK");
    }

    /**
     * <p>Getter for the field <code>command</code>.</p>
     *
     * @return the original command sent to the server
     */
    public String getCommand() {
        return command;
    }

    /**
     * <p>isOk.</p>
     *
     * @return true if and only if the response Reply-Text line starts with "+OK"
     */
    public boolean isOk() {
        return success;
    }

    /**
     * <p>Getter for the field <code>replyText</code>.</p>
     *
     * @return the full response Reply-Text line.
     */
    public String getReplyText() {
        return replyText;
    }

    /**
     * <p>Getter for the field <code>response</code>.</p>
     *
     * @return {@link EslMessage} the full response from the server
     */
    public EslMessage getResponse() {
        return response;
    }
}
