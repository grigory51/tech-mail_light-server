package ru.techMail.LightServer.servers.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ru.techMail.LightServer.exceptions.RequestException;
import ru.techMail.LightServer.packets.http.HttpRequest;
import ru.techMail.LightServer.packets.http.HttpResponse;
import ru.techMail.LightServer.packets.http.special.BadRequestHttpResponse;
import ru.techMail.LightServer.packets.http.special.ForbiddenHttpResponse;
import ru.techMail.LightServer.packets.http.special.NotFoundHttpResponse;
import ru.techMail.LightServer.packets.http.special.NotImplementedHttpResponse;
import ru.techMail.LightServer.servers.IServer;
import ru.techMail.LightServer.settings.ServerSettings;
import ru.techMail.LightServer.vfs.VFS;
import ru.techMail.LightServer.vfs.VFSFile;

public class HttpServer implements IServer, Runnable {
    private final ServerSettings serverSettings;
    private final VFS vfs;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private final Map<SocketChannel, ByteBuffer> writeQueue = new HashMap<>();
    private final ArrayList<HttpWorker> workers = new ArrayList<>();

    public HttpServer(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
        this.vfs = new VFS(serverSettings.getRoot());
    }

    @Override
    public void start() throws IOException {
        int workersNumber = Runtime.getRuntime().availableProcessors();

        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();

        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(this.serverSettings.getListenHost(), this.serverSettings.getListenPort()));

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        for (int i = 0; i < workersNumber; ++i) {
            HttpWorker worker = new HttpWorker();
            workers.add(worker);
            new Thread(worker, "Worker " + i).start();
        }

        Thread master = new Thread(this, "Master");
        master.start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    try {
                        if (key.isValid()) {
                            if (key.isAcceptable()) {
                                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                                SocketChannel socketChannel = channel.accept();
                                if (socketChannel != null) {
                                    socketChannel.configureBlocking(false);
                                    socketChannel.register(selector, SelectionKey.OP_READ);
                                }
                            } else if (key.isWritable()) {
                                this.sendResponse(key);
                            } else if (key.isReadable()) {
                                this.readRequest(key);
                            }
                        }
                    } catch (CancelledKeyException e) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        writeQueue.remove(channel);
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (selector != null) {
                try {
                    selector.close();
                    serverChannel.socket().close();
                    serverChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendResponse(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer byteBuffer = writeQueue.get(channel);

        try {
            channel.write(byteBuffer);

            if (byteBuffer.remaining() == 0) {
                writeQueue.remove(channel);
                key.cancel();
                channel.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void readRequest(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(2048);
        readBuffer.clear();
        int read;
        try {
            read = channel.read(readBuffer);
            if (read > 0) {
                readBuffer.flip();
                byte[] data = new byte[2048];
                readBuffer.get(data, 0, read);

                HttpRequest request = new HttpRequest(new String(data));

                VFSFile file = vfs.getFile(request.getRequestedPath());
                HttpResponse response;

                if (file != null) {
                    response = new HttpResponse(file);
                } else {
                    if (vfs.isDirectory(request.getRequestedPath())) {
                        file = vfs.getFile(request.getRequestedPath() + serverSettings.getIndex());
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
                    this.putIntoWriteQueue(channel, response);
                } else if (request.isHead()) {
                    response.setBody(null, false);
                    this.putIntoWriteQueue(channel, response);
                } else {
                    this.putIntoWriteQueue(channel, new NotImplementedHttpResponse());
                }

                key.interestOps(SelectionKey.OP_WRITE);
            } else {
                throw new RequestException();
            }
        } catch (IOException | RequestException e) {
            e.printStackTrace();
            this.putIntoWriteQueue(channel, new BadRequestHttpResponse());
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void putIntoWriteQueue(SocketChannel channel, HttpResponse response) {
        writeQueue.put(channel, ByteBuffer.wrap(response.getBytes()));
    }
}
