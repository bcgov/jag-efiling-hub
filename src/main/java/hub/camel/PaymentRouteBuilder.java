package hub.camel;

import hub.Payment;
import hub.XmlExtractor;
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
public class PaymentRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(PaymentRouteBuilder.class.getName());

    @Inject
    Payment payment;

    @Inject
    Stringify stringify;

    @Inject
    XmlExtractor extract;

    @Override
    public void configure() {

        from("direct:payment")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    String message = exception.getMessage();
                    LOGGER.log(Level.WARNING, message, exception);
                    exchange.getOut().setBody("PAYMENT FAILED: " + message);
                })
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "payment call..."))
            .process(exchange -> {
                String userguid = (String) exchange.getProperties().get("userguid");
                LOGGER.log(Level.INFO, "userguid="+userguid);
                String message = stringify.soapMessage(payment.message(userguid));
                LOGGER.log(Level.INFO, "payment message="+message);
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
            .choice()
                .when(body().contains("<resultCode>1</resultCode>"))
                    .process(exchange -> {
                        String body = exchange.getIn().getBody(String.class);
                        String message = extract.valueFromTag("resultMessage", body);
                        String answer = "<return><resultCode>1</resultCode><resultMessage>"+message+"</resultMessage></return>";
                        throw new Exception(answer);
                    })
                .otherwise()
                    .process(exchange -> {
                        String body = exchange.getIn().getBody(String.class);
                        String invoiceNumber = extract.valueFromTag("invoiceNo", body);
                        LOGGER.log(Level.INFO, "invoice number="+invoiceNumber);

                        exchange.getProperties().put("invoiceNumber", invoiceNumber);
                    })
        ;
    }
}
