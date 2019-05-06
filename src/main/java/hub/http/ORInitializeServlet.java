package hub.http;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ORInitializeServlet", urlPatterns = {"/initialize"}, loadOnStartup = 1)
public class ORInitializeServlet extends HttpServlet {

    @Inject
    @ContextName("cdi-context")
    private CamelContext context;

    private static final Logger LOGGER = Logger.getLogger(ORInitializeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        try {
            ProducerTemplate producer = context.createProducerTemplate();
            String result = producer.requestBody("direct:initialize", null, String.class);
            LOGGER.log(Level.INFO, result);
            res.getOutputStream().print(result);
        } catch (Exception e) {
            res.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }
}