package link.thingscloud.freeswitch.esl;

import lombok.Data;

/**
 *  挂机事件延迟处理对象；
 *
 *  背景：
 *  有一些esl消息在挂机后的1秒后，才产生并接收到，此时由于 EslConnectionDetail 中已经移除了该通话uui监听器的信息，
 *  从而导致该esl无法被投递成，消费端也就无法接收到。
 *  解决办法是，在收到挂机事件之后，EslConnectionDetail不移除该通话的uuid对象的监听器，
 *  转而把他放入到一个延时处理队列中。 在通话挂机的5-7秒后才从延时队列中移除该对象即可。*
 *
 *
 */
@Data
public class DelayCallInfo{
        private String uuid;
        private long hangupTime;

        public DelayCallInfo(String uuid) {
        this.uuid = uuid;
        this.hangupTime = System.currentTimeMillis();
    }
}