package ru.techMail.LightServer.servers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import ru.techMail.LightServer.exceptions.RequestException;
import ru.techMail.LightServer.packets.http.HttpRequest;
import ru.techMail.LightServer.packets.http.HttpResponse;
import ru.techMail.LightServer.packets.http.special.BadRequestHttpResponse;
import ru.techMail.LightServer.packets.http.special.NotFoundHttpResponse;
import ru.techMail.LightServer.packets.http.special.NotImplementedHttpResponse;
import ru.techMail.LightServer.settings.ServerSettings;
import ru.techMail.LightServer.vfs.VFS;
import ru.techMail.LightServer.vfs.VFSFile;

public class HttpServer implements IServer, Runnable {
    private ServerSettings serverSettings;
    private VFS vfs;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private Map<SocketChannel, byte[]> writeQueue = new HashMap<>();
    private Map<SocketChannel, StringBuilder> readQueue = new HashMap<>();

    public HttpServer(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
        this.vfs = new VFS(serverSettings.getRoot());
    }

    @Override
    public void start() throws IOException, InterruptedException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();

        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(this.serverSettings.getListenHost(), this.serverSettings.getListenPort()));

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        Thread thread = new Thread(this);
        thread.start();
        thread.join();
    }

    @Override
    public void stop() {

    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select(10000);
                for (SelectionKey key : selector.selectedKeys()) {
                    try {
                        if (key.isValid()) {
                            if (key.isAcceptable()) {
                                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                                SocketChannel socketChannel = channel.accept();
                                if (socketChannel != null) {
                                    socketChannel.configureBlocking(false);
                                    socketChannel.register(selector, SelectionKey.OP_READ);
                                }
                            }
                            if (key.isWritable()) {
                                this.sendResponse(key);
                            }
                            if (key.isReadable()) {
                                this.readRequest(key);
                            }
                        }
                    } catch (CancelledKeyException e) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        readQueue.remove(channel);
                        writeQueue.remove(channel);
                    }
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

        byte[] data = writeQueue.get(channel);
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);

        try {
            channel.write(byteBuffer);

            writeQueue.remove(channel);
            if (byteBuffer.hasRemaining()) {
                writeQueue.put(channel, byteBuffer.array());
            } else {
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

                key.interestOps(SelectionKey.OP_WRITE);
                HttpResponse response;
                if (file != null) {
                    response = new HttpResponse(file);
                } else {
                    response = new NotFoundHttpResponse(request.getRequestedPath());
                }

                if (request.isGet()) {
                    this.putIntoWriteQueue(channel, response);
                } else if (request.isHead()) {
                    response.setBody(null, false);
                    this.putIntoWriteQueue(channel, response);
                } else {
                    this.putIntoWriteQueue(channel, new NotImplementedHttpResponse());
                }
            } else {
                throw new RequestException();
            }
        } catch (IOException | RequestException e) {
            e.printStackTrace();
            key.interestOps(SelectionKey.OP_WRITE);
            this.putIntoWriteQueue(channel, new BadRequestHttpResponse());
        }
    }

    private void putIntoWriteQueue(SocketChannel channel, HttpResponse response) {
        writeQueue.put(channel, response.getBytes());
    }
}
