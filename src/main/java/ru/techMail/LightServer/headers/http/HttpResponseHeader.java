package ru.techMail.LightServer.headers.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import ru.techMail.LightServer.utils.GMTSimpleDateFormat;

/**
 * kts studio, 2014
 * author: grigory
 * date: 12.09.2014.
 */
public class HttpResponseHeader {
    private final static Calendar calendar = Calendar.getInstance();
    private final static SimpleDateFormat dateFormatter = new GMTSimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    private final static HashMap<Integer, String> responseMessage = new HashMap<Integer, String>() {{
        put(200, "Ok");
        put(400, "Bad request");
        put(403, "Forbidden");
        put(404, "Not found");
        put(405, "Not implemented");
    }};

    private final int status;
    private int contentLength;
    private final String server;
    private String contentType;
    private final String connection;
    private final Date date;

    public HttpResponseHeader(int status) {
        this(status, new Date());
    }

    private HttpResponseHeader(int status, Date date) {
        this.status = status;
        this.date = date;
        this.server = "tech-mail.ru server (via g.ozhegov)";
        this.contentLength = 0;
        this.contentType = "application/octet-stream";
        this.connection = "close";
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setContentType(String contentType) {
        this.contentType = (contentType == null ? "application/octet-stream" : contentType);
    }

    public byte[] getBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            //todo проверка статуса на null
            outputStream.write(("HTTP/1.1 " + Integer.toString(status) + " " + responseMessage.get(status) + "\r\n").getBytes());
            outputStream.write(("Date: " + dateFormatter.format(calendar.getTime()) + "\r\n").getBytes());
            outputStream.write(("Server: " + server + "\r\n").getBytes());
            outputStream.write(("Content-Length: " + contentLength + "\r\n").getBytes());
            outputStream.write(("Content-Type: " + contentType + "\r\n").getBytes());
            outputStream.write(("Connection: " + connection + "\r\n").getBytes());
            outputStream.write("\r\n".getBytes());
        } catch (IOException e) {
            return null;
        }
        return outputStream.toByteArray();
    }
}
