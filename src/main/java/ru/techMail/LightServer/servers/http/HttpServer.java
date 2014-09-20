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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.techMail.LightServer.exceptions.RequestException;
import ru.techMail.LightServer.packets.http.HttpResponse;
import ru.techMail.LightServer.packets.http.special.BadRequestHttpResponse;
import ru.techMail.LightServer.servers.IServer;
import ru.techMail.LightServer.settings.ServerSettings;

public class HttpServer implements IServer, Runnable {
    private final ServerSettings serverSettings;
    private final int workersNumber;

    private ServerSocketChannel serverChannel;
    private Selector selector;

    private final Map<SocketChannel, ByteBuffer> writeQueue = new ConcurrentHashMap<>();
    private final ArrayList<HttpWorker> workers = new ArrayList<>();

    public HttpServer(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
        this.workersNumber = Runtime.getRuntime().availableProcessors();
    }

    public ServerSettings getSettings() {
        return this.serverSettings;
    }

    @Override
    public void start() throws IOException {
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();

        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(this.serverSettings.getListenHost(), this.serverSettings.getListenPort()));

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        for (int i = 0; i < this.workersNumber; ++i) {
            HttpWorker worker = new HttpWorker(this);
            workers.add(worker);
            new Thread(worker, "Worker " + i).start();
        }

        Thread master = new Thread(this, "Master");
        master.start();
    }

    @Override
    public void stop() {

    }

    public void returnAsyncResultFromWorker(SelectionKey key, HttpResponse value) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            this.putIntoWriteQueue(channel, value);
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (CancelledKeyException e) {
            try {
                writeQueue.remove(channel);
                channel.close();
                key.cancel();
            } catch (IOException ignored) {
            }
        }
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

                try {
                    this.workers.get(key.hashCode() % this.workersNumber).handleRequest(key, new String(data));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
