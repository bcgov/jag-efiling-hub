package hub.http;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@WebServlet(name = "Form7PdfPreviewServlet", urlPatterns = {"/preview"}, loadOnStartup = 1)
public class Form7PdfPreviewServlet extends HttpServlet {

    @Inject
    @ContextName("cdi-context")
    private CamelContext context;

    private static final Logger LOGGER = Logger.getLogger(Form7PdfPreviewServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        try {
            ProducerTemplate producer = context.createProducerTemplate();
            byte[] result = producer.requestBody("direct:preview", null, byte[].class);
            res.setHeader(CONTENT_TYPE, "application/pdf");
            res.getOutputStream().write(result);
        } catch (Exception e) {
            res.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }
}