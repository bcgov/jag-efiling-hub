package hub;

import hub.helper.Environment;
import hub.helper.Stringify;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.TimeZone;
import java.util.logging.Logger;

@Named
public class WebcatsUpdate {

    private static final Logger LOGGER = Logger.getLogger(WebcatsUpdate.class.getName());

    @Inject
    Environment environment;

    @Inject
    Clock clock;

    @Inject
    Stringify stringify;

    public String user() {
        return environment.getValue("WEBCATS_USERNAME");
    }

    public String password() {
        return environment.getValue("WEBCATS_PASSWORD");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    public String endpoint() {
        return environment.getValue("WEBCATS_UPDATE_ENDPOINT");
    }

    public String updateSoapAction() {
        return environment.getValue("WEBCATS_UPDATE_SOAP_ACTION");
    }

    public String update(String caseNumber, String guid) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("templates/webcats-update.xml");
        String template = stringify.inputStream(inputStream);
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String now = dateFormatGmt.format(clock.now());

        return template
                .replace("<dat:CaseNumber>?</dat:CaseNumber>", "<dat:CaseNumber>"+caseNumber+"</dat:CaseNumber>")
                .replace("<dat:DateFiled>?</dat:DateFiled>", "<dat:DateFiled>"+now+"</dat:DateFiled>")
                .replace("<dat:DocumentGUID>?</dat:DocumentGUID>", "<dat:DocumentGUID>"+guid+"</dat:DocumentGUID>")
                ;
    }
}
