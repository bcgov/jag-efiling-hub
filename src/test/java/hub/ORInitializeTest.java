package hub;

import hub.http.ORInitializeServlet;
import hub.support.GetRequest;
import hub.support.HavingHubRunning;
import hub.support.HttpResponse;
import org.junit.Test;

import static hub.support.GetRequest.get;
import static hub.support.Resource.bodyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ORInitializeTest extends HavingHubRunning {

    @Test
    public void returnsApplicationTicket() throws Exception {
        context.addServlet(ORInitializeServlet.class, "/initialize");
        server.start();
        HttpResponse response = get("http://localhost:8888/initialize");

        assertThat(response.getBody(), containsString("AppTicket"));
        assertThat(response.getContentType(), equalTo("application/json"));
    }

}
