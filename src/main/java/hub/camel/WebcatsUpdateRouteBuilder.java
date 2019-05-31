package hub.camel;

import hub.WebcatsUpdate;
import hub.helper.Stringify;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.dataformat.xmljson.XmlJsonDataFormat;
import org.json.JSONObject;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
@Startup
@ContextName("cdi-context")
public class WebcatsUpdateRouteBuilder extends RouteBuilder {

    @Inject
    WebcatsUpdate webcatsUpdate;

    @Inject
    Stringify stringify;

    private static final Logger LOGGER = Logger.getLogger(WebcatsUpdateRouteBuilder.class.getName());

    @Override
    public void configure() {
        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);

        from("direct:update")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "update call..."))
            .process(exchange -> {
                String info = exchange.getIn().getBody(String.class);
                JSONObject jo = new JSONObject(info);
                String caseNumber = (String) jo.get(("caseNumber"));
                String guid = (String) jo.get(("guid"));

                LOGGER.log(Level.INFO, "caseNumber="+caseNumber);
                String message = stringify.soapMessage(webcatsUpdate.update(caseNumber, guid));
                LOGGER.log(Level.INFO, "message="+message);
                exchange.getOut().setBody(message);
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            .setHeader("Authorization", constant(webcatsUpdate.basicAuthorization()))
            .setHeader("SOAPAction", constant(webcatsUpdate.updateSoapAction()))
            .to(webcatsUpdate.endpoint())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer of update="+answer);

                exchange.getOut().setBody(answer);
            })
            .marshal(xmlJsonFormat)
        ;
    }
}
