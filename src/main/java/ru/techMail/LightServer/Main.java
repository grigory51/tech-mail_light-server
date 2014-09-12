package ru.techMail.LightServer;

import java.io.IOException;

import ru.techMail.LightServer.exceptions.SettingsException;
import ru.techMail.LightServer.servers.HttpServer;
import ru.techMail.LightServer.servers.IServer;
import ru.techMail.LightServer.settings.ServerSettings;

/**
 * kts studio, 2014
 * author: grigory
 * date: 12.09.2014.
 */
public class Main {
    public static void main(String[] args) throws SettingsException, IOException, InterruptedException {
        IServer server = null;

        String configPath = System.getProperty("config", System.getProperty("user.dir") + "/settings.json");
        ServerSettings serverSettings = new ServerSettings(configPath);

        if (serverSettings.getServerType().equals("http")) {
            server = new HttpServer(serverSettings);
        }

        if (server != null) {
            server.start();
        } else {
            throw new SettingsException(SettingsException.INVALID_PARAMETER, "serverType is invalid");
        }
    }
}
