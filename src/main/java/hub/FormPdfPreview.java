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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
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
            Document document = new Document(xmlData);
            PDFFormRenderSpec pdfFormRenderSpec = this.getPdfFormRenderSpec();
            URLSpec urlSpec = this.getURLSpec();

            // Invoke the renderPDFForm method to render an interactive PDF form on the client
            FormsServiceClient formsClient = this.createFormsServiceClient();
            FormsResult formOut = formsClient.renderPDFForm(xdpName, document, pdfFormRenderSpec, urlSpec, null);

            LOGGER.log(Level.INFO, "Got IOutputContext..." + (formOut == null));

            // Create a byte array. Call the IOutputContext interface's getOutputContext method
            Document doc = formOut.getOutputContent();
            byte[] cContent = bytify.inputStram(doc.getInputStream());
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
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("Form7_data.xml").toURI().getPath();
        File file = new File(path);

        return Files.readAllBytes(file.toPath());
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
