

package link.thingscloud.freeswitch.esl.inbound.listener;

import link.thingscloud.freeswitch.esl.inbound.handler.InboundChannelHandler;
import link.thingscloud.freeswitch.esl.transport.event.EslEvent;

/**
 * <p>ChannelEventListener interface.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public interface ChannelEventListener {

    /**
     * <p>onChannelActive.</p>
     *
     * @param remoteAddr            a {@link String} object.
     * @param inboundChannelHandler a {@link InboundChannelHandler} object.
     */
    void onChannelActive(String remoteAddr, InboundChannelHandler inboundChannelHandler);

    /**
     * <p>onChannelClosed.</p>
     *
     * @param remoteAddr a {@link String} object.
     */
    void onChannelClosed(String remoteAddr);

    /**
     * <p>handleAuthRequest.</p>
     *
     * @param remoteAddr            a {@link String} object.
     * @param inboundChannelHandler a {@link InboundChannelHandler} object.
     */
    void handleAuthRequest(String remoteAddr, InboundChannelHandler inboundChannelHandler);

    /**
     * <p>handleEslEvent.</p>
     *
     * @param remoteAddr a {@link String} object.
     * @param event      a {@link EslEvent} object.
     */
    void handleEslEvent(String remoteAddr, EslEvent event);

    /**
     * <p>handleDisconnectNotice.</p>
     *
     * @param remoteAddr a {@link String} object.
     */
    void handleDisconnectNotice(String remoteAddr);
}
