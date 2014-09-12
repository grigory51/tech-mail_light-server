package ru.techMail.LightServer.vfs;

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
    private String mimeType;
    private String path;
    private byte[] content;

    public VFSFile(String path) throws IOException {
        Path pathObject = Paths.get(path);

        this.path = path;
        this.content = Files.readAllBytes(pathObject);
        this.mimeType = Files.probeContentType(pathObject);
    }

    public String getMimeType() {return mimeType;}

    public String getPath() {return path;}

    public byte[] getContent() {return content;}
}
