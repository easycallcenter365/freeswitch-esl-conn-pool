

package link.thingscloud.freeswitch.esl.inbound.listener;

import java.util.List;

/**
 * <p>EventListener interface.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public interface EventListener {

    /**
     * <p>addEvents.</p>
     *
     * @param list a {@link List} object.
     */
    void addEvents(List<String> list);

    /**
     * <p>cancelEvents.</p>
     */
    void cancelEvents();

}
