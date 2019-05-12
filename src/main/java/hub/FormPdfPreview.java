package hub;

import com.adobe.idp.Document;
import com.adobe.idp.dsc.clientsdk.ServiceClientFactory;
import com.adobe.idp.dsc.clientsdk.ServiceClientFactoryProperties;
import com.adobe.livecycle.formsservice.client.*;
import hub.helper.Bytify;
import hub.helper.Environment;
import org.apache.commons.codec.binary.Base64;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
public class FormPdfPreview {

    private static final Logger LOGGER = Logger.getLogger(FormPdfPreview.class.getName());

    @Inject
    Environment environment;

    @Inject
    Bytify bytify;

    public byte[] renderPdf(String xdpName, byte[] xmlData, boolean encode) throws IOException {

        byte[] ret = null;

        try {
            LOGGER.log(Level.INFO, "Invoke new Document(xmlData)");
            Document document = new Document(xmlData);
            LOGGER.log(Level.INFO, "Invoke getPdfFormRenderSpec");
            PDFFormRenderSpec pdfFormRenderSpec = this.getPdfFormRenderSpec();
            LOGGER.log(Level.INFO, "Invoke getURLSpec");
            URLSpec urlSpec = this.getURLSpec();

            // Invoke the renderPDFForm method to render an interactive PDF form on the client
            LOGGER.log(Level.INFO, "Invoke createFormsServiceClient");
            FormsServiceClient formsClient = this.createFormsServiceClient();

            LOGGER.log(Level.INFO, "Invoke formsClient.renderPDFForm");
            FormsResult formOut = formsClient.renderPDFForm(xdpName, document, pdfFormRenderSpec, urlSpec, null);

            LOGGER.log(Level.INFO, "Got IOutputContext..." + (formOut == null));

            // Create a byte array. Call the IOutputContext interface's getOutputContext method
            Document doc = formOut.getOutputContent();
            byte[] cContent = bytify.inputStream(doc.getInputStream());
            LOGGER.log(Level.INFO, "Received content...");

            /* encode the array?? */
            if (encode) {
                ret = Base64.encodeBase64(cContent);
            } else {
                ret = cContent;
            }
        } catch (Exception ioEx) {

            ioEx.printStackTrace();
            LOGGER.log(Level.SEVERE, ioEx.getMessage());
            return null;
        }
        return ret;
    }

    public String endpoint() {
        return environment.getValue("ADOBE_ENDPOINT");
    }

    public String templateName() {
        return environment.getValue("ADOBE_TEMPLATE_NAME");
    }
    public String username() {
        return environment.getValue("ADOBE_USERNAME");
    }
    public String password() {
        return environment.getValue("ADOBE_PASSWORD");
    }
    public String serverContext() {
        return environment.getValue("ADOBE_SERVER_CONTEXT");
    }
    public String options() {
        return environment.getValue("ADOBE_OPTIONS");
    }

    public byte[] sampleData() throws URISyntaxException, IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<form:CEISForm xmlns:form=\"http://courts.gov.bc.ca/XMLSchema/eforms/forms/1.0\">\n" +
                "\t<form:courtOfAppealFileNo>4801:9864-1</form:courtOfAppealFileNo>\n" +
                "\t<form:supremeCourtFileNo>9923:549-1</form:supremeCourtFileNo>\n" +
                "\t<form:supremeCourtRegistry>Vancouver Supreme Court</form:supremeCourtRegistry>\n" +
                "\t<form:party1Names>\n" +
                "\t\t<form:participants>\n" +
                "\t\t\t<form:name>Jane Smith and Mike Smith</form:name>\n" +
                "\t\t\t<form:role>Plaintiff</form:role>\n" +
                "\t\t</form:participants>\n" +
                "\t</form:party1Names>\n" +
                "\t<form:party2Names>\n" +
                "\t\t<form:participants>\n" +
                "\t\t\t<form:name>Don Simpson</form:name>\n" +
                "\t\t\t<form:role>Defendants</form:role>\n" +
                "\t\t</form:participants>\n" +
                "\t</form:party2Names>\n" +
                "\t<form:party3Names>\n" +
                "\t\t<form:participants>\n" +
                "\t\t\t<form:name/>\n" +
                "\t\t\t<form:role/>\n" +
                "\t\t</form:participants>\n" +
                "\t</form:party3Names>\n" +
                "\t<form:partyAppealingOrder>Jane Smith</form:partyAppealingOrder>\n" +
                "\t<form:orderJudgeName>J Butler</form:orderJudgeName>\n" +
                "\t<form:courtOrOrganizationType>the Supreme Court of British Columbia</form:courtOrOrganizationType>\n" +
                "\t<form:courtDate>February 29, 2016</form:courtDate>\n" +
                "\t<form:courtLocation>Vancouver</form:courtLocation>\n" +
                "\t<form:additionalAppearanceText>Some other appearance details goes here.</form:additionalAppearanceText>\n" +
                "\t<form:trialJudgment>0</form:trialJudgment>\n" +
                "\t<form:orderOfAStatutoryBody>1</form:orderOfAStatutoryBody>\n" +
                "\t<form:summaryTrialJudgment>0</form:summaryTrialJudgment>\n" +
                "\t<form:chambersJudgment>0</form:chambersJudgment>\n" +
                "\t<form:nameOfMaker>Sara Thompson</form:nameOfMaker>\n" +
                "\t<form:constitutionalAdministrative>1</form:constitutionalAdministrative>\n" +
                "\t<form:civilProcedure>0</form:civilProcedure>\n" +
                "\t<form:torts>1</form:torts>\n" +
                "\t<form:commercial>0</form:commercial>\n" +
                "\t<form:motorVehicleAccidents>0</form:motorVehicleAccidents>\n" +
                "\t<form:municipalLaw>1</form:municipalLaw>\n" +
                "\t<form:equity>0</form:equity>\n" +
                "\t<form:realProperty>0</form:realProperty>\n" +
                "\t<form:willsAndEstates>0</form:willsAndEstates>\n" +
                "\t<form:divorce>0</form:divorce>\n" +
                "\t<form:corollaryReliefInADivorceProceeding>0</form:corollaryReliefInADivorceProceeding>\n" +
                "\t<form:familyLawAct>1</form:familyLawAct>\n" +
                "\t<form:otherFamily>0</form:otherFamily>\n" +
                "\t<form:orderDetails>Here are some order details.</form:orderDetails>\n" +
                "\t<form:timePeriodDaysHours>2 Days, 3 hours and 45 minutes</form:timePeriodDaysHours>\n" +
                "\t<form:datedAtLocation>Vancouver</form:datedAtLocation>\n" +
                "\t<form:dateSigned>February 29, 2016</form:dateSigned>\n" +
                "\t<form:opposingPartyName>Don Simpson</form:opposingPartyName>\n" +
                "\t<form:opposingPartyLawyer>T Parsons et al</form:opposingPartyLawyer>\n" +
                "\t<form:appelantsName>Jane Smith</form:appelantsName>\n" +
                "\t<form:appelantsServiceAddr>123 First Ave, Vancouver BC</form:appelantsServiceAddr>\n" +
                "</form:CEISForm>";

        return xml.getBytes();
    }

    public FormsServiceClient createFormsServiceClient(){

        FormsServiceClient formsClient = null;

        try {
            // Set connection properties required to invoke LiveCycle ES2 using SOAP mode
            Properties connectionProps = new Properties();
            connectionProps.setProperty(ServiceClientFactoryProperties.DSC_DEFAULT_SOAP_ENDPOINT, this.endpoint());
            connectionProps.setProperty(ServiceClientFactoryProperties.DSC_TRANSPORT_PROTOCOL,ServiceClientFactoryProperties.DSC_SOAP_PROTOCOL);
            connectionProps.setProperty(ServiceClientFactoryProperties.DSC_SERVER_TYPE, "JBoss");

            if ((this.username() != null) && (this.username().trim().length() > 0)) {
                connectionProps.setProperty(ServiceClientFactoryProperties.DSC_CREDENTIAL_USERNAME, this.username());
                connectionProps.setProperty(ServiceClientFactoryProperties.DSC_CREDENTIAL_PASSWORD, this.password());
            }

            // Create a ServiceClientFactory object
            ServiceClientFactory myFactory = ServiceClientFactory.createInstance(connectionProps);

            // Create a FormsServiceClient object
            formsClient = new FormsServiceClient(myFactory);

        } catch (Exception ioEx) {
            LOGGER.log(Level.SEVERE, ioEx.getMessage());
            return null;
        }
        return formsClient;
    }

    public URLSpec getURLSpec() {
        URLSpec uriValues = new URLSpec();
        uriValues.setApplicationWebRoot(null);
        uriValues.setContentRootURI(this.serverContext());
        uriValues.setTargetURL(null);
        return uriValues;
    }

    public PDFFormRenderSpec getPdfFormRenderSpec() {

        PDFFormRenderSpec pdfFormRenderSpec = new PDFFormRenderSpec();

        String options = this.options();

        if ((options != null) && (options.trim().length() > 0)) {
            String[] optionProps  = options.split("&");
            if ((optionProps != null) && (optionProps.length > 0)) {
                for (int i = 0; i < optionProps.length; i++) {
                    String[] keyValuePair = optionProps[i].split("=");
                    if ((keyValuePair != null) && (keyValuePair.length == 2)) {
                        if ("CachedEnabled".equals(keyValuePair[0])) {
                            pdfFormRenderSpec.setCacheEnabled(Boolean.valueOf(keyValuePair[1]));
                        } else if ("RenderAtClient".equals(keyValuePair[0])) {
                            if (Boolean.valueOf(keyValuePair[1])) {
                                pdfFormRenderSpec.setRenderAtClient(RenderAtClient.Yes);
                            } else {
                                pdfFormRenderSpec.setRenderAtClient(RenderAtClient.No);
                            }
                        } else if ("PDFVersion".equals(keyValuePair[0])) {
                            if ("1.5".equals(keyValuePair[1])) {
                                pdfFormRenderSpec.setPDFVersion( PDFVersion.PDFVersion_1_5);
                            } else if ("1.6".equals(keyValuePair[1])) {
                                pdfFormRenderSpec.setPDFVersion(PDFVersion.PDFVersion_1_6);
                            } else if ("1.7".equals(keyValuePair[1])) {
                                pdfFormRenderSpec.setPDFVersion(PDFVersion.PDFVersion_1_7);
                            }
                        } else {
                            LOGGER.log(Level.INFO, "FSRenderer Option " + keyValuePair[0] + "is not configured");
                        }
                    }
                }
            }
        }
        return pdfFormRenderSpec;
    }
}
