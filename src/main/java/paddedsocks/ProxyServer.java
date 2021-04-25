package paddedsocks;

import paddedsocks.config.DirectoryWatcher;
import paddedsocks.config.PropertiesLoader;
import paddedsocks.config.SocksConfig;
import paddedsocks.handler.Socks5WorkerChannelInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {
    private static final Logger LOG = LoggerFactory.getLogger(ProxyServer.class);
    private SocksConfig socksConfig;
    static String propertiesPath = "app.properties";

    public ProxyServer(final SocksConfig socksConfig) {
        this.socksConfig = socksConfig;
    }

    public void start() throws InterruptedException {
        EventLoopGroup acceptorGroup = new NioEventLoopGroup(socksConfig.getAcceptorThreads());
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup forwarderGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(acceptorGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, socksConfig.getSocketBacklog())
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, socksConfig.getConnectTimeoutMillis())
                    .childHandler(new Socks5WorkerChannelInitializer(socksConfig, forwarderGroup))
                    .childOption(ChannelOption.SO_KEEPALIVE, socksConfig.getChannelKeepAlive())
                    .childOption(ChannelOption.SO_SNDBUF, socksConfig.getChannelSendBuffer())
                    .childOption(ChannelOption.SO_RCVBUF, socksConfig.getChannelReceiveBuffer());

            ChannelFuture channelFuture = serverBootstrap.bind(socksConfig.getListenHost(), socksConfig.getListenPort()).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            forwarderGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            acceptorGroup.shutdownGracefully();
        }
    }

    static void runServer() {
        new Thread(() -> {
            try {
                SocksConfig socksConfig = getSocksConfig();
                ProxyServer proxyServer = new ProxyServer(socksConfig);
                DirectoryWatcher directoryWatcher = new DirectoryWatcher(proxyServer);
                new Thread(directoryWatcher).start();
                proxyServer.start();
            } catch (InterruptedException e) {
                LOG.info("Interrupted exception during server configuration and start", e);
            }
        }).start();
    }

    public void resetSocksConfig() {
        this.socksConfig = getSocksConfig();
        LOG.info("Resetting Socks Config");
    }

    private static SocksConfig getSocksConfig() {
        SocksConfig socksConfig = new SocksConfig();
        PropertiesLoader.loadPropertiesFile(propertiesPath);
        String listenHost = PropertiesLoader.getProp("listen.host", "127.0.0.1");
        int listenPort = PropertiesLoader.getIntProp("listen.port", 1080);
        socksConfig.setListenHost(listenHost);
        socksConfig.setListenPort(listenPort);
        socksConfig.setAcceptorThreads(PropertiesLoader.getIntProp("acceptor.threads", 2));
        socksConfig.setSocketBacklog(PropertiesLoader.getIntProp("socket.backlog", 128));
        socksConfig.setConnectTimeoutMillis(PropertiesLoader.getIntProp("connect.timeout", 3000));
        socksConfig.setReadTimeoutMillis(PropertiesLoader.getIntProp("read.timeout", 30000));
        socksConfig.setWriteTimeoutMillis(PropertiesLoader.getIntProp("write.timeout", 10000));
        String[] jsArray = PropertiesLoader.getProp("javascript.files","wpad.dat, pac_utils.js")
                .split("\\s*,\\s*");
        socksConfig.setJsRunner(jsArray);
        socksConfig.setForceDirect(PropertiesLoader.getBoolProp("force.direct", false));
        socksConfig.setChannelKeepAlive(PropertiesLoader.getBoolProp("channel.keep.alive", true));
        socksConfig.setChannelSendBuffer(PropertiesLoader.getIntProp("channel.send.buffer", 1024000));
        socksConfig.setChannelReceiveBuffer(PropertiesLoader.getIntProp("channel.receive.buffer", 1024000));
        return socksConfig;
    }

    public static void main(String[] args) {
        if (args.length > 0)
            propertiesPath = args[0];
        runServer();
    }
}
