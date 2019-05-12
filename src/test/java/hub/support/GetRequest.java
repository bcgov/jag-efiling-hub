package hub.support;

import hub.helper.Bytify;
import hub.helper.HttpResponse;
import hub.helper.Stringify;

import java.net.HttpURLConnection;
import java.net.URL;

public class GetRequest {

    public static HttpResponse get(String url) throws Exception {
        HttpURLConnection request = (HttpURLConnection) new URL( url ).openConnection();
        HttpResponse response = new HttpResponse();
        response.setStatusCode(request.getResponseCode());
        response.setContentType(request.getContentType());
        if (request.getResponseCode() < 400) {
            if ("application/pdf".equalsIgnoreCase(response.getContentType())) {
                response.setBinaryBody(new Bytify().inputStream(request.getInputStream()));
            }
            else {
                response.setBody(new Stringify().inputStream(request.getInputStream()));
            }
        } else {
            response.setBody(new Stringify().inputStream(request.getErrorStream()));
        }

        return response;
    }
}
