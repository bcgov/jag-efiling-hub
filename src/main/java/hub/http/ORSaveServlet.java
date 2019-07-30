package hub.http;

import hub.helper.Bytify;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

@WebServlet(name = "ORSaveServlet", urlPatterns = {"/save"}, loadOnStartup = 1)
public class ORSaveServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ORSaveServlet.class.getName());

    @Inject
    @ContextName("cdi-context")
    private CamelContext context;

    @Inject
    Bytify bytify;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) {
        try {
            InputStream inputStream = req.getInputStream();
            byte[] pdf = bytify.inputStream(inputStream);

            Map<String, Object> headers = new HashMap<>();
            headers.put("smgov_userguid", req.getHeader("smgov_userguid"));
            String result = save(pdf, headers);

            LOGGER.log(Level.INFO, result);
            if ("PAYMENT SERVICE UNAVAILABLE".equalsIgnoreCase(result)) {
                res.setStatus(500);
                res.setHeader(CONTENT_TYPE, "application/json");
                res.getOutputStream().print("{\"message\":\"Failed - Payment failed\"}");
            }
            else if (result.contains("\"resultCode\":\"1\"")) {
                res.setStatus(403);
                res.setHeader(CONTENT_TYPE, "application/json");
                JSONObject jo = new JSONObject(result);
                JSONObject container = (JSONObject) jo.get("return");
                String message = (String) container.get("resultMessage");
                res.getOutputStream().print("{\"message\":\""+message+"\"}");
            }
            else {
                res.setHeader(CONTENT_TYPE, "application/json");
                res.getOutputStream().print(result);
            }
        } catch (Exception e) {
            res.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    protected String save(byte[] pdf, Map<String, Object> headers) {
        ProducerTemplate producer = context.createProducerTemplate();
        return producer.requestBodyAndHeaders("direct:submit", pdf, headers, String.class);
    }
}
