package paddedsocks.upstream;

import io.netty.channel.Channel;

public abstract class Upstream<T extends Channel> {
    public static final String HANDLER_NAME = "proxy";
    private String upstreamHost;
    private int upstreamPort;

    public String getUpstreamHost() {
        return upstreamHost;
    }

    public void setUpstreamHost(String upstreamHost) {
        this.upstreamHost = upstreamHost;
    }

    public int getUpstreamPort() {
        return upstreamPort;
    }

    public void setUpstreamPort(int upstreamPort) {
        this.upstreamPort = upstreamPort;
    }

    public abstract void initChannel(T channel);
}
