package hub.camel;

import hub.Payment;
import hub.helper.Stringify;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
@Startup
@ContextName("cdi-context")
public class PaymentRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(PaymentRouteBuilder.class.getName());

    @Inject
    Payment payment;

    @Inject
    Stringify stringify;

    @Override
    public void configure() {

        from("direct:payment")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "payment call..."))
            .process(exchange -> {
                String userguid = (String) exchange.getProperties().get("userguid");
                LOGGER.log(Level.INFO, "userguid="+userguid);
                String message = stringify.soapMessage(payment.message(userguid));
                exchange.getOut().setBody(message);
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            .setHeader("Authorization", constant(payment.basicAuthorization()))
            .setHeader("SOAPAction", constant(payment.paymentSoapAction()))
            .to(payment.paymentEndpoint())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer payment="+answer);

                exchange.getOut().setBody(answer);
            })
        ;
    }
}
