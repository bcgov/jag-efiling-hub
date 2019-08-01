package hub.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.dataformat.xmljson.XmlJsonDataFormat;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;


@ApplicationScoped
@Startup
@ContextName("cdi-context")
public class SubmitRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(SubmitRouteBuilder.class.getName());

    @Override
    public void configure() {
        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);

        from("direct:submit")
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
                String data = (String) exchange.getIn().getHeaders().get("data");
                byte[] pdf = exchange.getIn().getBody(byte[].class);
                exchange.getProperties().put("pdf", pdf);
                exchange.getProperties().put("userguid", userguid);
                exchange.getProperties().put("data", data);
            })
            .to("direct:objectRepository")
            .to("direct:csoSaveFiling")
            .to("direct:webcatsUpdate")
            .marshal(xmlJsonFormat)
        ;
    }
}
