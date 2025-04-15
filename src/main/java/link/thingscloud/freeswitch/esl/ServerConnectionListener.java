package link.thingscloud.freeswitch.esl;

import link.thingscloud.freeswitch.esl.inbound.option.ServerOption;

/**
 * <p>ServerConnectionListener interface.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public interface ServerConnectionListener {

    /**
     * <p>onOpened.</p>
     *
     * @param serverOption a {@link ServerOption} object.
     */
    void onOpened(ServerOption serverOption);

    /**
     * <p>onClosed.</p>
     *
     * @param serverOption a {@link ServerOption} object.
     */
    void onClosed(ServerOption serverOption);

}
