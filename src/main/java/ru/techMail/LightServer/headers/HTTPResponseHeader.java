package ru.techMail.LightServer.headers;

import java.util.Date;

/**
 * kts studio, 2014
 * author: grigory
 * date: 12.09.2014.
 */
public class HTTPResponseHeader extends AbstractHeader {
    private Date date;
    private String server;
    private int contentLength;
    private String contentType;
    private String connection;
}
