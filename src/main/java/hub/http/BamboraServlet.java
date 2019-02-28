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

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@WebServlet(name = "BamboraServlet", urlPatterns = {"/payment"}, loadOnStartup = 1)
public class BamboraServlet extends HttpServlet {

    @Inject
    @ContextName("cdi-context")
    private CamelContext context;

    private static final Logger LOGGER = Logger.getLogger(BamboraServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        try {
            PaymentIncoming message = new PaymentIncoming();
            message.setNumber(req.getParameter("number"));
            message.setCvd(req.getParameter("cvd"));
            message.setExpiry(req.getParameter("expiry"));
            ProducerTemplate producer = context.createProducerTemplate();
            String result = producer.requestBody("direct:payment", message, String.class);

            LOGGER.log(Level.INFO, result);
            res.getOutputStream().print(result);

            res.setHeader(CONTENT_TYPE, "application/json");
        }
        catch (Exception e) {
            res.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

}

