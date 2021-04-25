package paddedsocks.handler;

import io.netty.handler.traffic.ChannelTrafficShapingHandler;

public class ConnectionManageHandler extends ChannelTrafficShapingHandler {
    public static final String NAME = ConnectionManageHandler.class.getName();

    public ConnectionManageHandler(long checkInterval) {
        super(checkInterval);
    }
}
