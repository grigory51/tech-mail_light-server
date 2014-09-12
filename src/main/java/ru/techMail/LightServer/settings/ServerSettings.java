package ru.techMail.LightServer.settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import ru.techMail.LightServer.exceptions.SettingsException;

public final class ServerSettings {
    private static final String[] allowedServers = {"http"};

    private String serverType;
    private String root;
    private String index;
    private String listenHost;
    private int listenPort;

    public ServerSettings(String configPath) throws SettingsException {
        String configRawString;
        JSONObject jsonSettings;

        try {
            configRawString = new String(Files.readAllBytes(Paths.get(configPath)));
            jsonSettings = new JSONObject(configRawString);
        } catch (IOException e) {
            throw new SettingsException(SettingsException.CONFIG_FILE_NOT_READABLE, configPath);
        } catch (JSONException e) {
            throw new SettingsException(SettingsException.INVALID_CONFIG_FORMAT, configPath);
        }

        if (!jsonSettings.has("root")) {
            throw new SettingsException(SettingsException.PARAMETER_REQUIRED, "root");
        }

        root = jsonSettings.getString("root");
        index = jsonSettings.get("index") != null ? jsonSettings.getString("index") : "index.html";
        listenHost = jsonSettings.has("listenHost") ? jsonSettings.getString("listenHost") : "127.0.0.1";
        listenPort = jsonSettings.has("listenPort") ? jsonSettings.getInt("listenPort") : 8000;
        serverType = jsonSettings.has("serverType") ? jsonSettings.getString("serverType") : "http";

        if (listenPort < 1 || listenPort > 65535) {
            throw new SettingsException(SettingsException.INVALID_PARAMETER, "listenPort must be in interval 1-65535");
        }

        if (!Arrays.asList(allowedServers).contains(serverType)) {
            throw new SettingsException(SettingsException.INVALID_PARAMETER, serverType + " is not allowed server type");
        }
    }

    public String getRoot() {
        return this.root;
    }

    public String getIndex() {
        return this.index;
    }

    public String getListenHost() {
        return listenHost;
    }

    public int getListenPort() {
        return listenPort;
    }

    public String getServerType() {
        return serverType;
    }
}
