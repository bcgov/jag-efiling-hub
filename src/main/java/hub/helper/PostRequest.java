package hub.helper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class PostRequest {

    public static HttpResponse post(String url, byte[] data) throws Exception {
        HttpURLConnection request = (HttpURLConnection) new URL( url ).openConnection();
        request.setDoOutput(true);
        request.setRequestMethod("POST");
        request.setRequestProperty( "Content-Length", Integer.toString(data.length));
        request.getOutputStream().write(data);

        HttpResponse response = new HttpResponse();
        response.setStatusCode(request.getResponseCode());
        response.setContentType(request.getContentType());
        if (request.getResponseCode() < 400) {
            response.setBody(StreamReader.readStream(request.getInputStream()));
        } else {
            response.setBody(StreamReader.readStream(request.getErrorStream()));
        }

        return response;
    }

    public static HttpResponse post(String url, Map<String, String> headers, byte[] data) throws Exception {
        HttpURLConnection request = (HttpURLConnection) new URL( url ).openConnection();
        request.setDoOutput(true);
        request.setRequestMethod("POST");
        request.setRequestProperty( "Content-Length", Integer.toString(data.length));
        for (String header: headers.keySet()) {
            request.setRequestProperty( header, headers.get(header));
        }
        request.getOutputStream().write(data);

        HttpResponse response = new HttpResponse();
        response.setStatusCode(request.getResponseCode());
        response.setContentType(request.getContentType());
        if (request.getResponseCode() < 400) {
            response.setBody(StreamReader.readStream(request.getInputStream()));
        } else {
            response.setBody(StreamReader.readStream(request.getErrorStream()));
        }

        return response;
    }
}
