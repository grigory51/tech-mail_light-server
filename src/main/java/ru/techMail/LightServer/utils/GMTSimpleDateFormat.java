package ru.techMail.LightServer.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * kts, 2014
 * Author: grigory51
 * Date: 12/09/14
 * Time: 22:34
 */
public class GMTSimpleDateFormat extends SimpleDateFormat {
    private static final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");

    public GMTSimpleDateFormat(String pattern) {
        super(pattern, Locale.US);
        this.setTimeZone(GMTSimpleDateFormat.gmtTimeZone);
    }
}
