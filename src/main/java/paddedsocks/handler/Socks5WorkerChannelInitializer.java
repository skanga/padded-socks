package paddedsocks.handler;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import paddedsocks.config.SocksConfig;

import java.util.concurrent.TimeUnit;

public class Socks5WorkerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final SocksConfig socksConfig;
    private Socks5InitialRequestHandler socks5InitialRequestHandler;
    private Socks5CommandRequestHandler socks5CommandRequestHandler;

    public Socks5WorkerChannelInitializer(SocksConfig socksConfig, EventLoopGroup forwarders) {
        this.socksConfig = socksConfig;

        // Initialize shared handlers
        socks5InitialRequestHandler = new Socks5InitialRequestHandler();
        socks5CommandRequestHandler = new Socks5CommandRequestHandler(forwarders, socksConfig);
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();

        // Connection management
        pipeline.addLast(ConnectionManageHandler.NAME, new ConnectionManageHandler(3000));

        // Idle timeout
        pipeline.addLast(new IdleStateHandler(10, 10, 0));
        pipeline.addLast(new IdleStateEventHandler());
        // Read and write timeout
        pipeline.addLast(new ReadTimeoutHandler(socksConfig.getReadTimeoutMillis(), TimeUnit.MILLISECONDS));
        pipeline.addLast(new WriteTimeoutHandler(socksConfig.getWriteTimeoutMillis(), TimeUnit.MILLISECONDS));

        // netty log
        //pipeline.addLast(new LoggingHandler());

        // Convert the Socks5Message into a ByteBuf
        pipeline.addLast(Socks5ServerEncoder.DEFAULT);

        // init
        pipeline.addLast(Socks5InitialRequestDecoder.class.getName(), new Socks5InitialRequestDecoder());
        pipeline.addLast(Socks5InitialRequestHandler.class.getName(), socks5InitialRequestHandler);

        // connection
        pipeline.addLast(Socks5CommandRequestDecoder.class.getName(), new Socks5CommandRequestDecoder());
        pipeline.addLast(Socks5CommandRequestHandler.class.getName(), socks5CommandRequestHandler);
    }
}
