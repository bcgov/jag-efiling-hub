package hub.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamReader {

    public static String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) { return ""; }

        return new String(readStreamAsbytes(inputStream));
    }

    public static byte[] readStreamAsbytes(InputStream inputStream) throws IOException {
        if (inputStream == null) { return new byte[0]; }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
