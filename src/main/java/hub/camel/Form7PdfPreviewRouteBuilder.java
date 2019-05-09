package hub.camel;

import hub.FormPdfPreview;
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
public class Form7PdfPreviewRouteBuilder extends RouteBuilder {

    @Inject
    FormPdfPreview formPdfPreview;

    private static final Logger LOGGER = Logger.getLogger(Form7PdfPreviewRouteBuilder.class.getName());

    @Override
    public void configure() {

        from("direct:preview")
                .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
                    LOGGER.log(Level.WARNING, exception.getMessage(), exception);
                })
                .setBody(constant("SERVICE UNAVAILABLE"))
                .end()
                .process(exchange -> LOGGER.log(Level.INFO, "preview call..."))
                .process(exchange -> {
                    LOGGER.log(Level.INFO, new String(formPdfPreview.sampleData()));
                    LOGGER.log(Level.INFO, formPdfPreview.endpoint());
                    LOGGER.log(Level.INFO, formPdfPreview.username());
                    LOGGER.log(Level.INFO, formPdfPreview.password());
                    LOGGER.log(Level.INFO, formPdfPreview.templateName());
                    LOGGER.log(Level.INFO, formPdfPreview.serverContext());
                    LOGGER.log(Level.INFO, formPdfPreview.options());
                })
                .process(exchange -> {
                    byte[] pdf = formPdfPreview.renderPdf(formPdfPreview.templateName(), formPdfPreview.sampleData(), false);

                    exchange.getOut().setBody(pdf, byte[].class);
                });
    }
}
