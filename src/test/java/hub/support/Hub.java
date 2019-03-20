package hub.support;

import hub.http.BamboraServlet;
import hub.http.CsoAccountServlet;
import hub.http.PingServlet;
import hub.http.SearchServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;

public class Hub {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8888);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("src/main/resources");
        context.addEventListener(new org.jboss.weld.environment.servlet.Listener());
        server.setHandler(context);

        context.addServlet(SearchServlet.class, "/form7s");
        context.addServlet(BamboraServlet.class, "/payment");
        context.addServlet(CsoAccountServlet.class, "/account");

        server.start();
        server.join();
    }
}
