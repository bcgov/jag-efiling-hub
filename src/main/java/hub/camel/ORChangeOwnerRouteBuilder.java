package hub.camel;

import hub.ORChangeOwner;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
@Startup
@ContextName("cdi-context")
public class ORChangeOwnerRouteBuilder extends RouteBuilder {

    @Inject
    ORChangeOwner changeOwner;

    private static final Logger LOGGER = Logger.getLogger(ORChangeOwnerRouteBuilder.class.getName());

    @Override
    public void configure() {

        from("direct:changeOwner")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "changeOwner call..."))
            .process(exchange -> {
                String ticket = (String) exchange.getProperties().get("ticket");
                String guid = (String) exchange.getProperties().get("guid");
                String message = changeOwner.body(ticket, guid);
                LOGGER.log(Level.INFO, "message="+message);
                exchange.getOut().setBody(message);
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setHeader("Authorization", constant(changeOwner.basicAuthorization()))
            .to(changeOwner.camelUrl())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer changeOwner="+answer);

                exchange.getOut().setBody(answer);
            })
        ;
    }
}
