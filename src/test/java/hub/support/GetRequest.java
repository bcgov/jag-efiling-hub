package hub.support;

import hub.helper.HttpResponse;

import java.net.HttpURLConnection;
import java.net.URL;

import static hub.helper.StreamReader.readStream;
import static hub.helper.StreamReader.readStreamAsbytes;

public class GetRequest {

    public static HttpResponse get(String url) throws Exception {
        HttpURLConnection request = (HttpURLConnection) new URL( url ).openConnection();
        HttpResponse response = new HttpResponse();
        response.setStatusCode(request.getResponseCode());
        response.setContentType(request.getContentType());
        if (request.getResponseCode() < 400) {
            if ("application/pdf".equalsIgnoreCase(response.getContentType())) {
                response.setBinaryBody(readStreamAsbytes(request.getInputStream()));
            }
            else {
                response.setBody(readStream(request.getInputStream()));
            }
        } else {
            response.setBody(readStream(request.getErrorStream()));
        }

        return response;
    }
}
