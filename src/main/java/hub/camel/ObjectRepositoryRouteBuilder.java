package hub.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
@Startup
@ContextName("cdi-context")
public class ObjectRepositoryRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(ObjectRepositoryRouteBuilder.class.getName());

    @Override
    public void configure() {

        from("direct:objectRepository")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "object repository calls..."))
            .to("direct:initialize")
            .to("direct:create")
            .to("direct:changeOwner")
        ;
    }
}
