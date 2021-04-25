package paddedsocks;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ProxyServerTest {
    String socksHost = "127.0.0.1";
    int socksPort = 1080;

    private int statusCodeOfRequest(String url) throws IOException {
        return SocksGet.socksGet(url, socksHost, socksPort).responseCode;
    }

    @BeforeEach
    void setUp() {
        ProxyServer.runServer();
    }

    @Test
    public void http() throws IOException {
        Assertions.assertEquals(200, statusCodeOfRequest("http://www.baidu.com"));
        Assertions.assertEquals(200, statusCodeOfRequest("http://bugvista.us.oracle.com:7777/pls/apex/f?p=112:1231:1247745961839::NO:::"));
        Assertions.assertEquals(200, statusCodeOfRequest("http://hudsonci.oraclecorp.com/"));
    }

    @Test
    public void https() throws IOException {
        Assertions.assertEquals(200, statusCodeOfRequest("https://www.github.com"));
        Assertions.assertEquals(200, statusCodeOfRequest("https://www.oracle.com"));
        Assertions.assertEquals(200, statusCodeOfRequest("https://bitbucket.oci.oraclecorp.com/dashboard"));
    }
}
