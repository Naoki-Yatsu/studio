package studio.kdb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Lm {
    private static int majorVersion = 3;
    private static int minorVersion = 33;
    public static Date buildDate;
    
    static {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
            f.setTimeZone(TimeZone.getTimeZone("GMT"));
            buildDate = f.parse("20151008");
        }
        catch (ParseException e) {
        }
    }

    public static int getMajorVersion() {
        return majorVersion;
    }

    public static int getMinorVersion() {
        return minorVersion;
    }

//    public static String getVersionString() {
//        NumberFormat numberFormatter = new DecimalFormat("##.00");
//        double d = getMajorVersion() + getMinorVersion() / 100.0;
//        return numberFormatter.format(d);
//    }
    
    //
    // Temporary for custom version
    //
    
    private static String customVersion = "2019.02.01";
    
    public static String getVersionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(majorVersion).append(".")
                .append(minorVersion).append("_")
                .append(customVersion);
        return sb.toString();
    }
}
