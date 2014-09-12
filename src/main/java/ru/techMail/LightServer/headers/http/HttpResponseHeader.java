package ru.techMail.LightServer.headers.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * kts studio, 2014
 * author: grigory
 * date: 12.09.2014.
 */
public class HttpResponseHeader {
    private final static DateFormat dateFormatter = DateFormat.getDateInstance();
    private final static HashMap<Integer, String> responseMessage = new HashMap<Integer, String>() {{
        put(200, "Ok");
        put(400, "Bad request");
        put(404, "Not found");
        put(405, "Not implemented");
    }};

    private int status;
    private Date date;
    private String server;
    private int contentLength;
    private String contentType;
    private String connection;

    public HttpResponseHeader() {
        this(200);
    }

    public HttpResponseHeader(int status) {
        this(status, new Date(), "tech-mail.ru server (via g.ozhegov)", 0, "application/octet-stream", "close");
    }

    public HttpResponseHeader(int status, Date date, String server, int contentLength, String contentType, String connection) {
        this.status = status;
        this.date = date;
        this.server = server;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.connection = connection;
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
            outputStream.write(("HTTP/1.1 " + Integer.toString(status) + " " + responseMessage.get(status) + "\n").getBytes());
            outputStream.write(("Date: " + dateFormatter.format(date) + "\n").getBytes());
            outputStream.write(("Server: " + server + "\n").getBytes());
            outputStream.write(("Content-Length: " + contentLength + "\n").getBytes());
            outputStream.write(("Content-Type: " + contentType + "\n").getBytes());
            outputStream.write(("Connection: " + connection + "\n").getBytes());
            outputStream.write("\n".getBytes());
        } catch (IOException e) {
            return null;
        }
        return outputStream.toByteArray();
    }
}
