package youtube.spider.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jianlin210349
 */
public class SpiderDateUtils {
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd";
    private static final Map<String, DateFormat> FORMAT_CACHE = new ConcurrentHashMap<String, DateFormat>();

    static {
        FORMAT_CACHE.put(DEFAULT_PATTERN, new SimpleDateFormat(DEFAULT_PATTERN));
    }

    private static void addFormatToCache(DateFormat dateFormat) {
        FORMAT_CACHE.put(DEFAULT_PATTERN, dateFormat);
    }

    public static Date formatDate(String dateText) {
        return formatDate(dateText, DEFAULT_PATTERN);
    }

    public static Date formatDate(String dateText, String format) {
        DateFormat dateFormat = FORMAT_CACHE.get(format);
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(format);
            addFormatToCache(dateFormat);
        }
        try {
            return dateFormat.parse(dateText);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException(String.format("convert dateText to date object error, dateText:%s, format:%s", dateText, format));
    }


    public static Date convertStrToDate(String sourceStr, String format) {
        if (sourceStr == null || "".equals(sourceStr)) {
            return null;
        }
        DateFormat dateFormat = FORMAT_CACHE.get(format);
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(format);
            addFormatToCache(dateFormat);
        }
        Date date = null;
        try {
            date = dateFormat.parse(sourceStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return date;
    }

    public static Date convertStrToDate(String sourceStr, String format, Locale locale) {
        if (sourceStr == null || "".equals(sourceStr)) {
            return null;
        }
        DateFormat dateFormat = FORMAT_CACHE.get(format);
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(format, locale);
            addFormatToCache(dateFormat);
        }
        Date date = null;
        try {
            date = dateFormat.parse(sourceStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return date;
    }

    /**
     * 根据字符串获取上传时间
     *
     * @param time 视频上传时间字符串
     * @return
     */
    public static Date getUploadTime(String time) {
        if (time == null || "".equals(time)) {
            return null;
        }
        if (time.contains("昨天")) {
            return DateUtils.addDays(new Date(), -1);
        } else if (time.contains("前天")) {
            return DateUtils.addDays(new Date(), -2);
        } else if (time.contains("天前")) {
            return DateUtils.addDays(new Date(), -Integer.parseInt(time.replaceAll("\\D", "")));
        } else if (time.contains("周前")) {
            return DateUtils.addDays(new Date(), -Integer.parseInt(time.replaceAll("\\D", "")) * 7);
        } else if (time.contains("年前")) {
            return DateUtils.addDays(new Date(), -Integer.parseInt(time.replaceAll("\\D", "")) * 365);
        } else if (time.contains("月前")) {
            return DateUtils.addDays(new Date(), -Integer.parseInt(time.replaceAll("\\D", "")) * 30);
        } else if (time.matches("[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}")) {
            DateFormat format = FORMAT_CACHE.get(DEFAULT_PATTERN);
            Calendar now = Calendar.getInstance();
            try {
                return new Date(format.parse(now.get(Calendar.YEAR) + "-" + time.split(" ")[0]).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (time.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{2}")) {
            DateFormat format = FORMAT_CACHE.get(DEFAULT_PATTERN);
            try {
                return new Date(format.parse(time).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (time.matches("[0-9]{1,2}-[0-9]{2}")) {
            DateFormat format = FORMAT_CACHE.get(DEFAULT_PATTERN);
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            try {
                return new Date(format.parse(calendar.get(Calendar.YEAR) + "-" + time).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (time.matches("[0-9]{2}:[0-9]{2}")) {
            DateFormat format = FORMAT_CACHE.get("yyyy-MM-dd HH:mm");
            Calendar now = Calendar.getInstance();
            try {
                return new Date(format.parse(now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-" + now.get(Calendar.DAY_OF_MONTH) + " " + time).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return new Date();
    }
}
