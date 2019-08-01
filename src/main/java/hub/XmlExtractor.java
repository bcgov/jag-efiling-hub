package hub;

import javax.inject.Named;

@Named
public class XmlExtractor {

    public String valueFromTag(String tag, String body) {
        int start = body.indexOf("<"+tag+">");
        int end = body.indexOf("</"+tag+">");

        return body.substring(start+tag.length()+2, end);
    }

    public String outerTag(String tag, String body) {
        int start =-1, end =-1;
        String[] lines = body.split("\n");
        for (int i=0; i<lines.length; i++) {
            if (lines[i].contains("<"+tag+">")) {
                start = i;
            }
            if (lines[i].contains("</"+tag+">")) {
                end = i;
            }
        }
        String outer = "";
        for (int i=start; i<=end; i++) {
            outer += lines[i] + "\n";
        }

        return outer;
    }
}
