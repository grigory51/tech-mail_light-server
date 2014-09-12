package ru.techMail.LightServer.packets.http.special;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ru.techMail.LightServer.packets.http.HttpResponse;

/**
 * kts, 2014
 * Author: grigory51
 * Date: 12/09/14
 * Time: 14:37
 */
public class NotImplementedHttpResponse extends HttpResponse {
    public NotImplementedHttpResponse() {
        super(405, null, "text/html");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(("<html><body><p>Not implemented</p></body></html>").getBytes());
            this.setBody(outputStream.toByteArray());
        } catch (IOException ignore) {
        }
    }
}
