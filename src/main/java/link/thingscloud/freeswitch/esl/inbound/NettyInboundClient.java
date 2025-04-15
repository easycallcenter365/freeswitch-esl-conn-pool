

package link.thingscloud.freeswitch.esl.inbound;

import link.thingscloud.freeswitch.esl.InboundClient;
import link.thingscloud.freeswitch.esl.constant.EslConstant;
import link.thingscloud.freeswitch.esl.inbound.handler.InboundChannelHandler;
import link.thingscloud.freeswitch.esl.inbound.option.InboundClientOption;
import link.thingscloud.freeswitch.esl.transport.CommandResponse;
import link.thingscloud.freeswitch.esl.transport.SendEvent;
import link.thingscloud.freeswitch.esl.transport.SendMsg;
import link.thingscloud.freeswitch.esl.transport.message.EslMessage;
import link.thingscloud.freeswitch.esl.exception.InboundTimeoutExcetion;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * <p>NettyInboundClient class.</p>
 *
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 * @version $Id: $Id
 */
public class NettyInboundClient extends AbstractInboundClient {

    /**
     * <p>Constructor for NettyInboundClient.</p>
     *
     * @param option a {@link InboundClientOption} object.
     */
    public NettyInboundClient(InboundClientOption option) {
        super(option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EslMessage sendSyncApiCommand(String addr, String command, String arg) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        StringBuilder sb = new StringBuilder();
        if (command != null && !command.isEmpty()) {
            sb.append("api ");
            sb.append(command);
        }
        if (arg != null && !arg.isEmpty()) {
            sb.append(' ');
            sb.append(arg);
        }
        log.debug("sendSyncApiCommand addr : {}, command : {}, arg : {}", addr, command, arg);
        return handler.sendSyncSingleLineCommand(sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EslMessage sendSyncApiCommand(String addr, String command, String arg, long timeoutSeconds) throws InboundTimeoutExcetion {
        try {
            return publicExecutor.submit(() -> sendSyncApiCommand(addr, command, arg)).get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new InboundTimeoutExcetion(String.format("sendSyncApiCommand addr : %s, command : %s, arg : %s, timeoutSeconds : %s", addr, command, arg, timeoutSeconds), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendAsyncApiCommand(String addr, String command, String arg) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        StringBuilder sb = new StringBuilder();
        if (command != null && !command.isEmpty()) {
            sb.append("bgapi ");
            sb.append(command);
        }
        if (arg != null && !arg.isEmpty()) {
            sb.append(' ');
            sb.append(arg);
        }

        return handler.sendAsyncCommand(sb.toString());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendSyncApiCommand(String addr, String command, String arg, Consumer<EslMessage> consumer) {
        publicExecutor.execute(() -> {
            EslMessage msg = sendSyncApiCommand(addr, command, arg);
            if (consumer != null) {
                consumer.accept(msg);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendAsyncApiCommand(String addr, String command, String arg, Consumer<String> consumer) {
        publicExecutor.execute(() -> {
            String msg = sendAsyncApiCommand(addr, command, arg);
            if (consumer != null) {
                consumer.accept(msg);
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse setEventSubscriptions(String addr, String format, String events) {
        if (!EslConstant.PLAIN.equalsIgnoreCase(format)) {
            throw new IllegalStateException("Only 'plain' event format is supported at present");
        }
        // 判断处理channel通道的handle是否经过认证
        InboundChannelHandler handler = getAuthedHandler(addr);

        StringBuilder sb = new StringBuilder();
        sb.append("event ");
        sb.append(format);
        if (events != null && !events.isEmpty()) {
            sb.append(' ');
            sb.append(events);
        }
        EslMessage response = handler.sendSyncSingleLineCommand(sb.toString());
        return new CommandResponse(sb.toString(), response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse cancelEventSubscriptions(String addr) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        EslMessage response = handler.sendSyncSingleLineCommand("noevents");
        return new CommandResponse("noevents", response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse addEventFilter(String addr, String eventHeader, String valueToFilter) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        StringBuilder sb = new StringBuilder();
        if (eventHeader != null && !eventHeader.isEmpty()) {
            sb.append("filter ");
            sb.append(eventHeader);
        }
        if (valueToFilter != null && !valueToFilter.isEmpty()) {
            sb.append(' ');
            sb.append(valueToFilter);
        }
        EslMessage response = handler.sendSyncSingleLineCommand(sb.toString());

        return new CommandResponse(sb.toString(), response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse deleteEventFilter(String addr, String eventHeader, String valueToFilter) {
        InboundChannelHandler handler = getAuthedHandler(addr);

        StringBuilder sb = new StringBuilder();
        if (eventHeader != null && !eventHeader.isEmpty()) {
            sb.append("filter delete ");
            sb.append(eventHeader);
        }
        if (valueToFilter != null && !valueToFilter.isEmpty()) {
            sb.append(' ');
            sb.append(valueToFilter);
        }
        EslMessage response = handler.sendSyncSingleLineCommand(sb.toString());
        return new CommandResponse(sb.toString(), response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse sendEvent(String addr, SendEvent sendEvent) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        EslMessage response = handler.sendSyncMultiLineCommand(sendEvent.getMsgLines());
        return new CommandResponse(sendEvent.toString(), response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse sendMessage(String addr, SendMsg sendMsg) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        EslMessage response = handler.sendSyncMultiLineCommand(sendMsg.getMsgLines());
        return new CommandResponse(sendMsg.toString(), response);
    }

    @Override
    public  CommandResponse execute(String addr, String app, String param, String uuid){
        SendMsg msg = new SendMsg(uuid);
        msg.addCallCommand("execute");
        msg.addExecuteAppName(app);
        msg.addExecuteAppArg(param);
        return  sendMessage(addr, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse setLoggingLevel(String addr, String level) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        StringBuilder sb = new StringBuilder();
        if (level != null && !level.isEmpty()) {
            sb.append("log ");
            sb.append(level);
        }
        EslMessage response = handler.sendSyncSingleLineCommand(sb.toString());
        return new CommandResponse(sb.toString(), response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse cancelLogging(String addr) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        EslMessage response = handler.sendSyncSingleLineCommand("nolog");
        return new CommandResponse("nolog", response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandResponse close(String addr) {
        InboundChannelHandler handler = getAuthedHandler(addr);
        EslMessage response = handler.sendSyncSingleLineCommand("exit");
        return new CommandResponse("exit", response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InboundClient closeChannel(String addr) {
        getAuthedHandler(addr).close();
        return this;
    }
}
