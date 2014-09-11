package ru.techMail.LightServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public final class HttpResponse {
    static public void sendOK(Socket client, byte[] content, String contentType) {
        StringBuilder responseHeader = new StringBuilder();
        responseHeader.append("HTTP/1.1 200 OK \n");
        responseHeader.append("Content-Type: " + contentType + "\n");
        responseHeader.append("Content-Length: " + content.length + "\n");
        responseHeader.append("Connection: close\n\n");
        /*System.out.println(responseHeader.toString() + "\n");*/

        OutputStream out = null;
        try {
            out = client.getOutputStream();
            out.write(responseHeader.toString().getBytes());
            out.write(content);
            out.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    static public void sendNotFound(Socket client) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.print("HTTP/1.1 404 Not Found\n");
        out.print("Server: /0.0.1");
        out.print("Content-Type: text/html; charset=UTF-8\n");
        out.print("Connection: close\n\n");
        out.print("<html><head><title>Page not Found</title></head><body><h2>Page not found</h2></body></html>");
        out.close();
    }
}
