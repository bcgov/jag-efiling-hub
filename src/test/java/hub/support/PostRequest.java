package hub.support;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static hub.support.StreamReader.readStream;

public class PostRequest {

    public static HttpResponse post(String url, String body) throws Exception {
        HttpURLConnection request = (HttpURLConnection) new URL( url ).openConnection();
        request.setDoOutput(true);
        request.setRequestMethod("POST");
        byte[] postData = body.getBytes( StandardCharsets.UTF_8 );
        request.setRequestProperty( "Content-Length", Integer.toString(postData.length));
        DataOutputStream writer = new DataOutputStream( request.getOutputStream());
        writer.write(postData);

        HttpResponse response = new HttpResponse();
        response.setStatusCode(request.getResponseCode());
        response.setContentType(request.getContentType());
        if (request.getResponseCode() < 400) {
            response.setBody(readStream(request.getInputStream()));
        } else {
            response.setBody(readStream(request.getErrorStream()));
        }

        return response;
    }
}
