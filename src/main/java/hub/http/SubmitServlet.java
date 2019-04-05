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
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@WebServlet(name = "SubmitServlet", urlPatterns = {"/submit"}, loadOnStartup = 1)
public class SubmitServlet extends HttpServlet {

    @Inject
    @ContextName("cdi-context")
    private CamelContext context;

    private static final Logger LOGGER = Logger.getLogger(SubmitServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        try {
            String body = req.getReader().lines().collect(Collectors.joining(lineSeparator()));
            ProducerTemplate producer = context.createProducerTemplate();
            String result = producer.requestBody("direct:submit", body, String.class);

            LOGGER.log(Level.INFO, result);
            res.getOutputStream().print(result);
        }
        catch (Exception e) {
            res.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

}

