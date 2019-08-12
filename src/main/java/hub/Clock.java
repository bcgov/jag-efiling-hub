package hub;

import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Named
public class Clock {

    private static Date date = null;

    public static void broken(Date date) {
        Clock.date = date;
    }

    public Date now() {
        if (Clock.date != null) {
            return Clock.date;
        }
        return new Date();
    }

    public String nowAsString() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        return dateFormatGmt.format(now());
    }
}
