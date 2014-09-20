package ru.techMail.LightServer.packets.http.special;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ru.techMail.LightServer.packets.http.HttpResponse;
import ru.techMail.LightServer.utils.StringHelper;

/**
 * kts, 2014
 * Author: grigory51
 * Date: 12/09/14
 * Time: 14:37
 */
public class NotFoundHttpResponse extends HttpResponse {
    public NotFoundHttpResponse(String comment) {
        super(404, null, "text/html");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(("<html><body><p>Not found" + (comment == null ? "" : ": " + StringHelper.escapeHtml(comment) + "</p></body></html>")).getBytes());
            this.setBody(outputStream.toByteArray());
        } catch (IOException ignore) {
        }
    }
}
