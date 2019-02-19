package hub;

import com.sun.net.httpserver.HttpServer;
import hub.http.SearchServlet;
import hub.support.HavingHubRunning;
import hub.support.HttpResponse;
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
import static hub.support.StreamReader.readStream;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class Form7SearchTest extends HavingHubRunning {

    private static final Logger LOGGER = Logger.getLogger(Form7SearchTest.class.getName());

    private HttpServer cso;
    String searchAnswer;
    String basicsAnswer;
    String partiesAnswer;

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
        context.addServlet(SearchServlet.class, "/search");
        server.start();
        HttpResponse response = get("http://localhost:8888/search?caseNumber=unknown");

        assertThat(response.getStatusCode(), equalTo(404));
    }

    @Test
    public void returnsInfoAsJson() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basics();
        partiesAnswer = parties();
        context.addServlet(SearchServlet.class, "/search");
        server.start();
        HttpResponse response = get("http://localhost:8888/search?caseNumber=visible");

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
    }

    @Test
    public void resistsInternalErrors() throws Exception {
        SearchServlet searchServlet = new SearchServlet();
        context.addServlet(new ServletHolder(searchServlet), "/search");
        server.start();
        HttpResponse response = get("http://localhost:8888/search?caseNumber=unknown");

        assertThat(response.getStatusCode(), equalTo(500));
        assertThat(response.getBody(), equalTo(""));
    }

    @Test
    public void returnsBasicsInfoWhenCaseIsAuthorized() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basics();
        partiesAnswer = parties();
        context.addServlet(SearchServlet.class, "/search");
        server.start();
        String answer = bodyOf("http://localhost:8888/search?caseNumber=visible");

        assertThat(answer, not(containsString("SearchByCaseNumberResponse")));
        assertThat(answer, containsString("ViewCaseBasicsResponse"));
        assertThat(answer, containsString("ViewCasePartyResponse"));
    }

    @Test
    public void returnsOnlySearchResultWhenCaseIsCriminal() throws Exception {
        searchAnswer = searchResultCriminal();
        context.addServlet(SearchServlet.class, "/search");
        server.start();
        String answer = bodyOf("http://localhost:8888/search?caseNumber=criminal");

        assertThat(answer, containsString("SearchByCaseNumberResponse"));
        assertThat(answer, not(containsString("ViewCaseBasicsResponse")));
        assertThat(answer, not(containsString("ViewCasePartyResponse")));
    }

    @Test
    public void returnsOnlyBasicsWhenCaseIsBanned() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basicsBanned();
        context.addServlet(SearchServlet.class, "/search");
        server.start();
        String answer = bodyOf("http://localhost:8888/search?caseNumber=banned");

        assertThat(answer, not(containsString("SearchByCaseNumberResponse")));
        assertThat(answer, containsString("ViewCaseBasicsResponse"));
        assertThat(answer, not(containsString("ViewCasePartyResponse")));
    }

    @Test
    public void returnsOnlyBasicsWhenCaseIsFamillyRestricted() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basicsFamillyRestricted();
        context.addServlet(SearchServlet.class, "/search");
        server.start();
        String answer = bodyOf("http://localhost:8888/search?caseNumber=banned");

        assertThat(answer, not(containsString("SearchByCaseNumberResponse")));
        assertThat(answer, containsString("ViewCaseBasicsResponse"));
        assertThat(answer, not(containsString("ViewCasePartyResponse")));
    }

    @Test
    public void returnsOnlyBasicsWhenHighLevelCategoryIsFamillyLaw() throws Exception {
        searchAnswer = searchResultCivil();
        basicsAnswer = basicsFamillyLawCategory();
        context.addServlet(SearchServlet.class, "/search");
        server.start();
        String answer = bodyOf("http://localhost:8888/search?caseNumber=banned");

        assertThat(answer, not(containsString("SearchByCaseNumberResponse")));
        assertThat(answer, containsString("ViewCaseBasicsResponse"));
        assertThat(answer, not(containsString("ViewCasePartyResponse")));
    }
}