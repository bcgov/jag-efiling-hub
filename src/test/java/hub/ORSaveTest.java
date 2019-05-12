package hub;

import hub.helper.Environment;
import hub.helper.HttpResponse;
import hub.http.ORSaveServlet;
import hub.support.HavingHubRunning;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static hub.helper.PostRequest.post;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ORSaveTest extends HavingHubRunning {


    public void returnsGuidWhenRanAgainstRealOr() throws Exception {
        context.addServlet(ORSaveServlet.class, "/save");
        server.start();

        byte[] pdf = named("form2-1.pdf");
        assertThat(pdf.length, equalTo(22186));

        HttpResponse response = post("http://localhost:8888/save", pdf);

        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), containsString("Object_GUID"));
    }

    private byte[] named(String name) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(name).getFile());
        return Files.readAllBytes(file.toPath());
    }


    public void manualCallAgainsRealOr() throws Exception {
        ORInitialize initialize = new ORInitialize();
        initialize.environment = new Environment();
        HttpResponse response = post(initialize.url(), initialize.headers(), initialize.body().getBytes());

        assertThat(response.getStatusCode(), equalTo(200));
        JSONObject jo = new JSONObject(response.getBody());
        String ticket = (String) jo.get("AppTicket");

        ORSave save = new ORSave();
        save.environment = new Environment();
        response = post(save.url(ticket), save.headers(), named("form2-1.pdf"));

        assertThat(response.getStatusCode(), equalTo(200));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getBody(), containsString("Object_GUID"));
    }

}
