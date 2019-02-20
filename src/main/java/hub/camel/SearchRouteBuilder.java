package hub.camel;

import hub.CsoSearch;
import hub.helper.Combine;
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
public class SearchRouteBuilder extends RouteBuilder {

    @Inject
    CsoSearch csoSearch;

    @Inject
    Stringify stringify;

    @Inject
    Combine combine;

    private static final Logger LOGGER = Logger.getLogger(SearchRouteBuilder.class.getName());

    @Override
    public void configure() {
        XmlJsonDataFormat xmlJsonFormat = new XmlJsonDataFormat();
        xmlJsonFormat.setEncoding("UTF-8");
        xmlJsonFormat.setForceTopLevelObject(true);
        xmlJsonFormat.setTrimSpaces(true);

        from("direct:search")
            .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    exception.printStackTrace();
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
            .end()
            .process(exchange -> LOGGER.log(Level.INFO, "search call..."))
            .process(exchange -> {
                String caseNumber = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "caseNumber="+caseNumber);
                String message = stringify.soapMessage(csoSearch.searchByCaseNumber(caseNumber));
                LOGGER.log(Level.INFO, "message="+message);
                exchange.getOut().setBody(message);
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            .setHeader("Authorization", constant(csoSearch.basicAuthorization()))
            .setHeader("SOAPAction", constant(csoSearch.searchByCaseNumberSoapAction()))
            .to(csoSearch.searchEndpoint())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer of first call="+answer);

                exchange.getOut().setBody(answer);
            })
            .to("direct:criminal-or-civil");

        from("direct:criminal-or-civil")
            .choice()
                .when(body().contains("<CaseType>Criminal</CaseType>"))
                    .process(exchange -> LOGGER.log(Level.INFO, "criminal case found"))
                    .marshal(xmlJsonFormat)
                .when(body().contains("<CaseType>Civil</CaseType>"))
                    .to("direct:civil")
                .otherwise()
                    .to("direct:not-found");

        from("direct:not-found")
            .process(exchange -> LOGGER.log(Level.INFO, "not found..."))
            .setBody(constant("NOT FOUND"));

        from("direct:civil")
            .process(exchange -> LOGGER.log(Level.INFO, "case basics call..."))
            .process(exchange -> {
                String caseId = csoSearch.extractCaseId(exchange.getIn().getBody(String.class));
                LOGGER.log(Level.INFO, "caseId="+caseId);
                exchange.getProperties().put("caseId", caseId);

                exchange.getOut().setBody(stringify.soapMessage(csoSearch.viewCaseBasics(caseId)));
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            .setHeader("Authorization", constant(csoSearch.basicAuthorization()))
            .setHeader("SOAPAction", constant(csoSearch.viewCaseBasicsSoapAction()))
            .to(csoSearch.searchEndpoint())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer of call="+answer);
                exchange.getProperties().put("caseBasicsAnswer", answer);

                exchange.getOut().setBody(answer);
            })
            .to("direct:civil-authorized-or-not");

        from("direct:civil-authorized-or-not")
            .choice()
                .when(body().contains("<Name>Publication Ban</Name>"))
                    .process(exchange -> LOGGER.log(Level.INFO, "publication ban found"))
                    .marshal(xmlJsonFormat)
                .when(body().contains("<Name>Family &amp; Restricted Files</Name>"))
                    .process(exchange -> LOGGER.log(Level.INFO, "restricted files found"))
                    .marshal(xmlJsonFormat)
                .when(body().contains("<HighLevelCategory>Family Law</HighLevelCategory>"))
                    .process(exchange -> LOGGER.log(Level.INFO, "familly law found"))
                    .marshal(xmlJsonFormat)
                .otherwise()
                    .to("direct:parties");

        from("direct:parties")
            .process(exchange -> LOGGER.log(Level.INFO, "case party call..."))
            .process(exchange -> {
                String caseId = (String) exchange.getProperties().get("caseId");
                LOGGER.log(Level.INFO, "caseId="+caseId);

                exchange.getOut().setBody(stringify.soapMessage(csoSearch.viewCaseParty(caseId)));
            })
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            .setHeader("Authorization", constant(csoSearch.basicAuthorization()))
            .setHeader("SOAPAction", constant(csoSearch.viewCasePartySoapAction()))
            .to(csoSearch.searchEndpoint())
            .process(exchange -> {
                String answer = exchange.getIn().getBody(String.class);
                LOGGER.log(Level.INFO, "answer of call="+answer);
                exchange.getProperties().put("casePartyAnswer", answer);
            })
            .to("direct:combine-answers");

        from("direct:combine-answers")
            .process(exchange -> {
                String caseBasicsAnswer = (String) exchange.getProperties().get("caseBasicsAnswer");
                String casePartyAnswer = (String) exchange.getProperties().get("casePartyAnswer");
                exchange.getOut().setBody(combine.answers(caseBasicsAnswer, casePartyAnswer));
            })
            .marshal(xmlJsonFormat);
    }
}
