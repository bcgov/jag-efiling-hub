package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Named
public class ORSave {

    @Inject
    Environment environment;

    public String camelUrl() {
        return environment.getValue("OR_ENDPOINT_CREATE");
    }

    public String url(String ticket) {
        String value = environment.getValue("OR_ENDPOINT_CREATE").replace("https4", "https");
        value += "?AppTicket=this-ticket&MimeType=application&MimeSubType=pdf&Filename=form2.pdf&RetentionPeriod=-1";
        value = value.replace("this-ticket", ticket);

        return value;
    }

    public String basicAuthUsername() {
        return environment.getValue("OR_BASIC_AUTH_USERNAME");
    }

    public String basicAuthPassword() {
        return environment.getValue("OR_BASIC_AUTH_PASSWORD");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.basicAuthUsername() + ":" + this.basicAuthPassword()).getBytes());

    }

    public Map<String, String> headers() {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", this.basicAuthorization());

        return headers;
    }
}
