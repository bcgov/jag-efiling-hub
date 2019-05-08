package hub.helper;

import javax.inject.Named;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Named
public class Bytify {

    public byte[] inputStram(InputStream inputStream) throws IOException {
        if (inputStream == null) { return new byte[0]; }

        byte[] response = new byte[ inputStream.available() ];
        inputStream.read( response );

        return response;
    }
}
