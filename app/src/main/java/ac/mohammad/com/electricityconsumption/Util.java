package ac.mohammad.com.electricityconsumption;

import java.util.Calendar;
import java.util.TimeZone;

public class Util {
    public static String getDateString(long msec) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTimeInMillis(msec);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        month++;
        String tmp = day + "/" + month + "/"+ year;
        return tmp;
    }
}
