package hub.camel;

import hub.CsoAccountInfo;
import hub.IsAuthorized;
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
public class IsAuthorizedRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(IsAuthorizedRouteBuilder.class.getName());

    @Inject
    IsAuthorized isAuthorized;

    @Inject
    Stringify stringify;

    @Override
    public void configure() {
        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);

        from("direct:isauthorized")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "is authorized call..."))
            .process(exchange -> {
                String userguid = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "userguid="+userguid);
                String message = stringify.soapMessage(isAuthorized.byUserguid(userguid));
                LOGGER.log(Level.INFO, "message="+message);
                exchange.getOut().setBody(message);
            })
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            .setHeader("Authorization", constant(isAuthorized.basicAuthorization()))
            .setHeader("SOAPAction", constant(isAuthorized.isAuthorizedSoapAction()))
            .to(isAuthorized.isAuthorizedEndpoint())
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "is authorized call answer="+body);
                exchange.getOut().setBody(body);
            })
            .choice()
                .when(body().contains("<return/>"))
                    .setBody(constant("NOT FOUND"))
                .otherwise()
                    .marshal(xmlJsonFormat);

    }
}
