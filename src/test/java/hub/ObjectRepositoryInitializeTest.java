package hub;

import hub.http.ORInitializeServlet;
import hub.support.HavingHubRunning;
import org.junit.Test;

import static hub.support.Resource.bodyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class ObjectRepositoryInitializeTest extends HavingHubRunning {

    @Test
    public void returnsApplicationTicket() throws Exception {
        context.addServlet(ORInitializeServlet.class, "/initialize");
        server.start();

        assertThat(bodyOf("http://localhost:8888/initialize"), containsString("AppTicket"));
    }

}