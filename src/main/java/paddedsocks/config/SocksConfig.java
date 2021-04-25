package paddedsocks.config;

import paddedsocks.upstream.DirectUpstream;
import paddedsocks.upstream.HttpUpstream;
import paddedsocks.upstream.Upstream;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class SocksConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SocksConfig.class);
    private String listenHost = "0.0.0.0";
    private int listenPort = 1080;
    private int acceptorThreads = 2;
    private int socketBacklog = 128;
    private int connectTimeoutMillis = 3000;
    private int readTimeoutMillis = 30000;
    private int writeTimeoutMillis = 10000;
    private int channelSendBuffer = 1024000;
    private int channelReceiveBuffer = 1024000;
    private boolean channelKeepAlive = true;
    private boolean forceDirect = false;

    // Only one direct upstream is needed which can be reused for all direct connections
    private Upstream<SocketChannel> directUpstream = new DirectUpstream();

    // Cache of destination to upstream - if same dest was used before no need for js lookup or upstream creation
    private ConcurrentHashMap<String, Upstream<SocketChannel>> upstreamDestMap = new ConcurrentHashMap<>();

    // Cache of upstream name (host:port) to upstream object so if a different destination needs same proxy then a new upstream is not created
    private ConcurrentHashMap<String, Upstream<SocketChannel>> nameUpstreamMap = new ConcurrentHashMap<>();

    private JSRunner jsRunner = new JSRunner(new String[]{"wpad.dat", "pac_utils.js"});

    public Upstream<SocketChannel> getFirstProxy(String destAddr)
    {
        String proxyList = jsRunner.exec("FindProxyForURL", "", destAddr);
        String[] proxyStrings = proxyList.split("\\s*;\\s*");
        if (proxyStrings[0].equals("DIRECT"))
            return directUpstream;

        String[] proxyParts = proxyStrings[0].split("\\s");
        if (!proxyParts[0].equals("PROXY"))
            System.err.println("WARNING: Unknown proxy type: " + proxyParts[0]);

        Upstream<SocketChannel> httpUpstream = nameUpstreamMap.get(proxyParts[1]);
        if (httpUpstream == null) {
            String[] hostPort = proxyParts[1].split(":");
            int proxyPort = Integer.parseInt(hostPort[1]);
            if (LOG.isDebugEnabled())
                LOG.debug(String.format("first proxy for host %s is %s:%d", destAddr, hostPort[0], proxyPort));
            httpUpstream = new HttpUpstream(hostPort[0], proxyPort);
            nameUpstreamMap.put(proxyParts[1], httpUpstream);
        }
        return httpUpstream;
    }

    public Upstream<SocketChannel> getUpstream(String destAddr) {
        if (forceDirect)
            return directUpstream;
        Upstream<SocketChannel> upstream = upstreamDestMap.get(destAddr);
        if (upstream == null) {
            upstream = getFirstProxy (destAddr);
            upstreamDestMap.put(destAddr, upstream);
        }
        return upstream;
    }

    public String getListenHost() {
        return listenHost;
    }

    public int getListenPort() {
        return listenPort;
    }

    public int getAcceptorThreads() {
        return acceptorThreads;
    }

    public int getSocketBacklog() {
        return socketBacklog;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public int getWriteTimeoutMillis() {
        return writeTimeoutMillis;
    }

    public int getChannelSendBuffer() {
        return channelSendBuffer;
    }

    public int getChannelReceiveBuffer() {
        return channelReceiveBuffer;
    }

    public boolean getChannelKeepAlive() {
        return channelKeepAlive;
    }

    public void setAcceptorThreads(int acceptorThreads) {
        this.acceptorThreads = acceptorThreads;
    }

    public void setSocketBacklog(int socketBacklog) {
        this.socketBacklog = socketBacklog;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public void setWriteTimeoutMillis(int writeTimeoutMillis) {
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public void setListenHost(String listenHost) {
        this.listenHost = listenHost;
    }

    public void setForceDirect(boolean forceDirect) {
        this.forceDirect = forceDirect;
    }

    public void setChannelSendBuffer(int channelSendBuffer) {
        this.channelSendBuffer = channelSendBuffer;
    }

    public void setChannelReceiveBuffer(int channelReceiveBuffer) {
        this.channelReceiveBuffer = channelReceiveBuffer;
    }

    public void setChannelKeepAlive(boolean channelKeepAlive) {
        this.channelKeepAlive = channelKeepAlive;
    }

    public void setJsRunner(String[] jsFiles) {
        this.jsRunner = new JSRunner(jsFiles);
    }
}
