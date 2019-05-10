package hub.camel;

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
public class ORSaveRouteBuilder extends RouteBuilder {

    @Inject
    ORSave save;

    private static final Logger LOGGER = Logger.getLogger(ORSaveRouteBuilder.class.getName());

    @Override
    public void configure() {

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
                byte[] pdf = exchange.getIn().getBody(byte[].class);
                exchange.getProperties().put("pdf", pdf);
            })
            .to("direct:initialize")
            .process(exchange -> {
                LOGGER.log(Level.INFO, "AppTicket received");
                String initializeResponse = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, initializeResponse);
                JSONObject jo = new JSONObject(initializeResponse);
                String ticket = (String) jo.get("AppTicket");
                exchange.getProperties().put("ticket", ticket);
            })
            .process(exchange -> {
                Map<String, Object> headers = new HashMap<>();
                headers.put(Exchange.HTTP_METHOD, constant(POST));
                headers.put("Authorization", constant(save.basicAuthorization()));
                headers.put(Exchange.HTTP_QUERY, "?AppTicket=this-ticket&MimeType=application&MimeSubType=pdf&Filename=form2.pdf&RetentionPeriod=-1"
                        .replace("this-ticket", (String) exchange.getProperties().get("ticket")));
                byte[] pdf = (byte[]) exchange.getProperties().get("pdf");
                LOGGER.log(Level.INFO, headers.toString());
                LOGGER.log(Level.INFO, "file size = "+pdf.length);

                exchange.getOut().setHeaders(headers);
                exchange.getOut().setBody(pdf, byte[].class);
            })
            .to(save.camelUrl())
        ;
    }
}
