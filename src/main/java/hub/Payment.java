package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.soap.*;
import java.util.Base64;

@Named
public class Payment {

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

    public String paymentEndpoint() {
        return environment.getValue("CSO_EXTENSION_ENDPOINT");
    }

    public String paymentSoapAction() {
        return environment.getValue("CSO_PAYMENT_PROCESS_SOAP_ACTION");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    public SOAPMessage message(String userguid) throws SOAPException {
        return getSoapMessage(userguid);
    }

    private SOAPMessage getSoapMessage(String userguid) throws SOAPException {
        MessageFactory myMsgFct = MessageFactory.newInstance();
        SOAPMessage message = myMsgFct.createMessage();
        SOAPPart mySPart = message.getSOAPPart();
        SOAPEnvelope myEnvp = mySPart.getEnvelope();
        SOAPBody body = myEnvp.getBody();
        Name bodyName = myEnvp.createName("paymentProcess", "cso", this.namespace());
        SOAPBodyElement gltp = body.addBodyElement(bodyName);

        addChild("serviceType", "EXFL", gltp, myEnvp);
        addChild("serviceDesc", "Form 2 Filing payment", gltp, myEnvp);
        addChild("userguid", userguid, gltp, myEnvp);
        addChild("bcolUserId", "", gltp, myEnvp);
        addChild("bcolSessionKey", "", gltp, myEnvp);
        addChild("bcolUniqueId", "", gltp, myEnvp);

        message.saveChanges();

        return message;
    }

    private void addChild(String tagName, String tagValue, SOAPBodyElement parent, SOAPEnvelope envelope) throws SOAPException {
        Name myContent = envelope.createName(tagName);
        SOAPElement mySymbol = parent.addChildElement(myContent);
        mySymbol.addTextNode(tagValue);
    }

    public String extractErrorMessage(String body) {
        String tag = "resultMessage";
        int start = body.indexOf("<"+tag+">");
        int end = body.indexOf("</"+tag+">");

        return body.substring(start+tag.length()+2, end);
    }
}
