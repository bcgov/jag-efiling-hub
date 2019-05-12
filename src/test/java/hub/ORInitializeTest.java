package hub;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import hub.helper.Environment;
import hub.helper.HttpResponse;
import hub.helper.PostRequest;
import hub.helper.StreamReader;
import hub.http.CsoAccountServlet;
import hub.http.ORInitializeServlet;
import hub.support.HavingHubRunning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Base64;

import static hub.support.GetRequest.get;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ORInitializeTest extends HavingHubRunning {

    private HttpServer objectRepository;
    private Headers sentHeaders;
    private String contentType;
    private String willAnswer = "OK";
    private String method;
    private String body;

    @Before
    public void startServer() throws Exception {
        objectRepository = HttpServer.create( new InetSocketAddress( 8111 ), 0 );
        objectRepository.createContext( "/", exchange -> {
            body = StreamReader.readStream(exchange.getRequestBody());
            method = exchange.getRequestMethod();
            sentHeaders = exchange.getRequestHeaders();
            contentType = exchange.getRequestHeaders().getFirst("content-type");
            exchange.sendResponseHeaders( 200, willAnswer.length() );
            exchange.getResponseBody().write( willAnswer.getBytes() );
            exchange.close();
        } );
        objectRepository.start();
        context.addServlet(ORInitializeServlet.class, "/initialize");
        server.start();
    }

    @After
    public void stopCsoServer() {
        objectRepository.stop( 0 );
    }
    @Before
    public void setProperties() {
        System.setProperty("OR_ENDPOINT_INITIALIZE", "http4://localhost:8111");
        System.setProperty("OR_APP_ID", "this-id");
        System.setProperty("OR_APP_PASSWORD", "this-password");
        System.setProperty("OR_BASIC_AUTH_USERNAME", "this-basic-auth-username");
        System.setProperty("OR_BASIC_AUTH_PASSWORD", "this-basic-auth-password");
    }

    @Test
    public void methodIsPost() throws Exception {
        get("http://localhost:8888/initialize");

        assertThat(method, equalTo("POST"));
    }

    @Test
    public void contentTypeIsJson() throws Exception {
        get("http://localhost:8888/initialize");

        assertThat(contentType, equalTo("application/json"));
    }

    @Test
    public void bodyIsAppCredentials() throws Exception {
        String expected = "" +
            "{" +
            "   \"AppId\":\"this-id\"," +
            "   \"AppPwd\":\"this-password\"," +
            "   \"TicketLifeTime\":\"120\"" +
            "}";
        get("http://localhost:8888/initialize");

        assertThat(body, equalTo(expected));
    }

    @Test
    public void sendBasicAuthHeader() throws Exception {
        String expected = "Basic " + Base64.getEncoder().encodeToString(
                ("this-basic-auth-username:this-basic-auth-password").getBytes());
        get("http://localhost:8888/initialize");

        assertThat(sentHeaders.getFirst("Authorization"), equalTo(expected));
    }

    @Test
    public void propagatesReceivedValue() throws Exception {
        HttpResponse response = get("http://localhost:8888/initialize");

        assertThat(response.getBody(), equalTo("OK"));
    }


    public void returnsApplicationTicket() throws Exception {
        HttpResponse response = get("http://localhost:8888/initialize");

        assertThat(response.getBody(), containsString("AppTicket"));
        assertThat(response.getContentType(), equalTo("application/json"));
    }


    public void manualCall() throws Exception {
        ORInitialize initialize = new ORInitialize();
        initialize.environment = new Environment();
        HttpResponse response = PostRequest.post(initialize.url(), initialize.headers(), initialize.body().getBytes());

        assertThat(response.getBody(), containsString("AppTicket"));
        assertThat(response.getContentType(), equalTo("application/json"));
    }
}
