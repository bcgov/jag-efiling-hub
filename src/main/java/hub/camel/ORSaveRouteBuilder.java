package hub.camel;

import hub.ORSave;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.dataformat.xmljson.XmlJsonDataFormat;
import org.json.JSONObject;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.camel.component.http4.HttpMethods.POST;


@ApplicationScoped
@Startup
@ContextName("cdi-context")
public class ORSaveRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(ORSaveRouteBuilder.class.getName());

    @Override
    public void configure() {
        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);

        from("direct:save")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> {
                String userguid = (String) exchange.getIn().getHeaders().get("smgov_userguid");
                byte[] pdf = exchange.getIn().getBody(byte[].class);
                exchange.getProperties().put("pdf", pdf);
                exchange.getProperties().put("userguid", userguid);
            })
            .to("direct:initialize")
            .to("direct:create")
            .to("direct:changeOwner")
            .to("direct:payment")
            .marshal(xmlJsonFormat)
        ;
    }
}
