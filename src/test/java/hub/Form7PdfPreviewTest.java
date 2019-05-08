package hub;

import hub.http.Form7PdfPreviewServlet;
import hub.http.ORInitializeServlet;
import hub.support.HavingHubRunning;
import hub.support.HttpResponse;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static hub.support.GetRequest.get;
import static hub.support.Resource.bodyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class Form7PdfPreviewTest extends HavingHubRunning {

    private String filename = "form7-received.pdf";

    @Test
    public void returnsPdf() throws Exception {
        context.addServlet(Form7PdfPreviewServlet.class, "/preview");
        server.start();
        HttpResponse response = get("http://localhost:8888/preview");

        save(response.getBinaryBody());

        File file = new File(filename);
        assertTrue(file.exists());
        assertTrue(file.length() > 40000 );
    }

    private void save(byte[] pdf) throws IOException {
        FileOutputStream file = new FileOutputStream (filename);
        file.write(pdf);
        file.flush();
        file.close();
    }

}