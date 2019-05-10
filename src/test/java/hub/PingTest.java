package hub;

import hub.http.PingServlet;
import hub.support.HavingHubRunning;
import hub.helper.HttpResponse;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Before;
import org.junit.Test;

import static hub.support.GetRequest.get;
import static hub.support.Resource.bodyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PingTest extends HavingHubRunning {

    @Before
    public void setCommitHash() {
        System.setProperty("OPENSHIFT_BUILD_COMMIT", "42isTheAnswer");
    }

    @Test
    public void returnsCommitHash() throws Exception {
        context.addServlet(PingServlet.class, "/ping");
        server.start();

        assertThat(bodyOf("http://localhost:8888/ping"), equalTo("42isTheAnswer"));
    }

    @Test
    public void resistsInternalErrors() throws Exception {
        PingServlet pingServlet = new PingServlet();
        context.addServlet(new ServletHolder(pingServlet), "/ping");
        server.start();

        HttpResponse response = get("http://localhost:8888/ping");
        assertThat(response.getStatusCode(), equalTo(500));
        assertThat(response.getBody(), equalTo(""));
    }
}