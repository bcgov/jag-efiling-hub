package hub;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import hub.helper.Environment;
import hub.helper.HttpResponse;
import hub.helper.PostRequest;
import hub.helper.StreamReader;
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

    private HttpServer initializeServer;
    private Headers initializeHeaders;
    private String initializeAnswer = "OK";
    private String initializeMethod;
    private String initializeBody;

    @Before
    public void startServer() throws Exception {
        initializeServer = HttpServer.create( new InetSocketAddress( 8111 ), 0 );
        initializeServer.createContext( "/", exchange -> {
            initializeBody = StreamReader.readStream(exchange.getRequestBody());
            initializeMethod = exchange.getRequestMethod();
            initializeHeaders = exchange.getRequestHeaders();
            exchange.sendResponseHeaders( 200, initializeAnswer.length() );
            exchange.getResponseBody().write( initializeAnswer.getBytes() );
            exchange.close();
        } );
        initializeServer.start();
        context.addServlet(ORInitializeServlet.class, "/initialize");
        server.start();
    }

    @After
    public void stopServer() {
        initializeServer.stop( 0 );
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

        assertThat(initializeMethod, equalTo("POST"));
    }

    @Test
    public void contentTypeIsJson() throws Exception {
        get("http://localhost:8888/initialize");

        assertThat(initializeHeaders.getFirst("content-type"), equalTo("application/json"));
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

        assertThat(initializeBody, equalTo(expected));
    }

    @Test
    public void sendBasicAuthHeader() throws Exception {
        String expected = "Basic " + Base64.getEncoder().encodeToString(
                ("this-basic-auth-username:this-basic-auth-password").getBytes());
        get("http://localhost:8888/initialize");

        assertThat(initializeHeaders.getFirst("Authorization"), equalTo(expected));
    }

    @Test
    public void propagatesReceivedValue() throws Exception {
        HttpResponse response = get("http://localhost:8888/initialize");

        assertThat(response.getBody(), equalTo("OK"));
    }


    public void returnsApplicationTicketWhenRanAgainstRealOr() throws Exception {
        HttpResponse response = get("http://localhost:8888/initialize");

        assertThat(response.getBody(), containsString("AppTicket"));
        assertThat(response.getContentType(), equalTo("application/json"));
    }


    public void manualCallAgainstRealOr() throws Exception {
        ORInitialize initialize = new ORInitialize();
        initialize.environment = new Environment();
        HttpResponse response = PostRequest.post(initialize.url(), initialize.headers(), initialize.body().getBytes());

        assertThat(response.getBody(), containsString("AppTicket"));
        assertThat(response.getContentType(), equalTo("application/json"));
    }
}
