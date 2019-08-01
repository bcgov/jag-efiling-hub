package hub;

import hub.helper.Environment;
import hub.helper.Stringify;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.TimeZone;
import java.util.logging.Logger;

@Named
public class CsoSaveFiling {

    private static final Logger LOGGER = Logger.getLogger(CsoSaveFiling.class.getName());

    @Inject
    Environment environment;

    @Inject
    Stringify stringify;

    public String user() {
        return environment.getValue("CSO_USER");
    }

    public String password() {
        return environment.getValue("CSO_PASSWORD");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    public String endpoint() {
        return environment.getValue("CSO_EXTENSION_ENDPOINT");
    }

    public String soapAction() {
        return environment.getValue("CSO_SAVE_FILING_SOAP_ACTION");
    }

    public String message(String userguid, String invoiceNumber, String data) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("templates/cso-save-filing.xml");
        String template = stringify.inputStream(inputStream);

        JSONObject jo = new JSONObject(data);
        String courtFileNumber = (String) jo.get(("formSevenNumber"));

        return template
                .replace("<userguid>?</userguid>", "<userguid>"+userguid+"</userguid>")
                .replace("<courtFileNumber>?</courtFileNumber>", "<courtFileNumber>"+courtFileNumber+"</courtFileNumber>")
                .replace("<invoiceNo>?</invoiceNo>", "<invoiceNo>"+invoiceNumber+"</invoiceNo>")
                ;
    }
}
