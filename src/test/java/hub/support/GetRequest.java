package hub.support;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static hub.support.StreamReader.readStream;
import static hub.support.StreamReader.readStreamAsbytes;

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
