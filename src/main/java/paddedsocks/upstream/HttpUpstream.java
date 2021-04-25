package paddedsocks.upstream;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.proxy.HttpProxyHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class HttpUpstream extends Upstream<SocketChannel> {

    public HttpUpstream(final String upstreamHost, final int upstreamPort) {
        setUpstreamHost(upstreamHost);
        setUpstreamPort(upstreamPort);
    }

    @Override
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        SocketAddress address = new InetSocketAddress(getUpstreamHost(), getUpstreamPort());
        pipeline.addFirst(HANDLER_NAME, new HttpProxyHandler(address));
    }
}
