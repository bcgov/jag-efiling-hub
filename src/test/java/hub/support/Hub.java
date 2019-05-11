package hub.support;

import hub.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Hub {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("src/main/resources");
        context.addEventListener(new org.jboss.weld.environment.servlet.Listener());
        server.setHandler(context);

        context.addServlet(PingServlet.class, "/ping");
        context.addServlet(Form7PdfPreviewServlet.class, "/preview");
        context.addServlet(ORInitializeServlet.class, "/initialize");
        context.addServlet(ORSaveServlet.class, "/save");
        context.addServlet(Form7SearchServlet.class, "/form7s");

        server.start();
        server.join();
    }
}
