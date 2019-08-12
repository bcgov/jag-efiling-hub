package hub;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class XmlExtractorTest {

    XmlExtractor extract = new XmlExtractor();

    @Test
    public void offerContentExtraction() {
        String value = extract.valueFromTag("world", "<hello><world>:)</world></hello>");

        assertThat(value, equalTo(":)"));
    }

    @Test
    public void offerOuterTagExtractionWhenInSeperateLines() {
        String value = extract.outerTag("world", "<hello>\n<world>:)</world>\n</hello>");

        assertThat(value, equalTo("<world>:)</world>\n"));
    }
}
