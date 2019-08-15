package hub;

import hub.helper.Environment;
import hub.helper.Stringify;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.logging.Logger;

@Named
public class CsoSaveFiling {

    private static final Logger LOGGER = Logger.getLogger(CsoSaveFiling.class.getName());

    @Inject
    Environment environment;

    @Inject
    Stringify stringify;

    @Inject
    XmlExtractor extract;

    @Inject
    Clock clock;

    public String user() {
        return environment.getValue("CSO_USER");
    }

    public String password() {
        return environment.getValue("CSO_PASSWORD");
    }

    public String basicAuthorization() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (this.user() + ":" + this.password()).getBytes());
    }

    public String endpoint() {
        return environment.getValue("CSO_EXTENSION_ENDPOINT");
    }

    public String soapAction() {
        return environment.getValue("CSO_SAVE_FILING_SOAP_ACTION");
    }

    public String message(String userguid, String invoiceNumber, String serviceId, String objectguid, String data) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("templates/cso-save-filing.xml");
        String template = stringify.inputStream(inputStream);

        JSONObject jo = new JSONObject(data);
        String courtFileNumber = (String) jo.get(("formSevenNumber"));

        String partyTemplate = extract.outerTag("parties", template);
        String parties = buildParties(jo, partyTemplate);

        String userAccessTemplate = extract.outerTag("userAccess", template);
        String accesses = buildAccesses(jo, userAccessTemplate);

        return template
                .replace("<userguid>?</userguid>", "<userguid>"+userguid+"</userguid>")
                .replace("<bcolUserId>?</bcolUserId>", "<bcolUserId></bcolUserId>")
                .replace("<bcolSessionKey>?</bcolSessionKey>", "<bcolSessionKey></bcolSessionKey>")
                .replace("<bcolUniqueId>?</bcolUniqueId>", "<bcolUniqueId></bcolUniqueId>")

                .replace("<cfcsa>?</cfcsa>", "<cfcsa>false</cfcsa>")
                .replace("<classCd>?</classCd>", "<classCd>O</classCd>")
                .replace("<clientRefNo>?</clientRefNo>", "<clientRefNo></clientRefNo>")
                .replace("<comments>?</comments>", "<comments></comments>")
                .replace("<courtFileNumber>?</courtFileNumber>", "<courtFileNumber>"+courtFileNumber+"</courtFileNumber>")
                .replace("<divisionCd>?</divisionCd>", "<divisionCd>I</divisionCd>")

                .replace("<existingFile>?</existingFile>", "<existingFile>false</existingFile>")
                .replace("<indigent>?</indigent>", "<indigent>false</indigent>")
                .replace("<invoiceNo>?</invoiceNo>", "<invoiceNo>"+invoiceNumber+"</invoiceNo>")
                .replace("<levelCd>?</levelCd>", "<levelCd>A</levelCd>")
                .replace("<locationCd>?</locationCd>", "<locationCd>COA</locationCd>")
                .replace("<notificationEmail>?</notificationEmail>", "<notificationEmail></notificationEmail>")
                .replace("<eNotification>?</eNotification>", "<eNotification>false</eNotification>")
                .replace("<por>?</por>", "<por>false</por>")
                .replace("<prevFileNumber>?</prevFileNumber>", "<prevFileNumber></prevFileNumber>")
                .replace("<processingComplete>?</processingComplete>", "<processingComplete>true</processingComplete>")
                .replace("<resubmission>?</resubmission>", "<resubmission>false</resubmission>")
                .replace("<rush>?</rush>", "<rush>false</rush>")
                .replace("<serviceId>?</serviceId>", "<serviceId>"+serviceId+"</serviceId>")
                .replace("<submittedDtm>?</submittedDtm>", "<submittedDtm>"+clock.nowAsString()+"</submittedDtm>")

                .replace("<objectGUID>?</objectGUID>", "<objectGUID>"+objectguid+"</objectGUID>")
                .replace("<documentDescriptionTxt>?</documentDescriptionTxt>", "<documentDescriptionTxt></documentDescriptionTxt>")
                .replace("<documentStatusTypeCd>?</documentStatusTypeCd>", "<documentStatusTypeCd>FILE</documentStatusTypeCd>")
                .replace("<documentSubTypeCd>?</documentSubTypeCd>", "<documentSubTypeCd>ODOC</documentSubTypeCd>")
                .replace("<documentTypeCd>?</documentTypeCd>", "<documentTypeCd>NAA</documentTypeCd>")
                .replace("<feeExempt>?</feeExempt>", "<feeExempt>false</feeExempt>")
                .replace("<filenameTxt>?</filenameTxt>", "<filenameTxt>NoticeOfAppeal</filenameTxt>")
                .replace("<initiating>?</initiating>", "<initiating>false</initiating>")
                .replace("<orderDocument>?</orderDocument>", "<orderDocument>false</orderDocument>")
                .replace("<statusDtm>?</statusDtm>", "<statusDtm>"+clock.nowAsString()+"</statusDtm>")
                .replace("<uploadStateCd>?</uploadStateCd>", "<uploadStateCd>CMPL</uploadStateCd>")
                .replace("<uploadedToApplicationCd>?</uploadedToApplicationCd>", "<uploadedToApplicationCd>WEBCATS</uploadedToApplicationCd>")

                .replace(partyTemplate, parties)
                .replace(userAccessTemplate, accesses)

                ;
    }

    private String buildAccesses(JSONObject jo, String template) {
        String accesses = "";

        JSONObject account = (JSONObject) jo.get("account");
        String accountId = environment.getValue("OVERWRITE_ACCOUNT_ID_WITH_THIS_VALUE");
        if (accountId == null || accountId.trim().length() == 0) {
            accountId = account.get("accountId").toString();
        }
        String clientId = environment.getValue("OVERWRITE_CLIENT_ID_WITH_THIS_VALUE");
        if (clientId == null || clientId.trim().length() == 0) {
            clientId = account.get("clientId").toString();
        }

        JSONArray authorizations = (JSONArray) jo.get("authorizations");

        String contribution = new String(template);
        contribution = contribution
                        .replace("<accountId>?</accountId>","<accountId>"+ accountId +"</accountId>")
                        .replace("<clientId>?</clientId>","<clientId>"+ clientId +"</clientId>")
                        .replace("<privilegeCd>?</privilegeCd>","<privilegeCd>UPDT</privilegeCd>")
        ;
        accesses += contribution;
        for (int i=0 ; i<authorizations.length(); i++) {
            JSONObject authorization = (JSONObject) authorizations.get(i);
            boolean isActive = (boolean) authorization.get("isActive");
            String otherClientId = authorization.get("clientId").toString();
            if (!otherClientId.equalsIgnoreCase(clientId) && isActive) {
                boolean isAdmin = (boolean) authorization.get("isAdmin");
                String authorizationContribution = new String(template);
                authorizationContribution = authorizationContribution
                        .replace("<accountId>?</accountId>", "<accountId>" + accountId + "</accountId>")
                        .replace("<clientId>?</clientId>", "<clientId>" + otherClientId + "</clientId>")
                        .replace("<privilegeCd>?</privilegeCd>", "<privilegeCd>"+ (isAdmin?"UPDT":"READ") +"</privilegeCd>")
                ;
                accesses += authorizationContribution;
            }
        }

        return accesses;
    }

    private String buildParties(JSONObject jo, String partyTemplate) {
        String parties = "";
        parties = addContribution(partyTemplate, parties, (JSONArray) jo.get("appellants"), "APL");
        parties = addContribution(partyTemplate, parties, (JSONArray) jo.get("respondents"), "RES");

        return parties;
    }

    private String addContribution(String partyTemplate, String parties, JSONArray collection, String roleType) {
        for (int i = 0; i < collection.length(); i++) {
            JSONObject party = (JSONObject) collection.get(i);
            String contribution = new String(partyTemplate);
            String name = (String) party.get("name");
            String firstname = name.substring(0, name.indexOf(" "));
            String surname = name.substring(name.indexOf(" ") + 1);
            contribution = contribution
                    .replace("<firstGivenName>?</firstGivenName>", "<firstGivenName>" + firstname + "</firstGivenName>")
                    .replace("<organizationName>?</organizationName>", "<organizationName></organizationName>")
                    .replace("<partyType>?</partyType>", "<partyType>IND</partyType>")
                    .replace("<roleType>?</roleType>", "<roleType>"+roleType+"</roleType>")
                    .replace("<secondGivenName>?</secondGivenName>", "<secondGivenName></secondGivenName>")
                    .replace("<surnameName>?</surnameName>", "<surnameName>"+ surname +"</surnameName>")
                    .replace("<thirdGivenName>?</thirdGivenName>", "<thirdGivenName></thirdGivenName>")
            ;
            parties += contribution;
        }
        return parties;
    }
}
