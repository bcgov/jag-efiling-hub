package hub.support;

public class Answers {

    public static String basicsFamillyRestricted() {
        return "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<ViewCaseBasicsResponse>" +
                "<ViewCaseBasicsResult>" +
                "<SecurityFlags><SecurityFlag><Name><Name>Family &amp; Restricted Files</Name></Name></SecurityFlag></SecurityFlags>" +
                "</ViewCaseBasicsResult>" +
                "</ViewCaseBasicsResponse>" +
                "</soap:Body></soap:Envelope>";
    }

    public static String basicsFamillyLawCategory() {
        return "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<ViewCaseBasicsResponse>" +
                "<ViewCaseBasicsResult>" +
                "<HighLevelCategory>Family Law</HighLevelCategory>" +
                "</ViewCaseBasicsResult>" +
                "</ViewCaseBasicsResponse>" +
                "</soap:Body></soap:Envelope>";
    }

    public static String basicsBanned() {
        return "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<ViewCaseBasicsResponse>" +
                "<ViewCaseBasicsResult>" +
                "<SecurityFlags><SecurityFlag><Name>Publication Ban</Name></SecurityFlag></SecurityFlags>" +
                "</ViewCaseBasicsResult>" +
                "</ViewCaseBasicsResponse>" +
                "</soap:Body></soap:Envelope>";
    }

    public static String basics() {
        return "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<ViewCaseBasicsResponse>anything</ViewCaseBasicsResponse>" +
                "</soap:Body></soap:Envelope>";
    }

    public static String parties() {
        return "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<ViewCasePartyResponse>anything</ViewCasePartyResponse>" +
                "</soap:Body></soap:Envelope>";
    }

    public static String searchResultCriminal() {
        return "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<SearchByCaseNumberResponse>" +
                "<SearchByCaseNumberResult>" +
                "<CaseId>12345</CaseId><CaseType>Criminal</CaseType>" +
                "</SearchByCaseNumberResult></SearchByCaseNumberResponse></soap:Body></soap:Envelope>";
    }

    public static String searchResultCivil() {
        return "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<SearchByCaseNumberResponse>" +
                "<SearchByCaseNumberResult>" +
                "<CaseId>12345</CaseId><CaseType>Civil</CaseType>" +
                "</SearchByCaseNumberResult></SearchByCaseNumberResponse></soap:Body></soap:Envelope>";
    }

}
