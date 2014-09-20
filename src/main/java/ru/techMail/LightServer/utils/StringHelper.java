package ru.techMail.LightServer.utils;

/**
 * kts, 2014
 * Author: grigory51
 * Date: 12/09/14
 * Time: 22:08
 */
public class StringHelper {
    public static String escapeHtml(String escapedString) {
        return escapedString
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
