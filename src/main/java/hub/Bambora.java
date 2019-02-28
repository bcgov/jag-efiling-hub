package hub;

import hub.helper.Environment;
import hub.http.PaymentIncoming;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.soap.*;
import java.util.Base64;

import static java.util.Base64.getEncoder;

@Named
public class Bambora {

    @Inject
    Environment environment;

    public String merchantId() {
        return environment.getValue("BAMBORA_MERCHANT_ID");
    }

    public String apiPassCode() {
        return environment.getValue("BAMBORA_API_PASSCODE");
    }

    public String tokenizationUrl() {
        return environment.getValue("BAMBORA_TOKENIZATION_URL");
    }

    public String paymentUrl() {
        return environment.getValue("BAMBORA_PAYMENT_URL");
    }

    public String passCode() {
        String passcode = merchantId() + ":" + apiPassCode();

        return getEncoder().encodeToString(passcode.getBytes());
    }

    public String tokenizationMessage(PaymentIncoming incoming) {
        String month = incoming.getExpiry().substring(0, 2);
        String year = incoming.getExpiry().substring(2);
        return "{\"cvd\":" + incoming.getCvd() + ",\"expiry_month\":"+ month +",\"expiry_year\":" + year + ",\"number\":" + incoming.getNumber() + "}";
    }

    public String paymentMessage(String token, String amount) {
        return "{\"amount\":" + amount + ",\"payment_method\":\"token\",\"token\":{\"name\":\"Jane Doe\",\"code\":\"" +token + "\"}}";
    }
}
