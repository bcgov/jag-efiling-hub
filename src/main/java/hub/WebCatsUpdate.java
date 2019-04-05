package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.soap.*;
import java.util.Base64;

@Named
public class WebCatsUpdate {

    @Inject
    Environment environment;

    public String user() {
        return environment.getValue("WEBCATS_USER");
    }

    public String password() {
        return environment.getValue("WEBCATS_PASSWORD");
    }

    public String webcatsEndpoint() {
        return environment.getValue("WEBCATS_ENDPOINT");
    }

    public String webcatsApiNamespace() {
        return environment.getValue("WEBCATS_API_NAMESPACE");
    }

    public String webcatsDataNamespace() {
        return environment.getValue("WEBCATS_DATA_NAMESPACE");
    }

    public String updateCaseSoapAction() {
        return environment.getValue("WEBCATS_UPDATE_CASE_SOAP_ACTION");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    public SOAPMessage updateCase(String caseNumber) throws SOAPException {
        return getSoapMessage(caseNumber);
    }

    private SOAPMessage getSoapMessage(String caseNumber) throws SOAPException {
        MessageFactory myMsgFct = MessageFactory.newInstance();
        SOAPMessage message = myMsgFct.createMessage();
        SOAPPart mySPart = message.getSOAPPart();
        SOAPEnvelope myEnvp = mySPart.getEnvelope();
        SOAPBody body = myEnvp.getBody();

        Name updateWebCatsElementName = myEnvp.createName("UpdateWebCATS", "api", this.webcatsApiNamespace());
        SOAPBodyElement updateWebCatsElement = body.addBodyElement(updateWebCatsElementName);

        Name updateRequestElementName = myEnvp.createName("updateRequest", "api", this.webcatsApiNamespace());
        SOAPElement updateRequestElement = updateWebCatsElement.addChildElement(updateRequestElementName);

        Name caseNumberElementName = myEnvp.createName("CaseNumber", "data", this.webcatsDataNamespace());
        SOAPElement mySymbol = updateRequestElement.addChildElement(caseNumberElementName);
        mySymbol.addTextNode(caseNumber);
        message.saveChanges();

        return message;
    }

}
