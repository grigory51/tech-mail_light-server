package ru.techMail.LightServer.headers.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.techMail.LightServer.exceptions.HeaderException;

/**
 * kts studio, 2014
 * author: grigory
 * date: 12.09.2014.
 */
public class HttpRequestHeader {
    private final static Pattern firstLine = Pattern.compile("(GET|PUT|POST|DELETE|HEAD)\\s([^\\?]*)\\??(.*)\\s(\\S+)");
    private final static Pattern field = Pattern.compile("(.*?): (.*?)\\r\\n");

    private String method;
    private String path;
    private String httpVersion;
    private String args;

    public HttpRequestHeader(String rawHeader) throws HeaderException {
        String[] splitHeader = rawHeader.split("\n", 2);

        if (splitHeader.length == 0) {
            throw new HeaderException();
        }

        Matcher firstLineMatcher = firstLine.matcher(splitHeader[0]);

        if (!firstLineMatcher.find(0)) {
            throw new HeaderException();
        }

        this.setMethod(firstLineMatcher.group(1));
        this.setPath(firstLineMatcher.group(2));
        this.setArgs(firstLineMatcher.group(3));
        this.setHttpVersion(firstLineMatcher.group(4));
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    private void setMethod(String method) {
        this.method = method.toUpperCase();
    }

    private void setPath(String path) {
        this.path = path;

    private void setArgs(String args) {
        this.args = args;
    }

    private void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }
}
