package hub;

import hub.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Hub {

    private Server server;
    private ServletContextHandler context;

    public Hub(int port) {
        server = new Server(port);
        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("src/main/resources");
        context.addEventListener(new org.jboss.weld.environment.servlet.Listener());
        server.setHandler(context);
    }

    public void start() throws Exception {
        context.addServlet(PingServlet.class, "/ping");
        context.addServlet(Form7PdfPreviewServlet.class, "/preview");
        context.addServlet(ORInitializeServlet.class, "/initialize");
        context.addServlet(ORSaveServlet.class, "/save");
        context.addServlet(Form7SearchServlet.class, "/form7s");
        context.addServlet(CsoAccountServlet.class, "/account");
        context.addServlet(IsAuthorizedServlet.class, "/isAuthorized");
        context.addServlet(CsoAccountUsersServlet.class, "/accountUsers");
        context.addServlet(WebcatsUpdateServlet.class, "/updateDocument");
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public static void main(String[] args) throws Exception {
        Hub hub = new Hub(8080);
        hub.start();
    }

    public ServletContextHandler getServletContext() {
        return context;
    }
}
