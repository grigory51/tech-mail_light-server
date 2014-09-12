package ru.techMail.LightServer.servers;

import java.io.IOException;

/**
 * kts studio, 2014
 * author: grigory
 * date: 12.09.2014.
 */
public interface IServer {
    public void start() throws IOException, InterruptedException;

    public void stop();
}
