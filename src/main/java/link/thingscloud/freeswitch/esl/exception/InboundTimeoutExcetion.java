

package link.thingscloud.freeswitch.esl.exception;

/**
 * <p>InboundTimeoutExcetion class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public class InboundTimeoutExcetion extends InboundClientException {
    /**
     * <p>Constructor for InboundTimeoutExcetion.</p>
     *
     * @param message a {@link String} object.
     */
    public InboundTimeoutExcetion(String message) {
        super(message);
    }

    /**
     * <p>Constructor for InboundTimeoutExcetion.</p>
     *
     * @param message a {@link String} object.
     * @param cause   a {@link Throwable} object.
     */
    public InboundTimeoutExcetion(String message, Throwable cause) {
        super(message, cause);
    }
}
