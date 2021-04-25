package paddedsocks.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);

    protected static String propertiesFilename = "app.properties";
    protected static String propertiesDirectory;
    protected static Properties allProperties = new Properties();

    public static void loadPropertiesFile() {
        String propertiesPath = getPropertiesDirectory() + getPropertiesFilename();
        loadPropertiesFile(propertiesPath);
    }

    public static void loadPropertiesFile(String propertiesPath) {
        try {
            if (LOG.isTraceEnabled())
                LOG.trace("Properties Path: " + propertiesPath);
            File propertiesFile = new File(propertiesPath);
            // read the file
            if (propertiesFile.isFile()) {
                if (LOG.isTraceEnabled())
                    LOG.trace("Loading properties from: " + propertiesFile.getCanonicalFile());
                try (FileInputStream file = new FileInputStream(propertiesPath)) {
                    // load all the properties from this file
                    allProperties.load(file);
                }
            } else {
                if (LOG.isTraceEnabled())
                    LOG.trace("Missing properties file: " + propertiesFile.getCanonicalFile());
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Exception reading properties file", e);
        }
    }

    public static String getPropertiesFilename() {
        return propertiesFilename;
    }

    public static String getPropertiesDirectory() {
        if (propertiesDirectory == null || propertiesDirectory.isEmpty()) {
            setPropertiesDirectory();
        }
        return propertiesDirectory;
    }

    public static void setPropertiesDirectory() {
        try {
            String separator = System.getProperty("file.separator");
            propertiesDirectory = PropertiesLoader.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            int index = propertiesDirectory.lastIndexOf(separator);
            propertiesDirectory = propertiesDirectory.substring(0, index + 1);
        } catch (Exception e) {
            if (LOG.isWarnEnabled())
                LOG.warn("Exception reading properties file", e);
        }
    }

    public static String getProp(String propName, String defaultValue) {
        return allProperties.getProperty(propName, defaultValue);
    }

    public static int getIntProp(String propName, int defaultValue) {
        try {
            return Integer.parseInt(getProp(propName, String.valueOf(defaultValue)));
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getBoolProp(String propName, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(getProp(propName, String.valueOf(defaultValue)));
        }
        catch (Exception e) {
            return defaultValue;
        }
    }
}
