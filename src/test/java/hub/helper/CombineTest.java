package hub.helper;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CombineTest {

    @Test
    public void combinesAnswersInSoapBody() {
        String casePartyAnswer = "<soap:Envelope><soap:Body>" +
                                    "<ViewCasePartyResponse>case-party-answer</ViewCasePartyResponse>" +
                                "</soap:Body></soap:Envelope>";
        String caseBasicsAnswer = "<soap:Envelope><soap:Body>" +
                                    "<ViewCaseBasicsResponse>case-basics-answer</ViewCaseBasicsResponse>" +
                                "</soap:Body></soap:Envelope>";
        String expected =   "<soap:Envelope><soap:Body>" +
                                "<ViewCasePartyResponse>case-party-answer</ViewCasePartyResponse>" +
                                "<ViewCaseBasicsResponse>case-basics-answer</ViewCaseBasicsResponse>" +
                            "</soap:Body></soap:Envelope>";

        assertThat(new Combine().answers(caseBasicsAnswer, casePartyAnswer), equalTo(expected));
    }
}
