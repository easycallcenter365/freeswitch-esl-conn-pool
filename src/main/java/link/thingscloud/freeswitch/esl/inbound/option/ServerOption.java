

package link.thingscloud.freeswitch.esl.inbound.option;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>ServerOption class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */

@Data
@Accessors(fluent = true)
public class ServerOption {
    private final String host;
    private final int port;
    private int timeoutSeconds;
    private String password;

    private ConnectState state = ConnectState.INIT;

    private int connectTimes = 0;

    /**
     * <p>addr.</p>
     *
     * @return a {@link String} object.
     */
    public String addr() {
        return host + ":" + port;
    }

    /**
     * <p>addConnectTimes.</p>
     */
    public void addConnectTimes() {
        connectTimes++;
    }
}
