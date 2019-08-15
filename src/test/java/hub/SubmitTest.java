package hub;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import hub.helper.Bytify;
import hub.helper.Environment;
import hub.helper.HttpResponse;
import hub.helper.Stringify;
import hub.support.HavingTestProperties;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static hub.support.PostRequest.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SubmitTest extends HavingTestProperties {

    private HttpServer initializeServer;
    private String initializeAnswer = "{ \"AppTicket\":\"ticket-value\" }";

    private HttpServer saveServer;
    private Headers saveHeaders;
    private String saveAnswer = "{ \"Object_GUID\":\"this-GUID_please\" }";
    private String saveMethod;
    private byte[] saveBody;
    private String saveUri;

    private HttpServer changeOwnerServer;
    private Headers changeOwnerHeaders;
    private String changeOwnerAnswer = "{\"Status\":\"Success\"}";
    private String changeOwnerMethod;
    private String changeOwnerBody;

    private HttpServer csoServer;
    private Headers csoHeaders;

    private Headers paymentHeaders;
    private int paymentResponseStatus = 200;
    private String paymentAnswer = "<return><answer>ok</answer><invoiceNumber>invoice-number-from-payment-call</invoiceNumber><serviceId>service-id-from-payment-call</serviceId></return>";
    private String paymentMethod;
    private String paymentBody;

    private Headers csoSaveFileHeaders;
    private int csoSaveFileResponseStatus = 200;
    private String csoSaveFileAnswer = "<return><save>ok</save><packageId>package-number-from-save-filing-call</packageId></return>";
    private String csoSaveFileMethod;
    private String csoSaveFileBody;

    private HttpServer webcatsServer;
    private Headers webcatsHeaders;
    private int webcatsResponseStatus = 200;
    private String webcatsAnswer = "<return><update>ok</update></return>";
    private String webcatsMethod;
    private String webcatsBody;


    private Hub hub;
    private Date now;
    private String submitUrl = "http://localhost:8888/save";


    @Before
    public void startHub() throws Exception {
        System.setProperty("OR_ENDPOINT_INITIALIZE", "http4://localhost:8111");
        System.setProperty("OR_ENDPOINT_CREATE", "http4://localhost:8222");
        System.setProperty("OR_ENDPOINT_CHANGEOWNER", "http4://localhost:8333");
        System.setProperty("OR_APP_ID", "this-id");
        System.setProperty("OR_APP_PASSWORD", "this-password");
        System.setProperty("OR_BASIC_AUTH_USERNAME", "this-basic-auth-username");
        System.setProperty("OR_BASIC_AUTH_PASSWORD", "this-basic-auth-password");

        System.setProperty("CSO_EXTENSION_ENDPOINT", "http4://localhost:8444");
        System.setProperty("CSO_USER", "cso-user");
        System.setProperty("CSO_PASSWORD", "cso-password");
        System.setProperty("CSO_NAMESPACE", "http://hub.org");
        System.setProperty("CSO_PAYMENT_PROCESS_SOAP_ACTION", "payment-process-soap-action");
        System.setProperty("CSO_SAVE_FILING_SOAP_ACTION", "cso-save-filing-soap-action");

        System.setProperty("WEBCATS_UPDATE_ENDPOINT", "http4://localhost:8555");
        System.setProperty("WEBCATS_UPDATE_SOAP_ACTION", "webcats-update-soap-action");
        System.setProperty("WEBCATS_USERNAME", "webcats-user");
        System.setProperty("WEBCATS_PASSWORD", "webcats-password");
        System.setProperty("WEBCATS_XMLNS_NS", "this-ns");
        System.setProperty("WEBCATS_XMLNS_DAT", "this-dat");

        System.setProperty("OVERWRITE_USERGUID_WITH_THIS_VALUE", "");
        System.setProperty("OVERWRITE_ACCOUNT_ID_WITH_THIS_VALUE", "");
        System.setProperty("OVERWRITE_CLIENT_ID_WITH_THIS_VALUE", "");

        hub = new Hub(8888);
        hub.start();
    }
    @After
    public void stopHub() throws Exception {
        hub.stop();
    }

    @Before
    public void startServers() throws Exception {
        initializeServer = HttpServer.create( new InetSocketAddress( 8111 ), 0 );
        initializeServer.createContext( "/", exchange -> {
            exchange.sendResponseHeaders( 200, initializeAnswer.length() );
            exchange.getResponseBody().write( initializeAnswer.getBytes() );
            exchange.close();
        } );
        initializeServer.start();

        saveServer = HttpServer.create( new InetSocketAddress( 8222 ), 0 );
        saveServer.createContext( "/", exchange -> {
            saveUri = exchange.getRequestURI().toString();
            saveBody = new Bytify().inputStream(exchange.getRequestBody());
            saveMethod = exchange.getRequestMethod();
            saveHeaders = exchange.getRequestHeaders();
            exchange.sendResponseHeaders( 200, saveAnswer.length() );
            exchange.getResponseBody().write( saveAnswer.getBytes() );
            exchange.close();
        } );
        saveServer.start();

        changeOwnerServer = HttpServer.create( new InetSocketAddress( 8333 ), 0 );
        changeOwnerServer.createContext( "/", exchange -> {
            changeOwnerBody = new Stringify().inputStream(exchange.getRequestBody());
            changeOwnerMethod = exchange.getRequestMethod();
            changeOwnerHeaders = exchange.getRequestHeaders();
            exchange.sendResponseHeaders( 200, changeOwnerAnswer.length() );
            exchange.getResponseBody().write( changeOwnerAnswer.getBytes() );
            exchange.close();
        } );
        changeOwnerServer.start();

        csoServer = HttpServer.create( new InetSocketAddress( 8444 ), 0 );
        csoServer.createContext( "/", exchange -> {
            csoHeaders = exchange.getRequestHeaders();
            String soapAction = csoHeaders.getFirst("SOAPAction");
            if ("payment-process-soap-action".equalsIgnoreCase(soapAction)) {
                paymentBody = new Stringify().inputStream(exchange.getRequestBody());
                paymentMethod = exchange.getRequestMethod();
                paymentHeaders = csoHeaders;
                exchange.sendResponseHeaders(paymentResponseStatus, paymentAnswer.length());
                exchange.getResponseBody().write(paymentAnswer.getBytes());
            }
            else if ("cso-save-filing-soap-action".equalsIgnoreCase(soapAction)) {
                csoSaveFileBody = new Stringify().inputStream(exchange.getRequestBody());
                csoSaveFileMethod = exchange.getRequestMethod();
                csoSaveFileHeaders = exchange.getRequestHeaders();
                exchange.sendResponseHeaders( csoSaveFileResponseStatus, csoSaveFileAnswer.length() );
                exchange.getResponseBody().write( csoSaveFileAnswer.getBytes() );
            }
            exchange.close();
        } );
        csoServer.start();

        webcatsServer = HttpServer.create( new InetSocketAddress( 8555 ), 0 );
        webcatsServer.createContext( "/", exchange -> {
            webcatsBody = new Stringify().inputStream(exchange.getRequestBody());
            webcatsMethod = exchange.getRequestMethod();
            webcatsHeaders = exchange.getRequestHeaders();
            exchange.sendResponseHeaders( webcatsResponseStatus, webcatsAnswer.length() );
            exchange.getResponseBody().write( webcatsAnswer.getBytes() );
            exchange.close();
        } );
        webcatsServer.start();
    }

    @Before
    public void fixeTime() {
        now = new Date();
        Clock.broken(now);
    }

    @After
    public void stopServer() {
        initializeServer.stop( 0 );
        saveServer.stop( 0 );
        changeOwnerServer.stop( 0 );
        csoServer.stop( 0 );
        webcatsServer.stop(0);
    }

    @Test
    public void expectations() throws Exception {
        System.setProperty("OR_ENDPOINT_INITIALIZE", "http://localhost:8111");
        System.setProperty("OR_ENDPOINT_CREATE", "http://localhost:8222");
        String expectedBasicAuth = "Basic " + Base64.getEncoder().encodeToString(
                ("this-basic-auth-username:this-basic-auth-password").getBytes());

        ORInitialize initialize = new ORInitialize();
        initialize.environment = new Environment();
        HttpResponse initializeResponse = post(initialize.url(), initialize.headers(), initialize.body().getBytes());

        JSONObject jo = new JSONObject(initializeResponse.getBody());
        String ticket = (String) jo.get("AppTicket");

        ORSave save = new ORSave();
        save.environment = new Environment();
        HttpResponse saveResponse = post(save.url(ticket), save.headers(), named("form2-1.pdf"));

        assertThat(saveMethod, equalTo("POST"));
        assertThat(saveUri, equalTo("/?AppTicket=ticket-value&MimeType=application&MimeSubType=pdf&Filename=form2.pdf&RetentionPeriod=-1"));
        assertThat(saveHeaders.getFirst("Authorization"), equalTo(expectedBasicAuth));
        assertThat(saveBody, equalTo(named("form2-1.pdf")));
        assertThat(saveResponse.getBody(), equalTo("{ \"Object_GUID\":\"this-GUID_please\" }"));
    }

    @Test
    public void sendsExpectedRequestToPayment() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");
        post(submitUrl, headers, pdf);

        assertThat(paymentMethod, equalTo("POST"));
        assertThat(paymentHeaders.getFirst("Authorization"), equalTo("Basic " + Base64.getEncoder().encodeToString(("cso-user:cso-password").getBytes())));
        assertThat(paymentHeaders.getFirst("Content-Type"), equalTo("text/xml"));
        assertThat(paymentHeaders.getFirst("SOAPAction"), equalTo("payment-process-soap-action"));
        assertThat(paymentBody, containsString("" +
                "<cso:paymentProcess xmlns:cso=\"http://hub.org\">" +
                "<serviceType>EXFL</serviceType>" +
                "<serviceDesc>Form 2 Filing payment</serviceDesc>" +
                "<userguid>MAX</userguid>" +
                "<bcolUserId/>" +
                "<bcolSessionKey/>" +
                "<bcolUniqueId/>" +
                "</cso:paymentProcess>"));
    }

    @Test
    public void resistsPaymentFailed() throws Exception {
        paymentResponseStatus = 200;
        paymentAnswer = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <soap:Body>\n" +
                "      <ns2:paymentProcessResponse xmlns:ns2=\"http://csoextws.jag.gov.bc.ca/\">\n" +
                "         <return>\n" +
                "            <resultCode>1</resultCode>\n" +
                "            <resultMessage>Failed - Either Account id or BC Client id is blank</resultMessage>\n" +
                "         </return>\n" +
                "      </ns2:paymentProcessResponse>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>";

        byte[] pdf = named("form2-1.pdf");
        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");
        HttpResponse response = post(submitUrl, headers, pdf);

        assertThat(response.getStatusCode(), equalTo(403));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), equalTo("{\"message\":\"Failed - Either Account id or BC Client id is blank\"}"));
    }

    @Test
    public void resistsPaymentError() throws Exception {
        paymentResponseStatus = 500;
        paymentAnswer = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <soap:Body>\n" +
                "      <soap:Fault>\n" +
                "         <faultcode>soap:Server</faultcode>\n" +
                "         <faultstring><![CDATA[BeanstreamPaymentManager.billRegisteredCreditCard 0:<LI>Invalid customer code<br><LI>Invalid Card Number<br><LI>Invalid expiration month<br><LI>Invalid expiration year<br>]]></faultstring>\n" +
                "         <detail>\n" +
                "            <ns1:CsoextwsException xmlns:ns1=\"http://csoextws.jag.gov.bc.ca/\"/>\n" +
                "         </detail>\n" +
                "      </soap:Fault>\n" +
                "   </soap:Body>\n" +
                "</soap:Envelope>";

        byte[] pdf = named("form2-1.pdf");
        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");
        HttpResponse response = post(submitUrl, headers, pdf);

        assertThat(response.getStatusCode(), equalTo(500));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), equalTo("{\"message\":\"Failed - Payment failed\"}"));
    }

    @Test
    public void sendsExpectedRequestToObjectRepository() throws Exception {
        String expectedBasicAuth = "Basic " + Base64.getEncoder().encodeToString(
                ("this-basic-auth-username:this-basic-auth-password").getBytes());

        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));

        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");

        post(submitUrl, headers, pdf);

        assertThat(saveMethod, equalTo("POST"));
        assertThat(saveUri, equalTo("/?AppTicket=ticket-value&MimeType=application&MimeSubType=pdf&Filename=form2.pdf&RetentionPeriod=-1"));
        assertThat(saveHeaders.getFirst("Authorization"), equalTo(expectedBasicAuth));
        assertThat(saveBody, equalTo(named("form2-1.pdf")));

        assertThat(changeOwnerMethod, equalTo("PUT"));
        assertThat(changeOwnerHeaders.getFirst("Authorization"), equalTo(expectedBasicAuth));
        assertThat(changeOwnerBody, equalTo("{ \"AppTicket\":\"ticket-value\", \"ObjectGUID\":\"this/GUID/please\", \"Application\":\"WebCATS\" }"));
    }

    @Test
    public void sendsExpectedRequestToCsoSaveFile() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":true,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");
        post(submitUrl, headers, pdf);

        assertThat(csoSaveFileMethod, equalTo("POST"));
        assertThat(csoSaveFileHeaders.getFirst("Authorization"), equalTo("Basic " + Base64.getEncoder().encodeToString(("cso-user:cso-password").getBytes())));
        assertThat(csoSaveFileHeaders.getFirst("Content-Type"), equalTo("text/xml"));
        assertThat(csoSaveFileHeaders.getFirst("SOAPAction"), equalTo("cso-save-filing-soap-action"));
        assertThat(csoSaveFileBody, equalTo("" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cso=\"http://csoextws.jag.gov.bc.ca/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <cso:saveFiling>\n" +
                "            <userguid>MAX</userguid>\n" +
                "            <bcolUserId></bcolUserId>\n" +
                "            <bcolSessionKey></bcolSessionKey>\n" +
                "            <bcolUniqueId></bcolUniqueId>\n" +
                "            <efilingPackage>\n" +
                "                <cfcsa>false</cfcsa>\n" +
                "                <classCd>O</classCd>\n" +
                "                <clientRefNo></clientRefNo>\n" +
                "                <comments></comments>\n" +
                "                <courtFileNumber>CA12345</courtFileNumber>\n" +
                "                <divisionCd>I</divisionCd>\n" +
                "                <documents>\n" +
                "                    <objectGUID>this/GUID/please</objectGUID>\n" +
                "                    <documentDescriptionTxt></documentDescriptionTxt>\n" +
                "                    <documentStatusTypeCd>FILE</documentStatusTypeCd>\n" +
                "                    <documentSubTypeCd>ODOC</documentSubTypeCd>\n" +
                "                    <documentTypeCd>NAA</documentTypeCd>\n" +
                "                    <feeExempt>false</feeExempt>\n" +
                "                    <filenameTxt>NoticeOfAppeal</filenameTxt>\n" +
                "                    <initiating>false</initiating>\n" +
                "                    <orderDocument>false</orderDocument>\n" +
                "                    <statusDtm>"+now()+"</statusDtm>\n" +
                "                    <uploadStateCd>CMPL</uploadStateCd>\n" +
                "                    <uploadedToApplicationCd>WEBCATS</uploadedToApplicationCd>\n" +
                "                </documents>\n" +
                "                <existingFile>false</existingFile>\n" +
                "                <indigent>false</indigent>\n" +
                "                <invoiceNo>invoice-number-from-payment-call</invoiceNo>\n" +
                "                <levelCd>A</levelCd>\n" +
                "                <locationCd>COA</locationCd>\n" +
                "                <notificationEmail></notificationEmail>\n" +
                "                <parties>\n" +
                "                    <firstGivenName>Max</firstGivenName>\n" +
                "                    <nameTypeCd></nameTypeCd>\n" +
                "                    <organizationName></organizationName>\n" +
                "                    <partyTypeCd>IND</partyTypeCd>\n" +
                "                    <roleTypeCd>APL</roleTypeCd>\n" +
                "                    <secondGivenName></secondGivenName>\n" +
                "                    <surnameName>FREE</surnameName>\n" +
                "                    <thirdGivenName></thirdGivenName>\n" +
                "                </parties>\n" +
                "                <parties>\n" +
                "                    <firstGivenName>MAX</firstGivenName>\n" +
                "                    <nameTypeCd></nameTypeCd>\n" +
                "                    <organizationName></organizationName>\n" +
                "                    <partyTypeCd>IND</partyTypeCd>\n" +
                "                    <roleTypeCd>APL</roleTypeCd>\n" +
                "                    <secondGivenName></secondGivenName>\n" +
                "                    <surnameName>SUPERFREE</surnameName>\n" +
                "                    <thirdGivenName></thirdGivenName>\n" +
                "                </parties>\n" +
                "                <parties>\n" +
                "                    <firstGivenName>Bob</firstGivenName>\n" +
                "                    <nameTypeCd></nameTypeCd>\n" +
                "                    <organizationName></organizationName>\n" +
                "                    <partyTypeCd>IND</partyTypeCd>\n" +
                "                    <roleTypeCd>RES</roleTypeCd>\n" +
                "                    <secondGivenName></secondGivenName>\n" +
                "                    <surnameName>NOT SO FREE</surnameName>\n" +
                "                    <thirdGivenName></thirdGivenName>\n" +
                "                </parties>\n" +
                "                <parties>\n" +
                "                    <firstGivenName>BOB</firstGivenName>\n" +
                "                    <nameTypeCd></nameTypeCd>\n" +
                "                    <organizationName></organizationName>\n" +
                "                    <partyTypeCd>IND</partyTypeCd>\n" +
                "                    <roleTypeCd>RES</roleTypeCd>\n" +
                "                    <secondGivenName></secondGivenName>\n" +
                "                    <surnameName>NOT FREE</surnameName>\n" +
                "                    <thirdGivenName></thirdGivenName>\n" +
                "                </parties>\n" +
                "                <por>false</por>\n" +
                "                <processingComplete>true</processingComplete>\n" +
                "                <resubmission>false</resubmission>\n" +
                "                <rush>false</rush>\n" +
                "                <serviceId>service-id-from-payment-call</serviceId>\n" +
                "                <submittedDtm>"+now()+"</submittedDtm>\n" +
                "                <userAccess>\n" +
                "                    <accountId>111</accountId>\n" +
                "                    <clientId>1</clientId>\n" +
                "                    <privilegeCd>UPDT</privilegeCd>\n" +
                "                </userAccess>\n" +
                "                <userAccess>\n" +
                "                    <accountId>111</accountId>\n" +
                "                    <clientId>2</clientId>\n" +
                "                    <privilegeCd>READ</privilegeCd>\n" +
                "                </userAccess>\n" +
                "                <eNotification>false</eNotification>\n" +
                "            </efilingPackage>\n" +
                "        </cso:saveFiling>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"));
    }

    @Test
    public void sendsExpectedRequestToWebcats() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));

        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");

        post(submitUrl, headers, pdf);

        assertThat(webcatsMethod, equalTo("POST"));
        assertThat(webcatsHeaders.getFirst("Authorization"), equalTo("Basic " + Base64.getEncoder().encodeToString(("webcats-user:webcats-password").getBytes())));
        assertThat(webcatsHeaders.getFirst("Content-Type"), equalTo("text/xml"));
        assertThat(webcatsHeaders.getFirst("SOAPAction"), equalTo("webcats-update-soap-action"));
        assertThat(webcatsBody, equalTo("" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://courts.gov.bc.ca/ws/appeal/2011/01/12/\" xmlns:dat=\"http://courts.gov.bc.ca/ws/appeal/2011/01/12/datacontracts\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <ns:UpdateWebCATS>\n" +
                "            <ns:updateRequest>\n" +
                "                <dat:CaseNumber>CA12345</dat:CaseNumber>\n" +
                "                <dat:Documents>\n" +
                "                    <dat:Document>\n" +
                "                        <dat:DateFiled>"+now()+"</dat:DateFiled>\n" +
                "                        <dat:DocumentGUID>this/GUID/please</dat:DocumentGUID>\n" +
                "                        <dat:DocumentName>Notice of Appearance</dat:DocumentName>\n" +
                "                        <dat:DocumentTypeCode>APP</dat:DocumentTypeCode>\n" +
                "                        <dat:DocumentTypeDescription>Appearance</dat:DocumentTypeDescription>\n" +
                "                        <dat:InitiatingDocument>N</dat:InitiatingDocument>\n" +
                "                    </dat:Document>\n" +
                "                </dat:Documents>\n" +
                "            </ns:updateRequest>\n" +
                "        </ns:UpdateWebCATS>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"));
    }

    private String now() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        return dateFormatGmt.format(now);
    }

    @Test
    public void returnsInfoAsJson() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));

        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");

        HttpResponse response = post(submitUrl, headers, pdf);

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), equalTo("{\"invoiceNumber\":\"invoice-number-from-payment-call\",\"packageNumber\":\"package-number-from-save-filing-call\"}"));
    }


    private byte[] named(String name) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(name).getFile());
        return Files.readAllBytes(file.toPath());
    }

    public void returnsGuidWhenRanAgainstRealOr() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));

        HttpResponse response = post(submitUrl, pdf);

        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), containsString("Object_GUID"));
    }

    public void manualCallAgainstRealOr() throws Exception {
        ORInitialize initialize = new ORInitialize();
        initialize.environment = new Environment();
        HttpResponse response = post(initialize.url(), initialize.headers(), initialize.body().getBytes());

        assertThat(response.getStatusCode(), equalTo(200));
        JSONObject jo = new JSONObject(response.getBody());
        String ticket = (String) jo.get("AppTicket");

        ORSave save = new ORSave();
        save.environment = new Environment();
        response = post(save.url(ticket), save.headers(), named("form2-1.pdf"));

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), equalTo("Object_GUID"));
    }

    @Test
    public void resistsMissingUserGuid() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));
        Map<String, String> headers = new HashMap<>();
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");

        HttpResponse response = post(submitUrl, headers, pdf);

        assertThat(response.getStatusCode(), equalTo(400));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), equalTo("{\"message\":\"user id missing in http header\"}"));
    }

    @Test
    public void resistsMissingFormData() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));
        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");

        HttpResponse response = post(submitUrl, headers, pdf);

        assertThat(response.getStatusCode(), equalTo(400));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), equalTo("{\"message\":\"form data missing in http header\"}"));
    }

    @Test
    public void userguidCanBeOverwriten() throws Exception {
        System.setProperty("OVERWRITE_USERGUID_WITH_THIS_VALUE", "42");

        byte[] pdf = named("form2-1.pdf");
        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":111,\"country\":\"Canada\",\"clientId\":1,\"accountName\":\"Beautiful Victoria\",\"city\":\"Victoria\",\"postalCode\":\"V1V0V1\",\"addressLine1\":\"123 Street\",\"addressLine2\":\"Suite 123\"},\"authorizations\":[{\"clientId\":1,\"surname\":\"Bruce\",\"givenName\":\"Batman\",\"isAdmin\":true,\"isActive\":true,\"isEditable\":false},{\"clientId\":2,\"surname\":\"Clark\",\"givenName\":\"Superman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true},{\"clientId\":3,\"surname\":\"Diana\",\"givenName\":\"WonderWoman\",\"isAdmin\":false,\"isActive\":false,\"isEditable\":true}]}");
        post(submitUrl, headers, pdf);

        assertThat(paymentMethod, equalTo("POST"));
        assertThat(paymentHeaders.getFirst("Authorization"), equalTo("Basic " + Base64.getEncoder().encodeToString(("cso-user:cso-password").getBytes())));
        assertThat(paymentHeaders.getFirst("Content-Type"), equalTo("text/xml"));
        assertThat(paymentHeaders.getFirst("SOAPAction"), equalTo("payment-process-soap-action"));
        assertThat(paymentBody, containsString("" +
                "<cso:paymentProcess xmlns:cso=\"http://hub.org\">" +
                    "<serviceType>EXFL</serviceType>" +
                    "<serviceDesc>Form 2 Filing payment</serviceDesc>" +
                    "<userguid>42</userguid>" +
                    "<bcolUserId/>" +
                    "<bcolSessionKey/>" +
                    "<bcolUniqueId/>" +
                "</cso:paymentProcess>"));
    }

    @Test
    public void userAccessInfoOfConnectedUserCanBeOverwriten() throws Exception {
        System.setProperty("OVERWRITE_ACCOUNT_ID_WITH_THIS_VALUE", "666");
        System.setProperty("OVERWRITE_CLIENT_ID_WITH_THIS_VALUE", "777");

        byte[] pdf = named("form2-1.pdf");
        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{},\"authorizations\":[]}");
        post(submitUrl, headers, pdf);

        assertThat(csoSaveFileMethod, equalTo("POST"));
        assertThat(csoSaveFileHeaders.getFirst("Authorization"), equalTo("Basic " + Base64.getEncoder().encodeToString(("cso-user:cso-password").getBytes())));
        assertThat(csoSaveFileHeaders.getFirst("Content-Type"), equalTo("text/xml"));
        assertThat(csoSaveFileHeaders.getFirst("SOAPAction"), equalTo("cso-save-filing-soap-action"));
        assertThat(csoSaveFileBody, equalTo("" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:cso=\"http://csoextws.jag.gov.bc.ca/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <cso:saveFiling>\n" +
                "            <userguid>MAX</userguid>\n" +
                "            <bcolUserId></bcolUserId>\n" +
                "            <bcolSessionKey></bcolSessionKey>\n" +
                "            <bcolUniqueId></bcolUniqueId>\n" +
                "            <efilingPackage>\n" +
                "                <cfcsa>false</cfcsa>\n" +
                "                <classCd>O</classCd>\n" +
                "                <clientRefNo></clientRefNo>\n" +
                "                <comments></comments>\n" +
                "                <courtFileNumber>CA12345</courtFileNumber>\n" +
                "                <divisionCd>I</divisionCd>\n" +
                "                <documents>\n" +
                "                    <objectGUID>this/GUID/please</objectGUID>\n" +
                "                    <documentDescriptionTxt></documentDescriptionTxt>\n" +
                "                    <documentStatusTypeCd>FILE</documentStatusTypeCd>\n" +
                "                    <documentSubTypeCd>ODOC</documentSubTypeCd>\n" +
                "                    <documentTypeCd>NAA</documentTypeCd>\n" +
                "                    <feeExempt>false</feeExempt>\n" +
                "                    <filenameTxt>NoticeOfAppeal</filenameTxt>\n" +
                "                    <initiating>false</initiating>\n" +
                "                    <orderDocument>false</orderDocument>\n" +
                "                    <statusDtm>"+now()+"</statusDtm>\n" +
                "                    <uploadStateCd>CMPL</uploadStateCd>\n" +
                "                    <uploadedToApplicationCd>WEBCATS</uploadedToApplicationCd>\n" +
                "                </documents>\n" +
                "                <existingFile>false</existingFile>\n" +
                "                <indigent>false</indigent>\n" +
                "                <invoiceNo>invoice-number-from-payment-call</invoiceNo>\n" +
                "                <levelCd>A</levelCd>\n" +
                "                <locationCd>COA</locationCd>\n" +
                "                <notificationEmail></notificationEmail>\n" +
                "                <parties>\n" +
                "                    <firstGivenName>Max</firstGivenName>\n" +
                "                    <nameTypeCd></nameTypeCd>\n" +
                "                    <organizationName></organizationName>\n" +
                "                    <partyTypeCd>IND</partyTypeCd>\n" +
                "                    <roleTypeCd>APL</roleTypeCd>\n" +
                "                    <secondGivenName></secondGivenName>\n" +
                "                    <surnameName>FREE</surnameName>\n" +
                "                    <thirdGivenName></thirdGivenName>\n" +
                "                </parties>\n" +
                "                <parties>\n" +
                "                    <firstGivenName>MAX</firstGivenName>\n" +
                "                    <nameTypeCd></nameTypeCd>\n" +
                "                    <organizationName></organizationName>\n" +
                "                    <partyTypeCd>IND</partyTypeCd>\n" +
                "                    <roleTypeCd>APL</roleTypeCd>\n" +
                "                    <secondGivenName></secondGivenName>\n" +
                "                    <surnameName>SUPERFREE</surnameName>\n" +
                "                    <thirdGivenName></thirdGivenName>\n" +
                "                </parties>\n" +
                "                <parties>\n" +
                "                    <firstGivenName>Bob</firstGivenName>\n" +
                "                    <nameTypeCd></nameTypeCd>\n" +
                "                    <organizationName></organizationName>\n" +
                "                    <partyTypeCd>IND</partyTypeCd>\n" +
                "                    <roleTypeCd>RES</roleTypeCd>\n" +
                "                    <secondGivenName></secondGivenName>\n" +
                "                    <surnameName>NOT SO FREE</surnameName>\n" +
                "                    <thirdGivenName></thirdGivenName>\n" +
                "                </parties>\n" +
                "                <parties>\n" +
                "                    <firstGivenName>BOB</firstGivenName>\n" +
                "                    <nameTypeCd></nameTypeCd>\n" +
                "                    <organizationName></organizationName>\n" +
                "                    <partyTypeCd>IND</partyTypeCd>\n" +
                "                    <roleTypeCd>RES</roleTypeCd>\n" +
                "                    <secondGivenName></secondGivenName>\n" +
                "                    <surnameName>NOT FREE</surnameName>\n" +
                "                    <thirdGivenName></thirdGivenName>\n" +
                "                </parties>\n" +
                "                <por>false</por>\n" +
                "                <processingComplete>true</processingComplete>\n" +
                "                <resubmission>false</resubmission>\n" +
                "                <rush>false</rush>\n" +
                "                <serviceId>service-id-from-payment-call</serviceId>\n" +
                "                <submittedDtm>"+now()+"</submittedDtm>\n" +
                "                <userAccess>\n" +
                "                    <accountId>666</accountId>\n" +
                "                    <clientId>777</clientId>\n" +
                "                    <privilegeCd>UPDT</privilegeCd>\n" +
                "                </userAccess>\n" +
                "                <eNotification>false</eNotification>\n" +
                "            </efilingPackage>\n" +
                "        </cso:saveFiling>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"));
    }
}
