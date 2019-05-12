package hub.helper;

import javax.inject.Named;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Named
public class Stringify {

    public String soapMessage(javax.xml.soap.SOAPMessage message) throws IOException, SOAPException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);

        return new String(out.toByteArray());
    }

    public String inputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) { return ""; }

        return new String(new Bytify().inputStream(inputStream));
    }
}
