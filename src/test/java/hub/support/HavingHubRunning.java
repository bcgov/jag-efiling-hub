package hub.support;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;

public class HavingHubRunning {

    protected Server server;
    protected ServletContextHandler context;
    protected int port = 8888;

    @Before
    public void startHub() throws Exception {
        server = new Server(port);
        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("src/main/resources");
        context.addEventListener(new org.jboss.weld.environment.servlet.Listener());
        server.setHandler(context);
    }
    @After
    public void stopHub() throws Exception {
        server.stop();
    }
    @Before
    public void setDefaultProperties() {
        System.setProperty("COA_NAMESPACE", "http://hub.org");
        System.setProperty("CSO_NAMESPACE", "http://hub.org");
        System.setProperty("COA_SEARCH_ENDPOINT", "http4://localhost:8111");
        System.setProperty("CSO_ACCOUNT_INFO_ENDPOINT", "http4://localhost:8111");
    }
}