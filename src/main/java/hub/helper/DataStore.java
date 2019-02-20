package hub.helper;

public class DataStore {

    private String caseId;
    private String casePartyAnswer;
    private String caseBasicsAnswer;

    public String combinedAnswers() {
        String caseBasics = bodyOf(caseBasicsAnswer);
        return casePartyAnswer.replaceAll("</soap:Body>", caseBasics + "</soap:Body>");
    }

    private String bodyOf(String answer) {
        int start = answer.indexOf("<soap:Body>");
        int end = answer.indexOf("</soap:Body>");

        return answer.substring(start + "<soap:Body>".length(), end);
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCasePartyAnswer(String casePartyAnswer) {
        this.casePartyAnswer = casePartyAnswer;
    }

    public void setCaseBasicsAnswer(String caseBasicsAnswer) {
        this.caseBasicsAnswer = caseBasicsAnswer;
    }
}
