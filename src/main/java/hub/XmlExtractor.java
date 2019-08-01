package hub;

import javax.inject.Named;

@Named
public class XmlExtractor {

    public String valueFromTag(String tag, String body) {
        int start = body.indexOf("<"+tag+">");
        int end = body.indexOf("</"+tag+">");

        return body.substring(start+tag.length()+2, end);
    }
}
