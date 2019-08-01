package hub.support;

import org.junit.Test;

import java.util.Base64;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ExplorationTest {

    @Test
    public void base64Encoding() {
        String encoded = Base64.getEncoder().encodeToString("hello world".getBytes());

        assertThat(encoded, equalTo("aGVsbG8gd29ybGQ="));
    }

    @Test
    public void base64Decoding() {
        byte[] decoded = Base64.getDecoder().decode("aGVsbG8gd29ybGQ=");

        assertThat(new String(decoded), equalTo("hello world"));
    }
}
