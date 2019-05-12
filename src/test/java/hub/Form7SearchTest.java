package hub;

import com.sun.net.httpserver.HttpServer;
import hub.helper.HttpResponse;
import hub.http.Form7SearchServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hub.support.Answers.*;
import static hub.support.GetRequest.get;
import static hub.support.Resource.bodyOf;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class Form7SearchTest {

    private static final Logger LOGGER = Logger.getLogger(Form7SearchTest.class.getName());

    private HttpServer cso;
    String searchAnswer;
    String basicsAnswer;
    String partiesAnswer;

    private Hub hub;

    @Before
    public void startHub() throws Exception {
        System.setProperty("COA_NAMESPACE", "http://hub.org");
        System.setProperty("COA_SEARCH_ENDPOINT", "http4://localhost:8111");

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
            String answer = "";
            String action = exchange.getRequestHeaders().getFirst("SOAPAction");
            if ("first-call".equalsIgnoreCase(action)) {
                answer = searchAnswer;
            }
            if ("second-call".equalsIgnoreCase(action)) {
                answer = basicsAnswer;
                LOGGER.log(Level.INFO, "answer=" + answer);
            }
            if ("third-call".equalsIgnoreCase(action)) {
                answer = partiesAnswer;
            }
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
        System.setProperty("COA_SEARCH_ENDPOINT", "http4://localhost:8111");
        System.setProperty("COA_SEARCH_SOAP_ACTION", "first-call");
        System.setProperty("COA_VIEW_CASE_BASICS_SOAP_ACTION", "second-call");
        System.setProperty("COA_VIEW_CASE_PARTY_SOAP_ACTION", "third-call");
    }

    @Test
    public void returns404WhenNotFound() throws Exception {
        searchAnswer = "";
        HttpResponse response = get("http://localhost:8888/form7s?caseNumber=unknown");

        assertThat(response.getStatusCode(), equalTo(404));
    }

    @Test
    public void returnsInfoAsJson() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basics();
        partiesAnswer = parties();
        HttpResponse response = get("http://localhost:8888/form7s?caseNumber=visible");

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
    }

    @Test
    public void resistsInternalErrors() throws Exception {
        Form7SearchServlet form7SearchServlet = new Form7SearchServlet();
        hub.getServletContext().addServlet(new ServletHolder(form7SearchServlet), "/broken-search");
        HttpResponse response = get("http://localhost:8888/broken-search?caseNumber=unknown");

        assertThat(response.getStatusCode(), equalTo(500));
        assertThat(response.getBody(), equalTo(""));
    }

    @Test
    public void returnsBasicsInfoWhenCaseIsAuthorized() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basics();
        partiesAnswer = parties();
        String answer = bodyOf("http://localhost:8888/form7s?caseNumber=visible");

        assertThat(answer, not(containsString("SearchByCaseNumberResponse")));
        assertThat(answer, containsString("ViewCaseBasicsResponse"));
        assertThat(answer, containsString("ViewCasePartyResponse"));
    }

    @Test
    public void returnsOnlySearchResultWhenCaseIsCriminal() throws Exception {
        searchAnswer = searchResultCriminal();
        String answer = bodyOf("http://localhost:8888/form7s?caseNumber=criminal");

        assertThat(answer, containsString("SearchByCaseNumberResponse"));
        assertThat(answer, not(containsString("ViewCaseBasicsResponse")));
        assertThat(answer, not(containsString("ViewCasePartyResponse")));
    }

    @Test
    public void returnsOnlyBasicsWhenCaseIsBanned() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basicsBanned();
        String answer = bodyOf("http://localhost:8888/form7s?caseNumber=banned");

        assertThat(answer, not(containsString("SearchByCaseNumberResponse")));
        assertThat(answer, containsString("ViewCaseBasicsResponse"));
        assertThat(answer, not(containsString("ViewCasePartyResponse")));
    }

    @Test
    public void returnsOnlyBasicsWhenCaseIsFamillyRestricted() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basicsFamillyRestricted();
        String answer = bodyOf("http://localhost:8888/form7s?caseNumber=banned");

        assertThat(answer, not(containsString("SearchByCaseNumberResponse")));
        assertThat(answer, containsString("ViewCaseBasicsResponse"));
        assertThat(answer, not(containsString("ViewCasePartyResponse")));
    }

    @Test
    public void returnsOnlyBasicsWhenHighLevelCategoryIsFamillyLaw() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basicsFamillyLawCategory();
        String answer = bodyOf("http://localhost:8888/form7s?caseNumber=banned");

        assertThat(answer, not(containsString("SearchByCaseNumberResponse")));
        assertThat(answer, containsString("ViewCaseBasicsResponse"));
        assertThat(answer, not(containsString("ViewCasePartyResponse")));
    }
}
