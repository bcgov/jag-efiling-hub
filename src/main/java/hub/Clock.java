package hub;

import javax.inject.Named;
import java.util.Date;

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
}
