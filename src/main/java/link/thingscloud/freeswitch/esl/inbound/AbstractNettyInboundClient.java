

package link.thingscloud.freeswitch.esl.inbound;

import link.thingscloud.freeswitch.esl.InboundClientService;
import link.thingscloud.freeswitch.esl.inbound.handler.InboundChannelHandler;
import link.thingscloud.freeswitch.esl.inbound.listener.ChannelEventListener;
import link.thingscloud.freeswitch.esl.inbound.option.InboundClientOption;
import link.thingscloud.freeswitch.esl.transport.message.EslFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author : <a href="mailto:ant.zhou@aliyun.com">zhouhailin</a>
 */
abstract class AbstractNettyInboundClient implements ChannelEventListener, InboundClientService {

    final Bootstrap bootstrap;
    final EventLoopGroup workerGroup;
    final ExecutorService publicExecutor;

    final InboundClientOption option;

    final Logger log = LoggerFactory.getLogger(getClass());


    /**
     *  利用netty的bootstrap的功能建立线程池+channel+handle<处理channel>
     */
    AbstractNettyInboundClient(InboundClientOption option) {
        this.option = option;

        bootstrap = new Bootstrap();

        publicExecutor = new ScheduledThreadPoolExecutor(option.publicExecutorThread(),
                new BasicThreadFactory.Builder().namingPattern("publicExecutor-%d").daemon(true).build());

        workerGroup = new NioEventLoopGroup(option.workerGroupThread());
        // 创建连接线程池处理tcp连接的，netty进行了升级<>
        bootstrap.group(workerGroup)
                // 不指定channelFactory,就需要指定channel,channel初始化时会指定默认的channelFactory
                .channel(NioSocketChannel.class)
                // TCP_NODELAY == true 表示关闭Nagle算法，实时发送数据，
                // 若为false减少网络交互，尽可能的利用网络带宽
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                // 发送缓冲区 65535
                .option(ChannelOption.SO_SNDBUF, option.sndBufSize())
                // 接受缓冲区 65535
                .option(ChannelOption.SO_RCVBUF, option.rcvBufSize())
                //指定ChannelHandler去处理上述的channel
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("encoder", new StringEncoder());
                        pipeline.addLast("decoder", new EslFrameDecoder(8192));
                        if (option.readerIdleTimeSeconds() > 0 && option.readTimeoutSeconds() > 0
                                && option.readerIdleTimeSeconds() < option.readTimeoutSeconds()) {
                            pipeline.addLast("idleState", new IdleStateHandler(option.readerIdleTimeSeconds(), 0, 0));
                            pipeline.addLast("readTimeout", new ReadTimeoutHandler(option.readTimeoutSeconds()));
                        }
                        // 事件处理机制  InboundChannelHandler 业务处理handle
                        pipeline.addLast("clientHandler", new InboundChannelHandler(AbstractNettyInboundClient.this, publicExecutor, option.disablePublicExecutor()));
                    }
                });
    }

}
