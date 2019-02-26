package hub.http;

import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.servlet.spec.HttpServletRequestImpl;
import org.apache.activemq.web.MessageServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "ActiveMqServlet", urlPatterns = {"/message/*"}, loadOnStartup = 1)
public class ActiveMqServlet extends MessageServlet {

    private static final Logger LOGGER = Logger.getLogger(ActiveMqServlet.class.getName());

    @Override
    public void init(ServletConfig config) throws ServletException {
        config.getServletContext().setInitParameter("org.apache.activemq.brokerURL", "vm://localhost");
        super.init(config);
    }

    @Override
    protected void doMessages(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request instanceof HttpServletRequestImpl) {
            ((HttpServletRequestImpl) request).getExchange()
                    .getAttachment(ServletRequestContext.ATTACHMENT_KEY).setAsyncSupported(true);
        }
        try {
            super.doMessages(request, response);
        }
        catch (Exception e) {
            response.setStatus(500);
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

}