package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.soap.*;
import java.util.Base64;

@Named
public class IsAuthorized {

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

    public String isAuthorizedEndpoint() {
        return environment.getValue("CSO_EXTENSION_ENDPOINT");
    }

    public String isAuthorizedSoapAction() {
        return environment.getValue("CSO_IS_AUTHORIZED_SOAP_ACTION");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    private SOAPMessage getSoapMessage(String userguid, String request) throws SOAPException {
        MessageFactory myMsgFct = MessageFactory.newInstance();
        SOAPMessage message = myMsgFct.createMessage();
        SOAPPart mySPart = message.getSOAPPart();
        SOAPEnvelope myEnvp = mySPart.getEnvelope();
        SOAPBody body = myEnvp.getBody();
        Name bodyName = myEnvp.createName(request, "cso", this.namespace());
        SOAPBodyElement gltp = body.addBodyElement(bodyName);
        Name myContent = myEnvp.createName("userguid");
        SOAPElement mySymbol = gltp.addChildElement(myContent);
        mySymbol.addTextNode(userguid);
        message.saveChanges();

        return message;
    }

    public SOAPMessage byUserguid(String userguid) throws SOAPException {
        return getSoapMessage(userguid, "isAuthorizedUser");
    }
}
