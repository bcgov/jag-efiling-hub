package hub;

import javax.inject.Named;
import javax.xml.soap.*;
import java.util.Base64;

@Named
public class CsoAccountInfo extends Cso {

    public String accountInfoEndpoint() {
        return environment.getValue("CSO_ACCOUNT_INFO_ENDPOINT");
    }

    public String accountInfoSoapAction() {
        return environment.getValue("CSO_ACCOUNT_INFO_SOAP_ACTION");
    }

    public SOAPMessage searchByAccountId(String accountId) throws SOAPException {
        return getSoapMessage(accountId, "getCsoClientProfiles");
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
}
