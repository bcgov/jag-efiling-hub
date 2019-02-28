package hub;

import com.sun.net.httpserver.HttpServer;
import hub.http.BamboraServlet;
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
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class BamboraTest extends HavingHubRunning {

    @Test
    public void returnsInfoAsJson() throws Exception {
        context.addServlet(BamboraServlet.class, "/payment");
        server.start();
        HttpResponse response = get("http://localhost:8888/payment?amount=15.45&number=4030000010001234&cvd=123&expiry=1022");

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), containsString("Approved"));
    }

}