package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;

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

}
