package studio.chart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

public class DateUtility {
    
    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////
    
    // sdf for format
    // private static final SimpleDateFormat sdfOutput1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    // private static final SimpleDateFormat sdfOutput2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdfOutput3 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat sdfOutput4 = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final SimpleDateFormat sdfOutput5 = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdfOutput6 = new SimpleDateFormat("HH:mm");

    // sdf for parse
    private static final SimpleDateFormat sdf01 = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
    private static final SimpleDateFormat sdf02 = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static final SimpleDateFormat sdf03 = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private static final SimpleDateFormat sdf04 = new SimpleDateFormat("MM.dd HH:mm:ss.SSS");
    private static final SimpleDateFormat sdf05 = new SimpleDateFormat("MM.dd HH:mm:ss");
    private static final SimpleDateFormat sdf06 = new SimpleDateFormat("MM.dd HH:mm");
    private static final SimpleDateFormat sdf07 = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final SimpleDateFormat sdf08 = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdf09 = new SimpleDateFormat("HH:mm");

    // List of parse sdfs
    private static final List<SimpleDateFormat> sdfList = new ArrayList<>();

    static {
        sdfList.add(sdf01);
        sdfList.add(sdf02);
        sdfList.add(sdf03);
        sdfList.add(sdf04);
        sdfList.add(sdf05);
        sdfList.add(sdf06);
        sdfList.add(sdf07);
        sdfList.add(sdf08);
        sdfList.add(sdf09);
    }
    
    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////
    
    public synchronized static Date parseDate(String str) {
        // remove alphabet
        str = str.replaceAll("[a-zA-Z]", " ");
        // replace date separator
        str = str.replaceAll("[/\\-]", ".");

        Date date = null;
        for (SimpleDateFormat sdf : sdfList) {
            try {
                date = sdf.parse(str);
                break;
            } catch (Exception e) {
            }
        }
        return date;
    }

    /**
     * parse to String
     * @param date
     * @param showDate
     * @return
     */
    public synchronized static String parseString(Date date, boolean showDate) {
        Calendar cal = DateUtils.toCalendar(date);
        if (showDate) {
            // if (cal.get(Calendar.MILLISECOND) != 0) {
            // return sdfOutput1.format(date);
            // } else if (cal.get(Calendar.SECOND) != 0) {
            // return sdfOutput2.format(date);
            // } else {
            // return sdfOutput3.format(date);
            // }
            return sdfOutput3.format(date);

        } else {
            if (cal.get(Calendar.MILLISECOND) != 0) {
                return sdfOutput4.format(date);
            } else if (cal.get(Calendar.SECOND) != 0) {
                return sdfOutput5.format(date);
            } else {
                return sdfOutput6.format(date);
            }
        }
    }

    /**
     * Compare "date" of two Dates.
     * 
     * @return
     */
    public static int compareDate(Date date1, Date date2) {
        Calendar cal1 = DateUtils.toCalendar(date1);
        Calendar cal2 = DateUtils.toCalendar(date2);

        int year = Integer.compare(cal1.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
        int month = Integer.compare(cal1.get(Calendar.MONTH), cal2.get(Calendar.MONTH));
        int dayOfMonth = Integer.compare(cal1.get(Calendar.DAY_OF_MONTH), cal2.get(Calendar.DAY_OF_MONTH));

        if (year != 0) {
            return year;
        }
        if (month != 0) {
            return month;
        }
        if (dayOfMonth != 0) {
            return dayOfMonth;
        }
        return 0;
    }

    // For TEST
//    public static void main(String[] args) {
//        String str = "2013/1/11T11:11";
//        Date date = parseDate(str);
//        System.out.println(sdfOutput1.format(date) + "  " + parseString(date, true));
//
//        str = "1-23T1:23:4";
//        date = parseDate(str);
//        System.out.println(sdfOutput1.format(date) + "  " + parseString(date, true));
//
//        str = "1:23:4.12";
//        date = parseDate(str);
//        System.out.println(sdfOutput4.format(date) + "  " + parseString(date, false));
//    }

}
