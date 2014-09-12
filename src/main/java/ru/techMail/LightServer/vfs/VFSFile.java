package ru.techMail.LightServer.vfs;

import org.apache.tika.Tika;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * kts studio, 2014
 * author: grigory
 * date: 12.09.2014.
 */
public class VFSFile {
    private final static Tika tika = new Tika();

    private String mimeType;
    private final String path;
    private byte[] content;

    public VFSFile(String path) throws IOException {
        Path pathObject = Paths.get(path);

        this.path = path;
        this.content = Files.readAllBytes(pathObject);

        this.setMimeType(tika.detect(pathObject.toFile()));
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public byte[] getContent() {
        return this.content;
    }

    private void setMimeType(String mimeType) {
        this.mimeType = (mimeType == null ? "application/octet-stream" : mimeType);
    }
}
