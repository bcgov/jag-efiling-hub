package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Named
public class ORInitialize {

    @Inject
    Environment environment;

    public String camelUrl() {
        return environment.getValue("OR_ENDPOINT_INITIALIZE");
    }

    public String url() {
        return environment.getValue("OR_ENDPOINT_INITIALIZE").replace("https4", "https");
    }

    public String application() {
        return environment.getValue("OR_APP_ID");
    }

    public String password() {
        return environment.getValue("OR_APP_PASSWORD");
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

    public String body() {
        return "" +
                "{" +
                "   \"AppId\":\"" + this.application() + "\"," +
                "   \"AppPwd\":\"" + this.password() + "\"," +
                "   \"TicketLifeTime\":\"120\"" +
                "}";
    }

    public Map<String, String> headers() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        headers.put("authorization", this.basicAuthorization());

        return headers;
    }
}
