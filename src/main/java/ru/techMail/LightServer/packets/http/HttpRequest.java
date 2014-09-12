package ru.techMail.LightServer.packets.http;

import ru.techMail.LightServer.exceptions.RequestException;
import ru.techMail.LightServer.headers.http.HttpRequestHeader;

/**
 * kts, 2014
 * Author: grigory51
 * Date: 12/09/14
 * Time: 12:07
 */
public class HttpRequest {
    private HttpRequestHeader header;
    String body;

    public HttpRequest(String content) throws RequestException {
        String[] headerAndBody = content.split("\n\n", 2);
        if (headerAndBody.length == 0) {
            throw new RequestException();
        }

        this.header = new HttpRequestHeader(headerAndBody[0]);
        this.body = headerAndBody.length == 2 ? headerAndBody[1] : null;
    }

    private HttpRequestHeader getHeader() {
        return this.header;
    }

    public String getRequestedPath() {
        return this.header.getPath();
    }

    public boolean isGet() {
        return this.getHeader().getMethod().equals("GET");
    }

    public boolean isHead() {
        return this.getHeader().getMethod().equals("HEAD");
    }
}
