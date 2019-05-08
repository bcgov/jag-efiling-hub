package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class FormPdfPreview {

    @Inject
    Environment environment;

    public String endpoint() {
        return environment.getValue("ADOBE_ENDPOINT");
    }

}
