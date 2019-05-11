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

@WebServlet(name = "CsoAccountServlet", urlPatterns = {"/account"}, loadOnStartup = 1)
public class CsoAccountServlet extends HttpServlet {

    @Inject
    @ContextName("cdi-context")
    private CamelContext context;

    private static final Logger LOGGER = Logger.getLogger(CsoAccountServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        try {
            String accountId = req.getParameter("accountId");
            LOGGER.log(Level.INFO, accountId);
            if (accountId == null) {
                res.setHeader(CONTENT_TYPE, "text/plain");
                res.setStatus(400);
                res.getOutputStream().print("Bad Request");
            }
            else {
                ProducerTemplate producer = context.createProducerTemplate();
                String result = producer.requestBody("direct:csoaccount", accountId, String.class);

                LOGGER.log(Level.INFO, result);
                res.getOutputStream().print(result);

                res.setHeader(CONTENT_TYPE, "application/json");
                if ("NOT FOUND".equalsIgnoreCase(result)) {
                    res.setHeader(CONTENT_TYPE, "text/plain");
                    res.setStatus(404);
                }
                if ("SERVICE UNAVAILABLE".equalsIgnoreCase(result)) {
                    res.setHeader(CONTENT_TYPE, "text/plain");
                    res.setStatus(503);
                }
            }
        }
        catch (Exception e) {
            res.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

}

