package hub.camel;

import hub.Bambora;
import hub.http.PaymentIncoming;
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
public class BamboraRouteBuilder extends RouteBuilder {

    @Inject
    Bambora bambora;

    private static final Logger LOGGER = Logger.getLogger(BamboraRouteBuilder.class.getName());

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
            .process(exchange -> {
                PaymentIncoming incoming = exchange.getIn().getBody(PaymentIncoming.class);
                exchange.getProperties().put("incoming", incoming);
                String tokenization = bambora.tokenizationMessage(incoming);
                LOGGER.log(Level.INFO, "tokenization="+tokenization);
                exchange.getOut().setBody(tokenization);
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .process(exchange -> LOGGER.log(Level.INFO, "tokenization call..."))
            .to(bambora.tokenizationUrl())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer of tokenization call="+answer);
                int tokenIndex = answer.indexOf("token");
                String token = answer.substring(tokenIndex + 8, tokenIndex + 8 + 40);
                LOGGER.log(Level.INFO, "token="+token);

                PaymentIncoming incoming = (PaymentIncoming) exchange.getProperties().get("incoming");
                String payment = bambora.paymentMessage(token, incoming.getAmount());
                LOGGER.log(Level.INFO, "payment="+payment);
                exchange.getOut().setBody(payment);
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setHeader("Authorization", constant("Passcode " + bambora.passCode()))
            .process(exchange -> LOGGER.log(Level.INFO, "payment call..."))
            .to(bambora.paymentUrl())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer of payment call="+answer);

                exchange.getOut().setBody(answer);
            });
    }
}
