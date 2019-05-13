package hub;

import hub.helper.HttpResponse;
import hub.http.PingServlet;
import hub.support.HavingTestProperties;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static hub.support.GetRequest.get;
import static hub.support.Resource.bodyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PingTest extends HavingTestProperties {

    private Hub hub;

    @Before
    public void startHub() throws Exception {
        System.setProperty("OPENSHIFT_BUILD_COMMIT", "42isTheAnswer");
        hub = new Hub(8888);
        hub.start();
    }
    @After
    public void stopHub() throws Exception {
        hub.stop();
    }

    @Test
    public void returnsCommitHash() throws Exception {
        assertThat(bodyOf("http://localhost:8888/ping"), equalTo("42isTheAnswer"));
    }

    @Test
    public void resistsInternalErrors() throws Exception {
        PingServlet pingServlet = new PingServlet();
        hub.getServletContext().addServlet(new ServletHolder(pingServlet), "/broken-ping");

        HttpResponse response = get("http://localhost:8888/broken-ping");

        assertThat(response.getStatusCode(), equalTo(500));
        assertThat(response.getBody(), equalTo(""));
    }
}