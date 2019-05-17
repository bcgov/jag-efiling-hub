package hub;

import com.sun.net.httpserver.HttpServer;
import hub.helper.HttpResponse;
import hub.support.HavingTestProperties;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hub.support.Answers.*;
import static hub.support.GetRequest.get;
import static hub.support.Resource.bodyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CsoAccountUsersTest extends HavingTestProperties {

    private static final Logger LOGGER = Logger.getLogger(CsoAccountUsersTest.class.getName());

    private HttpServer cso;

    private String authorizationWillAnswer = isAuthorized();
    private String accountInfoWillAnswer = accountInfo();

    private Hub hub;

    @Before
    public void startHub() throws Exception {
        System.setProperty("CSO_NAMESPACE", "http://hub.org");
        System.setProperty("CSO_EXTENSION_ENDPOINT", "http4://localhost:8111");
        System.setProperty("CSO_ACCOUNT_INFO_SOAP_ACTION", "account-info-soap-action");
        System.setProperty("CSO_IS_AUTHORIZED_SOAP_ACTION", "is-authorized-soap-action");
        System.setProperty("CSO_USER", "this-user");
        System.setProperty("CSO_PASSWORD", "this-password");

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
            String action = exchange.getRequestHeaders().getFirst("SOAPAction");
            LOGGER.log(Level.INFO, "action = " + action);
            String willAnswer = "is-authorized-soap-action".equalsIgnoreCase(action) ? authorizationWillAnswer : accountInfoWillAnswer;
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

    @Test
    public void returnsInfoAsJson() throws Exception {
        HttpResponse response = get("http://localhost:8888/accountUsers?userguid=BA589724D21347DE81BAAEE02FA5D495");

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
    }

    @Test
    public void returnsCombinedInfo() throws Exception {
        String answer = bodyOf("http://localhost:8888/accountUsers?userguid=BA589724D21347DE81BAAEE02FA5D495");
        JSONObject jo = new JSONObject(answer);
        JSONObject envelope = (JSONObject) jo.get("soap:Envelope");
        JSONObject body = (JSONObject) envelope.get("soap:Body");
        JSONObject response = (JSONObject) body.get("ns2:getCsoClientProfilesResponse");
        JSONObject info = (JSONObject) response.get("return");
        JSONObject account = (JSONObject) info.get("account");

        assertThat(account.toString(), equalTo("{\"accountId\":\"1304\",\"clientId\":\"1801\",\"accountName\":\"Minnie Mouse.\"}"));
    }

    @Test
    public void resistsBadRequest() throws Exception {
        HttpResponse response = get("http://localhost:8888/accountUsers");

        assertThat(response.getStatusCode(), equalTo(400));
        assertThat(response.getBody(), equalTo("Bad Request"));
    }

    @Test
    public void resistsUnknown() throws Exception {
        authorizationWillAnswer = isNotAuthorized();
        HttpResponse response = get("http://localhost:8888/accountUsers?userguid=unknown");

        assertThat(response.getStatusCode(), equalTo(404));
        assertThat(response.getBody(), equalTo("NOT FOUND"));
    }
}
