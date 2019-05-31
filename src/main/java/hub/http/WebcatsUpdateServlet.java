package hub.http;

import hub.helper.Stringify;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@WebServlet(name = "WebcatsUpdateServlet", urlPatterns = {"/updateDocument"}, loadOnStartup = 1)
public class WebcatsUpdateServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(WebcatsUpdateServlet.class.getName());

    @Inject
    @ContextName("cdi-context")
    private CamelContext context;

    @Inject
    Stringify stringify;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        try {
            InputStream inputStream = req.getInputStream();
            String info = stringify.inputStream(inputStream);

            String result = update(info);

            LOGGER.log(Level.INFO, result);
            res.setHeader(CONTENT_TYPE, "application/json");
            res.getOutputStream().print(result);
        } catch (Exception e) {
            res.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    protected String update(String info) {
        ProducerTemplate producer = context.createProducerTemplate();
        return producer.requestBody("direct:update", info, String.class);
    }
}
