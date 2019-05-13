package hub;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import hub.helper.HttpResponse;
import hub.support.HavingTestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.logging.Logger;

import static hub.support.Answers.accountInfo;
import static hub.support.Answers.accountNotFound;
import static hub.support.GetRequest.get;
import static hub.support.Resource.bodyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CsoAccountTest extends HavingTestProperties {

    private static final Logger LOGGER = Logger.getLogger(CsoAccountTest.class.getName());

    private HttpServer cso;
    private Headers sentHeaders;
    private String contentType;
    private String willAnswer;

    private Hub hub;

    @Before
    public void startHub() throws Exception {
        System.setProperty("CSO_NAMESPACE", "http://hub.org");
        System.setProperty("CSO_ACCOUNT_INFO_ENDPOINT", "http4://localhost:8111");

        hub = new Hub(8888);
        hub.start();
    }
    @After
    public void stopHub() throws Exception {
        hub.stop();
    }

    @Before
    public void startServer() throws Exception {
        cso = HttpServer.create( new InetSocketAddress( 8111 ), 0 );
        cso.createContext( "/", exchange -> {
            sentHeaders = exchange.getRequestHeaders();
            contentType = exchange.getRequestHeaders().getFirst("content-type");
            exchange.sendResponseHeaders( 200, willAnswer.length() );
            exchange.getResponseBody().write( willAnswer.getBytes() );
            exchange.close();
        } );
        cso.start();
    }

    @After
    public void stopCsoServer() {
        cso.stop( 0 );
    }
    @Before
    public void setProperties() {
        System.setProperty("CSO_NAMESPACE", "csoext-namespace");
        System.setProperty("CSO_ACCOUNT_INFO_ENDPOINT", "http4://localhost:8111");
        System.setProperty("CSO_ACCOUNT_INFO_SOAP_ACTION", "account-info-soap-action");
        System.setProperty("CSO_USER", "this-user");
        System.setProperty("CSO_PASSWORD", "this-password");
        willAnswer = accountInfo();
    }

    @Test
    public void xmlContentTypeIsMandatory() throws Exception {
        get("http://localhost:8888/account?accountId=1304");

        assertThat(contentType, equalTo("text/xml"));
    }

    @Test
    public void returnsInfoAsJson() throws Exception {
        HttpResponse response = get("http://localhost:8888/account?accountId=1304");

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
    }

    @Test
    public void returnsInfo() throws Exception {
        String answer = bodyOf("http://localhost:8888/account?accountId=1304");

        assertThat(answer, containsString("\"accountId\":\"1304\""));
        assertThat(answer, containsString("\"accountName\":\"Minnie Mouse.\""));
    }

    @Test
    public void soapActionIsSetInHeaders() throws Exception {
        get("http://localhost:8888/account?accountId=1304");

        assertThat(sentHeaders.getFirst("SOAPAction"), equalTo("account-info-soap-action"));
    }

    @Test
    public void basicAuthorizationSetInHeaders() throws Exception {
        String expected = "Basic " + Base64.getEncoder().encodeToString(("this-user:this-password").getBytes());
        get("http://localhost:8888/account?accountId=1304");

        assertThat(sentHeaders.getFirst("Authorization"), equalTo(expected));
    }

    @Test
    public void resistsBadRequest() throws Exception {
        HttpResponse response = get("http://localhost:8888/account");

        assertThat(response.getStatusCode(), equalTo(400));
        assertThat(response.getBody(), equalTo("Bad Request"));
    }

    @Test
    public void resistsUnknown() throws Exception {
        willAnswer = accountNotFound();
        HttpResponse response = get("http://localhost:8888/account?accountId=unknown");

        assertThat(response.getStatusCode(), equalTo(404));
        assertThat(response.getBody(), equalTo("NOT FOUND"));
    }
}
