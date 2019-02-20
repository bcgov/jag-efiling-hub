package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.soap.*;
import java.util.Base64;
import java.util.logging.Logger;

@Named
public class CsoSearch {

    private static final Logger LOGGER = Logger.getLogger(CsoSearch.class.getName());

    @Inject
    Environment environment;

    public String user() {
        return environment.getValue("COA_USER");
    }

    public String password() {
        return environment.getValue("COA_PASSWORD");
    }

    public String searchEndpoint() {
        return environment.getValue("COA_SEARCH_ENDPOINT");
    }

    public String namespace() {
        return environment.getValue("COA_NAMESPACE");
    }

    public String searchByCaseNumberSoapAction() {
        return environment.getValue("COA_SEARCH_SOAP_ACTION");
    }

    public String viewCasePartySoapAction() {
        return environment.getValue("COA_VIEW_CASE_PARTY_SOAP_ACTION");
    }

    public String viewCaseBasicsSoapAction() {
        return environment.getValue("COA_VIEW_CASE_BASICS_SOAP_ACTION");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    public SOAPMessage searchByCaseNumber(String caseNumber) throws SOAPException {
        MessageFactory myMsgFct = MessageFactory.newInstance();
        SOAPMessage message = myMsgFct.createMessage();
        SOAPPart mySPart = message.getSOAPPart();
        SOAPEnvelope myEnvp = mySPart.getEnvelope();
        SOAPBody body = myEnvp.getBody();
        Name bodyName = myEnvp.createName("SearchByCaseNumber", "any", this.namespace());
        SOAPBodyElement gltp = body.addBodyElement(bodyName);
        Name myContent = myEnvp.createName("strCaseNumber", "any", this.namespace());
        SOAPElement mySymbol = gltp.addChildElement(myContent);
        mySymbol.addTextNode(caseNumber);
        message.saveChanges();

        return message;
    }

    public SOAPMessage viewCaseParty(String caseId) throws SOAPException {
        MessageFactory myMsgFct = MessageFactory.newInstance();
        SOAPMessage message = myMsgFct.createMessage();
        SOAPPart mySPart = message.getSOAPPart();
        SOAPEnvelope myEnvp = mySPart.getEnvelope();
        SOAPBody body = myEnvp.getBody();
        Name bodyName = myEnvp.createName("ViewCaseParty", "any", this.namespace());
        SOAPBodyElement gltp = body.addBodyElement(bodyName);
        Name myContent = myEnvp.createName("intCaseId", "any", this.namespace());
        SOAPElement mySymbol = gltp.addChildElement(myContent);
        mySymbol.addTextNode(caseId);
        message.saveChanges();

        return message;
    }

    public SOAPMessage viewCaseBasics(String caseId) throws SOAPException {
        MessageFactory myMsgFct = MessageFactory.newInstance();
        SOAPMessage message = myMsgFct.createMessage();
        SOAPPart mySPart = message.getSOAPPart();
        SOAPEnvelope myEnvp = mySPart.getEnvelope();
        SOAPBody body = myEnvp.getBody();
        Name bodyName = myEnvp.createName("ViewCaseBasics", "any", this.namespace());
        SOAPBodyElement gltp = body.addBodyElement(bodyName);
        Name myContent = myEnvp.createName("intCaseId", "any", this.namespace());
        SOAPElement mySymbol = gltp.addChildElement(myContent);
        mySymbol.addTextNode(caseId);
        message.saveChanges();

        return message;
    }

    public String extractCaseId(String body) {
        int start = body.indexOf("<CaseId>");
        int end = body.indexOf("</CaseId>");

        return body.substring(start+8, end);
    }
}
