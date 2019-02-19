package hub.helper;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DataStoreTest {

    @Test
    public void combinesAnswersInSoapBody() {
        DataStore store = new DataStore();
        store.casePartyAnswer = "<soap:Envelope><soap:Body>" +
                                    "<ViewCasePartyResponse>case-party-answer</ViewCasePartyResponse>" +
                                "</soap:Body></soap:Envelope>";
        store.caseBasicsAnswer = "<soap:Envelope><soap:Body>" +
                                    "<ViewCaseBasicsResponse>case-basics-answer</ViewCaseBasicsResponse>" +
                                "</soap:Body></soap:Envelope>";
        String expected =   "<soap:Envelope><soap:Body>" +
                                "<ViewCasePartyResponse>case-party-answer</ViewCasePartyResponse>" +
                                "<ViewCaseBasicsResponse>case-basics-answer</ViewCaseBasicsResponse>" +
                            "</soap:Body></soap:Envelope>";

        assertThat(store.combinedAnswers(), equalTo(expected));
    }
}
