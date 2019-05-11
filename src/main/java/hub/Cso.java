package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import java.util.Base64;

public class Cso {

    @Inject
    Environment environment;

    public String user() {
        return environment.getValue("CSO_USER");
    }

    public String password() {
        return environment.getValue("CSO_PASSWORD");
    }

    public String namespace() {
        return environment.getValue("CSO_NAMESPACE");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }


}
