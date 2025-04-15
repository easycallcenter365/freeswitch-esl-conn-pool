

package link.thingscloud.freeswitch.esl.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * <p>RemotingUtil class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public class RemotingUtil {

    /**
     * private constructor
     */
    private RemotingUtil() {
    }

    /**
     * <p>socketAddress2String.</p>
     *
     * @param addr a {@link SocketAddress} object.
     * @return a {@link String} object.
     */
    public static String socketAddress2String(final SocketAddress addr) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) addr;
        return inetSocketAddress.getAddress().getHostAddress() +
                ":" +
                inetSocketAddress.getPort();
    }
}
