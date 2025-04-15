
package link.thingscloud.freeswitch.esl.exception;

/**
 * <p>InboundClientException class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public class InboundClientException extends RuntimeException {
    /**
     * <p>Constructor for InboundClientException.</p>
     *
     * @param message a {@link String} object.
     */
    public InboundClientException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for InboundClientException.</p>
     *
     * @param message a {@link String} object.
     * @param cause   a {@link Throwable} object.
     */
    public InboundClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>Constructor for InboundClientException.</p>
     *
     * @param cause a {@link Throwable} object.
     */
    public InboundClientException(Throwable cause) {
        super(cause);
    }
}
