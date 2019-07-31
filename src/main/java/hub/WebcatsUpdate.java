package hub;

import hub.helper.Environment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.soap.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.logging.Level;

@Named
public class WebcatsUpdate {

    private static final Logger LOGGER = Logger.getLogger(WebcatsUpdate.class.getName());

    @Inject
    Environment environment;

    public String user() {
        return environment.getValue("WEBCATS_USERNAME");
    }

    public String password() {
        return environment.getValue("WEBCATS_PASSWORD");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    public String endpoint() {
        return environment.getValue("WEBCATS_UPDATE_ENDPOINT");
    }

    public String updateSoapAction() {
        return environment.getValue("WEBCATS_UPDATE_SOAP_ACTION");
    }

    public String nsNamespace() {
        return environment.getValue("WEBCATS_XMLNS_NS");
    }

    public String datNamespace() {
        return environment.getValue("WEBCATS_XMLNS_DAT");
    }

    public SOAPMessage update(String caseNumber, String guid) throws SOAPException {
        MessageFactory myMsgFct = MessageFactory.newInstance();
        SOAPMessage message = myMsgFct.createMessage();
        SOAPPart mySPart = message.getSOAPPart();
        SOAPEnvelope myEnvp = mySPart.getEnvelope();
        SOAPBody body = myEnvp.getBody();

        myEnvp.addNamespaceDeclaration("ns", this.nsNamespace());
        myEnvp.addNamespaceDeclaration("dat", this.datNamespace());

        Name updateWebcats = myEnvp.createName("UpdateWebCATS", "ns", this.nsNamespace());
        SOAPElement updateWebcatsElement = body.addChildElement(updateWebcats);

        Name updateRequest = myEnvp.createName("updateRequest", "ns", this.nsNamespace());
        SOAPElement updateRequestElement = updateWebcatsElement.addChildElement(updateRequest);

        Name caseNumberName = myEnvp.createName("CaseNumber", "dat", this.datNamespace());
        SOAPElement caseNumberElement = updateRequestElement.addChildElement(caseNumberName);
        caseNumberElement.addTextNode(caseNumber);

        Name documents = myEnvp.createName("Documents", "dat", this.datNamespace());
        SOAPElement documentsElement = updateRequestElement.addChildElement(documents);
        Name document = myEnvp.createName("Document", "dat", this.datNamespace());
        SOAPElement documentElement = documentsElement.addChildElement(document);

        Name dateFiled = myEnvp.createName("DateFiled", "dat", this.datNamespace());
        SOAPElement dateFiledElement = documentElement.addChildElement(dateFiled);
        dateFiledElement.addTextNode("now");

        Name documentGuid = myEnvp.createName("DocumentGUID", "dat", this.datNamespace());
        SOAPElement documentGuidElement = documentElement.addChildElement(documentGuid);
        documentGuidElement.addTextNode(guid);

        Name documentName = myEnvp.createName("DocumentName", "dat", this.datNamespace());
        SOAPElement documentNameElement = documentElement.addChildElement(documentName);
        documentNameElement.addTextNode("Notice of Appearance");

        Name documentTypeCode = myEnvp.createName("DocumentTypeCode", "dat", this.datNamespace());
        SOAPElement documentTypeCodeElement = documentElement.addChildElement(documentTypeCode);
        documentTypeCodeElement.addTextNode("APP");

        Name documentDescription = myEnvp.createName("DocumentTypeDescription", "dat", this.datNamespace());
        SOAPElement documentDescriptionElement = documentElement.addChildElement(documentDescription);
        documentDescriptionElement.addTextNode("Appearance");

        Name initiatingDocument = myEnvp.createName("InitiatingDocument", "dat", this.datNamespace());
        SOAPElement initiatingDocumentElement = documentElement.addChildElement(initiatingDocument);
        initiatingDocumentElement.addTextNode("N");

        message.saveChanges();
        return message;
    }
}
