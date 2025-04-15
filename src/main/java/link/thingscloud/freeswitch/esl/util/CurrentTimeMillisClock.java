package link.thingscloud.freeswitch.esl.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *  获取当前系统时间戳;
 */
public class CurrentTimeMillisClock {

    public static long now() {
        return System.currentTimeMillis();
    }

}
