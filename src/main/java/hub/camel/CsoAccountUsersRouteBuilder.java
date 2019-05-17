package hub.camel;

import hub.IsAuthorized;
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
public class CsoAccountUsersRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = Logger.getLogger(CsoAccountUsersRouteBuilder.class.getName());

    @Override
    public void configure() {
        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);

        from("direct:accountusers")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "accountusers call..."))
            .to("direct:isauthorized")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "is authorized call answer="+body);
                exchange.getOut().setBody(body);
            })
            .choice()
                .when(body().not().contains("NOT FOUND"))
                    .process(exchange -> {
                        LOGGER.log(Level.INFO, "authorized");
                        String authorizationResponse = exchange.getIn().getBody(String.class);
                        JSONObject jo = new JSONObject(authorizationResponse);
                        JSONObject envelope = (JSONObject) jo.get("soap:Envelope");
                        JSONObject body = (JSONObject) envelope.get("soap:Body");
                        JSONObject response = (JSONObject) body.get("ns2:isAuthorizedUserResponse");
                        JSONObject info = (JSONObject) response.get("return");
                        String clientId = (String) info.get("clientId");
                        String accountId = (String) info.get("accountId");
                        exchange.getProperties().put("clientId", clientId);

                        exchange.getOut().setBody(accountId, String.class);
                    })
                    .to("direct:csoaccount")
                    .process(exchange -> {
                        String body = exchange.getIn().getBody(String.class);
                        LOGGER.log(Level.INFO, "account info call answer="+body);
                        exchange.getOut().setBody(body);
                    })
                    .choice()
                        .when(body().not().contains("NOT FOUND"))
                            .process(exchange -> {
                                String accountResponse = exchange.getIn().getBody(String.class);
                                JSONObject jo = new JSONObject(accountResponse);
                                JSONObject envelope = (JSONObject) jo.get("soap:Envelope");
                                JSONObject body = (JSONObject) envelope.get("soap:Body");
                                JSONObject response = (JSONObject) body.get("ns2:getCsoClientProfilesResponse");
                                JSONObject info = (JSONObject) response.get("return");
                                JSONObject account = (JSONObject) info.get("account");
                                account.put("clientId", exchange.getProperties().get("clientId"));

                                exchange.getOut().setBody(jo.toString(), String.class);
                            })
        ;

    }
}
