package paddedsocks.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import paddedsocks.config.SocksConfig;
import paddedsocks.upstream.Upstream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);
    private final EventLoopGroup forwarders;
    private final SocksConfig socksConfig;

    public Socks5CommandRequestHandler(EventLoopGroup forwarders, SocksConfig socksConfig) {
        this.forwarders = forwarders;
        this.socksConfig = socksConfig;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, DefaultSocks5CommandRequest msg) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.remove(Socks5CommandRequestDecoder.class.getName());
        pipeline.remove(this);

        if (LOG.isDebugEnabled()) {
            Channel channel = ctx.channel();
            LOG.debug(String.format("%s %s %s:%d",
                    channel.remoteAddress(),
                    msg.type(),
                    msg.dstAddr(), msg.dstPort()));
        }

        if (msg.type().equals(Socks5CommandType.CONNECT)) {
            handleConnect(ctx, msg);
        } else {
            //TODO: Do we need to handle any other command types?
            ctx.close();
        }
    }

    private void handleConnect(final ChannelHandlerContext client, Socks5CommandRequest msg) {
        Bootstrap bootstrap = new Bootstrap();
        Upstream<SocketChannel> upstream = socksConfig.getUpstream(msg.dstAddr());

        bootstrap.group(forwarders)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        upstream.initChannel(channel);
                    }
                });

        ChannelFuture forwarderConnectFuture = bootstrap.connect(msg.dstAddr(), msg.dstPort());

        forwarderConnectFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("upstream connected");
                }

                ChannelPipeline upstreamPipeline = future.channel().pipeline();
                upstreamPipeline.addLast("from-upstream", new ChannelHandlerContextForwardingHandler(client, false));

                ChannelPipeline clientPipeline = client.pipeline();
                clientPipeline.addLast("to-upstream", new ChannelForwardingHandler(future.channel(), true));

                client.writeAndFlush(socks5CommandResponse(msg, true));
            } else {
                LOG.error("Upstream unavailable due to: " + future.cause().getMessage());
                client.writeAndFlush(socks5CommandResponse(msg, false)).addListener(ChannelFutureListener.CLOSE);
            }
        });
    }

    private Socks5CommandResponse socks5CommandResponse(Socks5CommandRequest request, boolean success) {
        Socks5CommandStatus status = success ? Socks5CommandStatus.SUCCESS : Socks5CommandStatus.FAILURE;
        // bug: Out of service DOMAIN Will cause the message to fail to be sent
        Socks5AddressType addressType = Socks5AddressType.IPv4;
        return new DefaultSocks5CommandResponse(status, addressType);
    }
}
