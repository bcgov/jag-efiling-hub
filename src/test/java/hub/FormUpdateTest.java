package hub;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import hub.helper.HttpResponse;
import hub.helper.Stringify;
import hub.support.HavingTestProperties;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.logging.Logger;

import static hub.support.PostRequest.post;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FormUpdateTest extends HavingTestProperties {

    private static final Logger LOGGER = Logger.getLogger(FormUpdateTest.class.getName());
    private HttpServer webcats;
    private Hub hub;
    private String body;
    private String method;
    private Headers headers;

    @Before
    public void startHub() throws Exception {
        System.setProperty("WEBCATS_UPDATE_ACTION", "update-action");
        System.setProperty("WEBCATS_USERNAME", "this-username");
        System.setProperty("WEBCATS_PASSWORD", "this-password");
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
    public void startServer() throws Exception {
        webcats = HttpServer.create( new InetSocketAddress( 8111 ), 0 );
        webcats.createContext( "/", exchange -> {
            body = new Stringify().inputStream(exchange.getRequestBody());
            method = exchange.getRequestMethod();
            headers = exchange.getRequestHeaders();
            String answer = "<any><response>value</response></any>";
            exchange.sendResponseHeaders( 200, answer.length() );
            exchange.getResponseBody().write( answer.getBytes() );
            exchange.close();
        } );
        webcats.start();
    }

    @After
    public void stopCsoServer() {
        webcats.stop( 0 );
    }

    @Test
    public void returnsAnswerAsJson() throws Exception {
        JSONObject jo = new JSONObject();
        jo.put("caseNumber", "CA12345");
        jo.put("guid", "42");
        HttpResponse response = post("http://localhost:8888/updateDocument", jo.toString().getBytes());

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), equalTo("{\"any\":{\"response\":\"value\"}}"));
    }

    @Test
    public void sendExpectedRequest() throws Exception {
        String expectedBasicAuth = "Basic " + Base64.getEncoder().encodeToString(
                ("this-username:this-password").getBytes());
        JSONObject jo = new JSONObject();
        jo.put("caseNumber", "CA12345");
        jo.put("guid", "42");
        post("http://localhost:8888/updateDocument", jo.toString().getBytes());

        assertThat(method, equalTo("POST"));
        assertThat(headers.getFirst("Authorization"), equalTo(expectedBasicAuth));
        assertThat(headers.getFirst("SOAPAction"), equalTo("update-action"));
        assertThat(body, equalTo(expected()));
    }

    private String expected() {
        String value = "" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:dat=\"this-dat\" xmlns:ns=\"this-ns\">" +
                "<SOAP-ENV:Header/>" +
                "<SOAP-ENV:Body>" +
                    "<ns:UpdateWebCATS>" +
                        "<ns:updateRequest>" +
                            "<dat:CaseNumber>CA12345</dat:CaseNumber>" +
                            "<dat:Documents>" +
                                "<dat:Document>" +
                                    "<dat:DateFiled>today</dat:DateFiled>" +
                                    "<dat:DocumentGUID>42</dat:DocumentGUID>" +
                                    "<dat:DocumentName>Form2</dat:DocumentName>" +
                                    "<dat:DocumentTypeCode>Form2</dat:DocumentTypeCode>" +
                                    "<dat:DocumentTypeDescription>Form2</dat:DocumentTypeDescription>" +
                                    "<dat:InitiatingDocument>unknown</dat:InitiatingDocument>" +
                                "</dat:Document>" +
                            "</dat:Documents>" +
                        "</ns:updateRequest>" +
                    "</ns:UpdateWebCATS>" +
                "</SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>";

        return value;
    }
}
