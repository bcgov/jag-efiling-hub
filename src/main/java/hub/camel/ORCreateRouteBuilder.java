package hub.camel;

import hub.ORInitialize;
import hub.ORSave;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
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
public class ORCreateRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(ORCreateRouteBuilder.class.getName());

    @Inject
    ORSave save;

    @Override
    public void configure() {

        from("direct:create")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "create call..."))
            .process(exchange -> {
                Map<String, Object> headers = new HashMap<>();
                headers.put(Exchange.HTTP_METHOD, constant(POST));
                headers.put("Authorization", save.basicAuthorization());
                headers.put(Exchange.HTTP_QUERY, "AppTicket=this-ticket&MimeType=application&MimeSubType=pdf&Filename=form2.pdf&RetentionPeriod=-1"
                        .replace("this-ticket", (String) exchange.getProperties().get("ticket")));
                byte[] pdf = (byte[]) exchange.getProperties().get("pdf");
                LOGGER.log(Level.INFO, headers.toString());
                LOGGER.log(Level.INFO, "file size = "+pdf.length);

                exchange.getOut().setHeaders(headers);
                exchange.getOut().setBody(pdf, byte[].class);
            })
            .to(save.camelUrl())
            .process(exchange -> {
                String saveResponse = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, saveResponse);
                JSONObject jo = new JSONObject(saveResponse);
                String guid = (String) jo.get("Object_GUID");
                LOGGER.log(Level.INFO, "GUID received: " + guid);

                exchange.getProperties().put("objectguid", guid);
            })
        ;
    }
}
