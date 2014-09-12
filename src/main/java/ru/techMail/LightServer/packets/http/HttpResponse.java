package ru.techMail.LightServer.packets.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ru.techMail.LightServer.headers.http.HttpResponseHeader;
import ru.techMail.LightServer.vfs.VFSFile;

/**
 * kts, 2014
 * Author: grigory51
 * Date: 12/09/14
 * Time: 12:07
 */
public class HttpResponse {
    private final HttpResponseHeader header;
    private byte[] body;

    public HttpResponse(VFSFile file) {
        this(200, file.getContent(), file.getMimeType());
    }

    private HttpResponse(int code, byte[] body) {
        this(code, body, "application/octet-stream");
    }

    protected HttpResponse(int code, byte[] body, String mimeType) {
        this.header = new HttpResponseHeader(code);
        this.setBody(body);
        this.header.setContentType(mimeType);
    }

    public byte[] getBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(this.header.getBytes());
            if (body != null) {
                outputStream.write(body);
            }
        } catch (IOException e) {
            return null;
        }
        return outputStream.toByteArray();
    }

    protected void setBody(byte[] body) {
        this.setBody(body, true);
    }

    public void setBody(byte[] body, boolean changeContentLength) {
        this.body = body;
        if (changeContentLength) {
            this.header.setContentLength(body == null ? 0 : body.length);
        }
    }
}
