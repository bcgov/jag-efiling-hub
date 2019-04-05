package hub;

import com.sun.net.httpserver.HttpServer;
import hub.http.SearchServlet;
import hub.http.SubmitServlet;
import hub.support.HavingHubRunning;
import hub.support.HttpResponse;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static hub.support.GetRequest.get;
import static hub.support.PostRequest.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class Form2SubmitTest extends HavingHubRunning {

    private static final Logger LOGGER = Logger.getLogger(Form2SubmitTest.class.getName());

    private HttpServer cso;

    @Before
    public void startServer() throws Exception {
        cso = HttpServer.create( new InetSocketAddress( 8111 ), 0 );
        cso.createContext( "/", exchange -> {
            BufferedReader data = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            String body = data.lines().collect(Collectors.joining());
            String token = "<data:CaseNumber xmlns:data=\"data\">";
            int beginIndex = body.indexOf("<data:CaseNumber xmlns:data=\"data\">") + token.length();
            int endIndex = body.indexOf("</data:CaseNumber>");
            String answer = body.substring(beginIndex, endIndex);

            exchange.sendResponseHeaders( 200, answer.length() );
            exchange.getResponseBody().write( answer.getBytes() );
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
        System.setProperty("WEBCATS_ENDPOINT", "http4://localhost:8111");
        System.setProperty("WEBCATS_UPDATE_CASE_SOAP_ACTION", "update-case");
        System.setProperty("WEBCATS_API_NAMESPACE", "api");
        System.setProperty("WEBCATS_DATA_NAMESPACE", "data");
    }

    @Test
    public void returnsInfoAsJson() throws Exception {
        context.addServlet(SubmitServlet.class, "/submit");
        server.start();
        String body = "will be echoed";
        HttpResponse response = post("http://localhost:8888/submit", body);

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getBody(), equalTo("will be echoed"));
    }

    @Test
    public void resistsInternalErrors() throws Exception {
        SubmitServlet submitServlet = new SubmitServlet();
        context.addServlet(new ServletHolder(submitServlet), "/submit");
        server.start();
        HttpResponse response = post("http://localhost:8888/submit", "anything");

        assertThat(response.getStatusCode(), equalTo(500));
        assertThat(response.getBody(), equalTo(""));
    }
}