package hub.support;

import java.io.IOException;
import java.io.InputStream;

public class StreamReader {

    public static String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) { return ""; }

        byte[] response = new byte[ inputStream.available() ];
        inputStream.read( response );

        return new String(response);
    }

    public static byte[] readStreamAsbytes(InputStream inputStream) throws IOException {
        if (inputStream == null) { return new byte[0]; }

        byte[] response = new byte[ inputStream.available() ];
        inputStream.read( response );

        return response;
    }
}
