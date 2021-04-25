package paddedsocks.upstream;

import io.netty.channel.socket.SocketChannel;

public class DirectUpstream extends Upstream<SocketChannel> {

    public DirectUpstream() {
    }

    @Override
    public void initChannel(SocketChannel channel) {
    }
}
