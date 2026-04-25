package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GoogleConfig {
    private static final Properties properties = new Properties();
    private static final Logger log = LoggerFactory.getLogger(GoogleConfig.class);

    static {
        try (InputStream input = GoogleConfig.class.getClassLoader().getResourceAsStream("google-auth.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Warning: Unable to find google-auth.properties in classpath");
            }
        } catch (IOException ex) {
            log.error("Error loading google-auth.properties", ex);
        }
    }

    public static String getClientId() {
        return properties.getProperty("GOOGLE_CLIENT_ID", "");
    }

    public static String getClientSecret() {
        return properties.getProperty("GOOGLE_CLIENT_SECRET", "");
    }
}
