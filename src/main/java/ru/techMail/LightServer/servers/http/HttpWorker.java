package ru.techMail.LightServer.servers.http;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;

import ru.techMail.LightServer.exceptions.RequestException;
import ru.techMail.LightServer.packets.http.HttpRequest;
import ru.techMail.LightServer.packets.http.HttpResponse;
import ru.techMail.LightServer.packets.http.special.ForbiddenHttpResponse;
import ru.techMail.LightServer.packets.http.special.NotFoundHttpResponse;
import ru.techMail.LightServer.packets.http.special.NotImplementedHttpResponse;
import ru.techMail.LightServer.vfs.VFS;
import ru.techMail.LightServer.vfs.VFSFile;

/**
 * kts, 2014
 * Author: grigory51
 * Date: 12/09/14
 * Time: 18:44
 */
class HttpWorker implements Runnable {
    private final HttpServer server;
    private final VFS vfs;
    private final LinkedList<HandleContainer> keyQueue;

    public HttpWorker(HttpServer server) {
        this.server = server;
        this.vfs = new VFS(this.server.getSettings().getRoot());
        this.keyQueue = new LinkedList<>();
    }

    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this.keyQueue) {
                    while (this.keyQueue.isEmpty()) {
                        this.keyQueue.wait();
                    }
                }

                HandleContainer handleContainer = this.keyQueue.removeFirst();
                SelectionKey key = handleContainer.getKey();
                String requestString = handleContainer.getRequestString();

                HttpRequest request = new HttpRequest(requestString);

                VFSFile file = vfs.getFile(request.getRequestedPath());
                HttpResponse response;

                if (file != null) {
                    response = new HttpResponse(file);
                } else {
                    if (vfs.isDirectory(request.getRequestedPath())) {
                        file = vfs.getFile(request.getRequestedPath() + this.server.getSettings().getIndex());
                        if (file != null) {
                            response = new HttpResponse(file);
                        } else {
                            response = new ForbiddenHttpResponse();
                        }
                    } else {
                        response = new NotFoundHttpResponse(request.getRequestedPath());
                    }
                }

                if (request.isGet()) {
                    this.server.returnAsyncResultFromWorker(key, response);
                } else if (request.isHead()) {
                    response.setBody(null, false);
                    this.server.returnAsyncResultFromWorker(key, response);
                } else {
                    this.server.returnAsyncResultFromWorker(key, new NotImplementedHttpResponse());
                }
            }
        } catch (InterruptedException | RequestException e) {
            e.printStackTrace();
        }
    }

    public void handleRequest(final SelectionKey key, final String requestString) throws InterruptedException {
        synchronized (keyQueue) {
            keyQueue.add(new HandleContainer(key, requestString));
            keyQueue.notify();
        }
    }

    class HandleContainer {
        private final SelectionKey key;
        private final String requestString;

        public HandleContainer(SelectionKey key, String requestString) {
            this.key = key;
            this.requestString = requestString;
        }

        public SelectionKey getKey() {
            return key;
        }

        public String getRequestString() {
            return requestString;
        }
    }
}