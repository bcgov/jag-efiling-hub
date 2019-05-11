package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.soap.*;
import java.util.Base64;

@Named
public class CsoAccountInfo {

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

    public String accountInfoEndpoint() {
        return environment.getValue("CSO_ACCOUNT_INFO_ENDPOINT");
    }

    public String accountInfoSoapAction() {
        return environment.getValue("CSO_ACCOUNT_INFO_SOAP_ACTION");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    private SOAPMessage getSoapMessage(String accountId, String request) throws SOAPException {
        MessageFactory myMsgFct = MessageFactory.newInstance();
        SOAPMessage message = myMsgFct.createMessage();
        SOAPPart mySPart = message.getSOAPPart();
        SOAPEnvelope myEnvp = mySPart.getEnvelope();
        SOAPBody body = myEnvp.getBody();
        Name bodyName = myEnvp.createName(request, "cso", this.namespace());
        SOAPBodyElement gltp = body.addBodyElement(bodyName);
        Name myContent = myEnvp.createName("accountId");
        SOAPElement mySymbol = gltp.addChildElement(myContent);
        mySymbol.addTextNode(accountId);
        message.saveChanges();

        return message;
    }

    public SOAPMessage searchByAccountId(String accountId) throws SOAPException {
        return getSoapMessage(accountId, "getCsoClientProfiles");
    }
}
