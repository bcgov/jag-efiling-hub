package hub.helper;

import javax.inject.Named;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Named
public class DataStore {


    public String caseId;
    public String casePartyAnswer;
    public String caseBasicsAnswer;

    public String combinedAnswers() {
        String caseBasics = bodyOf(caseBasicsAnswer);
        return casePartyAnswer.replaceAll("</soap:Body>", caseBasics + "</soap:Body>");
    }

    private String bodyOf(String answer) {
        int start = answer.indexOf("<soap:Body>");
        int end = answer.indexOf("</soap:Body>");

        return answer.substring(start + "<soap:Body>".length(), end);
    }
}
