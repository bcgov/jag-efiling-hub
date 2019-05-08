package hub.helper;

import javax.inject.Named;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
public class Environment {

    private static final Logger LOGGER = Logger.getLogger(Environment.class.getName());

    public String getValue(String key) {
        LOGGER.log(Level.INFO, "env.{0}={1}", new Object[] {key, System.getenv(key)});
        LOGGER.log(Level.INFO, "properties.{0}={1}", new Object[] {key, System.getProperty(key)});

        String value = System.getProperty(key);
        return value != null ? value : System.getenv(key);
    }
}
