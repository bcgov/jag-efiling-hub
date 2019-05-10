package hub;

import hub.helper.Environment;
import hub.helper.HttpResponse;
import hub.helper.PostRequest;
import hub.http.ORInitializeServlet;
import hub.support.HavingHubRunning;
import org.junit.Test;

import static hub.support.GetRequest.get;
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


    public void manualCall() throws Exception {
        ORInitialize initialize = new ORInitialize();
        initialize.environment = new Environment();
        HttpResponse response = PostRequest.post(initialize.url(), initialize.headers(), initialize.body().getBytes());

        assertThat(response.getBody(), containsString("AppTicket"));
        assertThat(response.getContentType(), equalTo("application/json"));
    }
}
