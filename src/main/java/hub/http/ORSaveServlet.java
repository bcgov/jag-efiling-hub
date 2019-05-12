package hub.http;

import hub.ORInitialize;
import hub.ORSave;
import hub.helper.HttpResponse;
import hub.helper.StreamReader;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hub.helper.PostRequest.post;
import static hub.helper.StreamReader.readStreamAsbytes;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@WebServlet(name = "ORSaveServlet", urlPatterns = {"/save"}, loadOnStartup = 1)
public class ORSaveServlet extends HttpServlet {

    @Inject
    @ContextName("cdi-context")
    private CamelContext context;

    private static final Logger LOGGER = Logger.getLogger(ORSaveServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        try {
            InputStream inputStream = req.getInputStream();
            byte[] pdf = readStreamAsbytes(inputStream);

            String result = save(pdf);

            LOGGER.log(Level.INFO, result);
            res.setHeader(CONTENT_TYPE, "application/json");
            res.getOutputStream().print(result);
        } catch (Exception e) {
            res.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    protected String save(byte[] pdf) {
        ProducerTemplate producer = context.createProducerTemplate();
        return producer.requestBody("direct:save", pdf, String.class);
    }
}
