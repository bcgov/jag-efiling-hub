package hub.helper;

import javax.inject.Named;

@Named
public class Combine {

    public String answers(String caseBasicsAnswer, String casePartyAnswer) {
        String caseBasics = bodyOf(caseBasicsAnswer);
        return casePartyAnswer.replaceAll("</soap:Body>", caseBasics + "</soap:Body>");
    }

    private String bodyOf(String answer) {
        int start = answer.indexOf("<soap:Body>");
        int end = answer.indexOf("</soap:Body>");

        return answer.substring(start + "<soap:Body>".length(), end);
    }

}
