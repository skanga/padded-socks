package paddedsocks;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class SocksGet {
    public static void main(String... args) throws IOException {
        if (args.length != 4)
        {
            System.out.println("USAGE: SocksGet <get-url> <socks-host> <socks-port> <file-name>");
            return;
        }
        byte[] fileBytes = socksGet(args[0], args[1], Integer.parseInt(args[2])).responseBody;
        toFile(fileBytes, args[3]);
    }

    public static void test() throws IOException {
        String socksHost = "localhost";
        final int socksPort = 1080;

        System.out.println("HTTP -> \n" + toString (socksGet("http://www.baidu.com", socksHost, socksPort).responseBody));
        System.out.println("HTTPS -> \n" + toString (socksGet("https://www.oracle.com", socksHost, socksPort).responseBody));
    }

    public static void toFile (byte[] inputBytes, String fileName) throws IOException {
        Path path = Paths.get(fileName);
        Files.write(path, inputBytes);
    }

    public static String toString (byte[] inputBytes)
    {
        return new String(inputBytes, StandardCharsets.UTF_8);
    }

    public static HttpResponse socksGet(String urlString, String socksHost, int socksPort) throws IOException {
        // We can also use Proxy.Type.HTTP for HTTP proxies but for now SOCKS is enough
        Proxy socksProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksHost, socksPort));

        URL httpUrl = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) httpUrl.openConnection(socksProxy);
        urlConnection.setRequestMethod("GET");
        HttpResponse httpResponse = new HttpResponse();
        int status = urlConnection.getResponseCode();
        httpResponse.responseCode = status;
        InputStream inputStream;

        if (status > 299) {
            inputStream = urlConnection.getErrorStream();
        } else {
            inputStream = urlConnection.getInputStream();
        }

        for (int i = 0;; i++) {
            String headerName = urlConnection.getHeaderFieldKey(i);
            String headerValue = urlConnection.getHeaderField(i);
            httpResponse.responseHeaders.put(headerName, headerValue);
            if (headerName == null && headerValue == null)
                break;
        }
        httpResponse.responseBody = readInputStream (inputStream);
        inputStream.close();
        urlConnection.disconnect();
        return httpResponse;
    }

    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        BufferedInputStream inStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        for (int nextByte = inStream.read(); nextByte != -1; nextByte = inStream.read()) {
            outStream.write((byte) nextByte);
        }
        return outStream.toByteArray();
    }

    static class HttpResponse {
        HashMap<String, String> responseHeaders = new HashMap<>();
        int responseCode;
        byte[] responseBody;
    }
}
