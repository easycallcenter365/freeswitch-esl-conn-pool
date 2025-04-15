
package link.thingscloud.freeswitch.esl.inbound.listener;

import link.thingscloud.freeswitch.esl.inbound.option.ServerOption;

/**
 * <p>ServerOptionListener interface.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public interface ServerOptionListener {

    /**
     * <p>onAdded.</p>
     *
     * @param serverOption a {@link ServerOption} object.
     */
    void onAdded(ServerOption serverOption);

    /**
     * <p>onRemoved.</p>
     *
     * @param serverOption a {@link ServerOption} object.
     */
    void onRemoved(ServerOption serverOption);

}
