package hub.support;

import hub.http.PingServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class Hub {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8888);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("src/main/resources");
        context.addEventListener(new org.jboss.weld.environment.servlet.Listener());
        server.setHandler(context);

        context.addServlet(PingServlet.class, "/ping");

        server.start();
        server.join();
    }
}
