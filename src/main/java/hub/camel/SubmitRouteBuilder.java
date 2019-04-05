package hub.camel;

import hub.WebCatsUpdate;
import hub.helper.Stringify;
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
public class SubmitRouteBuilder extends RouteBuilder {

    @Inject
    WebCatsUpdate webCatsUpdate;

    @Inject
    Stringify stringify;

    private static final Logger LOGGER = Logger.getLogger(SubmitRouteBuilder.class.getName());

    @Override
    public void configure() {

        from("direct:submit")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "submit call..."))
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "body="+body);

                String caseNumber = body;

                String message = stringify.soapMessage(webCatsUpdate.updateCase(caseNumber));
                LOGGER.log(Level.INFO, "message="+message);
                exchange.getOut().setBody(message);
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            .setHeader("Authorization", constant(webCatsUpdate.basicAuthorization()))
            .setHeader("SOAPAction", constant(webCatsUpdate.updateCaseSoapAction()))
            .to(webCatsUpdate.webcatsEndpoint())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer submit="+answer);

                exchange.getOut().setBody(answer);
            });
    }
}
