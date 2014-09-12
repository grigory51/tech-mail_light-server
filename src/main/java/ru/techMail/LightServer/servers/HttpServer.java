package ru.techMail.LightServer.servers;

import ru.techMail.LightServer.settings.ServerSettings;
import ru.techMail.LightServer.vfs.VFS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;

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
                                SocketChannel channel = (SocketChannel) key.channel();

                                byte[] data = writeQueue.get(channel);
                                writeQueue.remove(channel);

                                channel.write(ByteBuffer.wrap(data));

                                key.cancel();
                                channel.close();
                            }

                            if (key.isReadable()) {
                                SocketChannel channel = (SocketChannel) key.channel();
                                ByteBuffer readBuffer = ByteBuffer.allocate(2048);
                                readBuffer.clear();
                                int read;
                                try {
                                    read = channel.read(readBuffer);
                                    if (read == -1) {
                                        key.interestOps(SelectionKey.OP_WRITE);
                                        writeQueue.put(channel, "400".getBytes());
                                        continue;
                                    }

                                    readBuffer.flip();
                                    byte[] data = new byte[2048];
                                    readBuffer.get(data, 0, read);

                                    String header = new String(data);


                                } catch (IOException e) {
                                    key.interestOps(SelectionKey.OP_WRITE);
                                    writeQueue.put(channel, "400".getBytes());
                                }
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


}
