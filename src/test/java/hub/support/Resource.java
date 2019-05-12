package hub.support;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Resource {

    private static final Logger LOGGER = Logger.getLogger(Resource.class.getName());

    public static String bodyOf(String uri) throws Exception {
        URL url = new URL( uri );
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection.getResponseCode() < 400) {
            InputStream inputStream = connection.getInputStream();
            byte[] response = new byte[inputStream.available()];
            inputStream.read(response);

            return new String(response);
        } else {
            LOGGER.log(Level.INFO, connection.getResponseCode() + ":" + connection.getResponseMessage());
            InputStream inputStream = connection.getErrorStream();
            byte[] response = new byte[inputStream.available()];
            inputStream.read(response);

            return new String(response);
        }
    }
}
