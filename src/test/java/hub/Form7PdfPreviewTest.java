package hub;

import hub.helper.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static hub.support.GetRequest.get;
import static org.junit.Assert.assertTrue;

public class Form7PdfPreviewTest {

    private String filename = "form7-received.pdf";

    private Hub hub;

    @Before
    public void startHub() throws Exception {
        hub = new Hub(8888);
        hub.start();
    }
    @After
    public void stopHub() throws Exception {
        hub.stop();
    }

    public void returnsPdf() throws Exception {
        HttpResponse response = get("http://localhost:8888/preview");

        save(response.getBinaryBody());

        File file = new File(filename);
        assertTrue(file.exists());
        assertTrue(file.length() > 100 );
    }

    private void save(byte[] pdf) throws IOException {
        FileOutputStream file = new FileOutputStream (filename);
        file.write(pdf);
        file.flush();
        file.close();
    }

}
