package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Base64;

@Named
public class ORInitialize {

    @Inject
    Environment environment;

    public String application() {
        return environment.getValue("OR_APP_ID");
    }

    public String password() {
        return environment.getValue("OR_APP_PASSWORD");
    }

    public String orEndpoint() {
        return environment.getValue("OR_ENDPOINT");
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
}
