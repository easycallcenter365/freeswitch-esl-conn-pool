

package link.thingscloud.freeswitch.esl;

import link.thingscloud.freeswitch.esl.inbound.option.InboundClientOption;
import link.thingscloud.freeswitch.esl.exception.InboundClientException;
import link.thingscloud.freeswitch.esl.inbound.NettyInboundClient;

/**
 * 保证单例对象
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 */
class InboundClientFactory {

    private InboundClient inboundClient = null;

    private InboundClientFactory() {
    }

    static InboundClientFactory getInstance() {
        return InboundClientFactoryInstance.INSTANCE;
    }

    synchronized InboundClient newInboundClient(InboundClientOption option) {
        if (inboundClient == null) {
            inboundClient = new NettyInboundClient(option == null ? new InboundClientOption() : option);
            return inboundClient;
        }
        throw new InboundClientException("InboundClient has been created already, instance : [" + inboundClient + "]!");
    }

    InboundClient getInboundClient() {
        if (inboundClient == null) {
            throw new InboundClientException("InboundClient is null, you must be create it first.");
        }
        return inboundClient;
    }

    private static class InboundClientFactoryInstance {
        private static final InboundClientFactory INSTANCE = new InboundClientFactory();
    }

}
