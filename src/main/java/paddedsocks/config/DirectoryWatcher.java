package paddedsocks.config;

import paddedsocks.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirectoryWatcher implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryWatcher.class);
    ProxyServer proxyServer;

    public DirectoryWatcher(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    public void run() {
        try {
            fileChanged();
        } catch (IOException | InterruptedException e) {
            LOG.error("Exception in dir watcher: ", e);
        }
    }

    private void fileChanged() throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        String dirName = System.getProperty("user.dir");
        Path monitoringPath = Paths.get(dirName);
        LOG.info("Monitoring path: " + monitoringPath);

        monitoringPath.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey watchKey;
        while ((watchKey = watchService.take()) != null) {
            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                //if (watchEvent.context().toString().equals("app.properties"))
                LOG.info("Got watch event of kind: " + watchEvent.kind() + " on file: " + watchEvent.context());
                proxyServer.resetSocksConfig();
            }
            watchKey.reset();
        }
    }
}
