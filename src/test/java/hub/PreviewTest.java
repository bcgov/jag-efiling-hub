package hub;

import hub.support.HttpResponse;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static hub.support.PostRequest.post;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PreviewTest {

    @Test
    public void returnsInfoAsJson() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get("src/test/java/hub/form7.xml"));
        String encoded = Base64.getEncoder().encodeToString(bytes);
        String template = new String(Files.readAllBytes(Paths.get("src/test/java/hub/soap.xml")));
        String body = template.replaceAll("encoded", encoded);

        HttpResponse response = post("https://wsgw.dev.jag.gov.bc.ca:8443?CachedEnabled=False&RenderAtClient=False&PDFVersion=1.5", body);

        assertThat(response.getStatusCode(), equalTo(200));
    }

}