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

import static hub.support.GetRequest.get;
import static hub.support.PostRequest.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SubmitTest extends HavingTestProperties {

    private HttpServer initializeServer;
    private String initializeAnswer = "{ \"AppTicket\":\"ticket-value\" }";

    private HttpServer saveServer;
    private Headers saveHeaders;
    private String saveAnswer = "{ \"Object_GUID\":\"this-GUID\" }";
    private String saveMethod;
    private byte[] saveBody;
    private String saveUri;

    private HttpServer changeOwnerServer;
    private Headers changeOwnerHeaders;
    private String changeOwnerAnswer = "OK";
    private String changeOwnerMethod;
    private String changeOwnerBody;

    private HttpServer paymentServer;
    private Headers paymentHeaders;
    private int paymentResponseStatus = 200;
    private String paymentAnswer = "<return><answer>ok</answer></return>";
    private String paymentMethod;
    private String paymentBody;

    private HttpServer webcatsServer;
    private Headers webcatsHeaders;
    private int webcatsResponseStatus = 200;
    private String webcatsAnswer = "<return><update>ok</update></return>";
    private String webcatsMethod;
    private String webcatsBody;

    private Hub hub;
    private Date now;

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

        System.setProperty("WEBCATS_UPDATE_ENDPOINT", "http4://localhost:8555");
        System.setProperty("WEBCATS_UPDATE_SOAP_ACTION", "webcats-update-soap-action");
        System.setProperty("WEBCATS_USERNAME", "webcats-user");
        System.setProperty("WEBCATS_PASSWORD", "webcats-password");
        System.setProperty("WEBCATS_XMLNS_NS", "this-ns");
        System.setProperty("WEBCATS_XMLNS_DAT", "this-dat");

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

        paymentServer = HttpServer.create( new InetSocketAddress( 8444 ), 0 );
        paymentServer.createContext( "/", exchange -> {
            paymentBody = new Stringify().inputStream(exchange.getRequestBody());
            paymentMethod = exchange.getRequestMethod();
            paymentHeaders = exchange.getRequestHeaders();
            exchange.sendResponseHeaders( paymentResponseStatus, paymentAnswer.length() );
            exchange.getResponseBody().write( paymentAnswer.getBytes() );
            exchange.close();
        } );
        paymentServer.start();

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
        paymentServer.stop( 0 );
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
        assertThat(saveResponse.getBody(), equalTo("{ \"Object_GUID\":\"this-GUID\" }"));
    }

    @Test
    public void worksAsExpected() throws Exception {
        String expectedBasicAuth = "Basic " + Base64.getEncoder().encodeToString(
                ("this-basic-auth-username:this-basic-auth-password").getBytes());

        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));

        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\"}");

        post("http://localhost:8888/save", headers, pdf);

        assertThat(saveMethod, equalTo("POST"));
        assertThat(saveUri, equalTo("/?AppTicket=ticket-value&MimeType=application&MimeSubType=pdf&Filename=form2.pdf&RetentionPeriod=-1"));
        assertThat(saveHeaders.getFirst("Authorization"), equalTo(expectedBasicAuth));
        assertThat(saveBody, equalTo(named("form2-1.pdf")));

        assertThat(changeOwnerMethod, equalTo("PUT"));
        assertThat(changeOwnerHeaders.getFirst("Authorization"), equalTo(expectedBasicAuth));
        assertThat(changeOwnerBody, equalTo("{ \"AppTicket\":\"ticket-value\", \"ObjectGUID\":\"this-GUID\", \"Application\":\"WebCATS\" }"));

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

        assertThat(webcatsMethod, equalTo("POST"));
        assertThat(webcatsHeaders.getFirst("Authorization"), equalTo("Basic " + Base64.getEncoder().encodeToString(("webcats-user:webcats-password").getBytes())));
        assertThat(webcatsHeaders.getFirst("Content-Type"), equalTo("text/xml"));
        assertThat(webcatsHeaders.getFirst("SOAPAction"), equalTo("webcats-update-soap-action"));
        assertThat(webcatsBody, equalTo("" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:dat=\"this-dat\" xmlns:ns=\"this-ns\">" +
                    "<SOAP-ENV:Header/>" +
                    "<SOAP-ENV:Body>" +
                        "<ns:UpdateWebCATS>" +
                            "<ns:updateRequest>" +
                                "<dat:CaseNumber>CA12345</dat:CaseNumber>" +
                                "<dat:Documents>" +
                                    "<dat:Document>" +
                                        "<dat:DateFiled>"+now()+"</dat:DateFiled>" +
                                        "<dat:DocumentGUID>this-GUID</dat:DocumentGUID>" +
                                        "<dat:DocumentName>Notice of Appearance</dat:DocumentName>" +
                                        "<dat:DocumentTypeCode>APP</dat:DocumentTypeCode>" +
                                        "<dat:DocumentTypeDescription>Appearance</dat:DocumentTypeDescription>" +
                                        "<dat:InitiatingDocument>N</dat:InitiatingDocument>" +
                                    "</dat:Document>" +
                                "</dat:Documents>" +
                            "</ns:updateRequest>" +
                        "</ns:UpdateWebCATS>" +
                    "</SOAP-ENV:Body>" +
                "</SOAP-ENV:Envelope>"));
    }

    private String now() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        return dateFormatGmt.format(now);
    }

    @Test
    public void returnsInfoAsJson() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));

        Map<String, String> headers = new HashMap<>();
        headers.put("smgov_userguid", "MAX");
        headers.put("data", "{\"formSevenNumber\":\"CA12345\"}");

        HttpResponse response = post("http://localhost:8888/save", headers, pdf);

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), containsString("\"update\":\"ok\""));
    }


    private byte[] named(String name) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(name).getFile());
        return Files.readAllBytes(file.toPath());
    }

    public void returnsGuidWhenRanAgainstRealOr() throws Exception {
        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));

        HttpResponse response = post("http://localhost:8888/save", pdf);

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
        HttpResponse response = post("http://localhost:8888/save", headers, pdf);

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
        HttpResponse response = post("http://localhost:8888/save", headers, pdf);

        assertThat(response.getStatusCode(), equalTo(500));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), equalTo("{\"message\":\"Failed - Payment failed\"}"));
    }
}
