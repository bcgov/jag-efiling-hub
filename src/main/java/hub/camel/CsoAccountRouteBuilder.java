package hub.camel;

import hub.CsoAccountInfo;
import hub.helper.Stringify;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.dataformat.xmljson.XmlJsonDataFormat;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
@Startup
@ContextName("cdi-context")
public class CsoAccountRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(CsoAccountRouteBuilder.class.getName());

    @Inject
    CsoAccountInfo csoAccountInfo;

    @Inject
    Stringify stringify;

    @Override
    public void configure() {
        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);

        from("direct:csoaccount")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "cso account call..."))
            .process(exchange -> {
                String accountId = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "accountId="+accountId);
                String message = stringify.soapMessage(csoAccountInfo.searchByAccountId(accountId));
                LOGGER.log(Level.INFO, "message="+message);
                exchange.getOut().setBody(message);
            })
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            .setHeader("Authorization", constant(csoAccountInfo.basicAuthorization()))
            .setHeader("SOAPAction", constant(csoAccountInfo.accountInfoSoapAction()))
            .to(csoAccountInfo.accountInfoEndpoint())
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "cso account call answer="+body);
                exchange.getOut().setBody(body);
            })
            .choice()
                .when(body().contains("<account/>"))
                    .setBody(constant("NOT FOUND"))
                .otherwise()
                    .marshal(xmlJsonFormat);

    }
}
