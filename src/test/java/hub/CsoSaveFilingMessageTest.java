package hub;

import hub.helper.Environment;
import hub.helper.Stringify;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class CsoSaveFilingMessageTest {

    private CsoSaveFiling csoSaveFiling;

    @Before
    public void createInstance() {
        csoSaveFiling = new CsoSaveFiling();
        csoSaveFiling.stringify = new Stringify();
        csoSaveFiling.extract = new XmlExtractor();
        csoSaveFiling.environment = new Environment();
        csoSaveFiling.clock = new Clock();
    }

    @Test
    public void populatesFileNumber() throws IOException {
        String message = csoSaveFiling.message(
                "this-userguid",
                "this-invoice-number",
                "this-service-id",
                "this-objectguid",
                "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":\"123\", \"clientId\":\"321\"},\"authorizations\":[]}"
        );
        assertThat(message, containsString("<courtFileNumber>CA12345</courtFileNumber>"));
    }

    @Test
    public void populatesWithoutNotificationByDefault() throws IOException {
        String message = csoSaveFiling.message(
                "this-userguid",
                "this-invoice-number",
                "this-service-id",
                "this-objectguid",
                "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":false,\"selectedContactIndex\":0,\"account\":{\"accountId\":\"123\", \"clientId\":\"321\"},\"authorizations\":[]}"
        );
        assertThat(message, containsString("<notificationEmail></notificationEmail>"));
        assertThat(message, containsString("<eNotification>false</eNotification>"));
    }

    @Test
    public void populatesNotificationWhenProvided() throws IOException {
        String message = csoSaveFiling.message(
                "this-userguid",
                "this-invoice-number",
                "this-service-id",
                "this-objectguid",
                "{\"formSevenNumber\":\"CA12345\",\"appellants\":[{\"name\":\"Max FREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":0},{\"name\":\"MAX SUPERFREE\",\"address\":{\"addressLine1\":\"123 - Nice Street\",\"addressLine2\":\"B201\",\"city\":\"Here\",\"postalCode\":\"V1V 0M0\",\"province\":\"British Columbia\"},\"id\":1}],\"respondents\":[{\"name\":\"Bob NOT SO FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\",\"phone\":\"7783501234\",\"email\":\"me@here.net\"},\"id\":0,\"selected\":true},{\"name\":\"BOB NOT FREE\",\"address\":{\"addressLine1\":\"456 - Near Street\",\"addressLine2\":\"A2\",\"city\":\"Faraway\",\"postalCode\":\"V2V 0M0\",\"province\":\"British Columbia\"},\"id\":1,\"selected\":true}],\"useServiceEmail\":false,\"sendNotifications\":true,\"selectedContactIndex\":0,\"account\":{\"accountId\":\"123\", \"clientId\":\"321\"},\"authorizations\":[]}"
        );
        assertThat(message, containsString("<notificationEmail>me@here.net</notificationEmail>"));
        assertThat(message, containsString("<eNotification>true</eNotification>"));
    }
}
