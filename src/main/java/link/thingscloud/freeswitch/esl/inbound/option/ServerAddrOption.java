

package link.thingscloud.freeswitch.esl.inbound.option;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * <p>ServerAddrOption class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public class ServerAddrOption {
    private final List<ServerOption> serverOptions;
    private static final String VALIDATE_MESSAGE_1 = "serverOptions must be not empty!";

    ServerAddrOption(List<ServerOption> serverOptions) {
        this.serverOptions = serverOptions;
    }

    /**
     * <p>first.</p>
     *
     * @return a {@link String} object.
     */
    public String first() {
        Validate.notEmpty(serverOptions, VALIDATE_MESSAGE_1);
        return serverOptions.get(0).addr();
    }

    /**
     * <p>last.</p>
     *
     * @return a {@link String} object.
     */
    public String last() {
        Validate.notEmpty(serverOptions, VALIDATE_MESSAGE_1);
        return serverOptions.get(serverOptions.size() - 1).addr();
    }

    /**
     * <p>random.</p>
     *
     * @return a {@link String} object.
     */
    public String random() {
        Validate.notEmpty(serverOptions, VALIDATE_MESSAGE_1);
        return serverOptions.get(RandomUtils.nextInt(0, serverOptions.size())).addr();
    }


}
