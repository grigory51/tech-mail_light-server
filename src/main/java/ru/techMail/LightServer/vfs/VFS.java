package ru.techMail.LightServer.vfs;

import java.io.IOException;
import java.util.HashMap;

public class VFS {
    private final HashMap<String, VFSFile> memoryCache;
    private final boolean memoryCacheEnabled;
    private final String root;

    public VFS(String root) {
        this(root, true);
    }

    private VFS(String root, boolean memoryCacheEnabled) {
        this.memoryCache = new HashMap<>();
        this.root = root;
        this.memoryCacheEnabled = memoryCacheEnabled;
    }

    private String getAbsolutePath(String relativePath) {
        return this.root + Paths.get(relativePath).normalize();
    }

    public VFSFile getFile(String relativePath) {
        String path = this.getAbsolutePath(relativePath);

        if (memoryCacheEnabled && this.memoryCache.containsKey(path)) {
            return memoryCache.get(path);
        }

        try {
            VFSFile vfsFile = new VFSFile(path);
            if (this.memoryCacheEnabled) {
                memoryCache.put(relativePath, vfsFile);
            }
            return vfsFile;
        } catch (IOException e) {
            return null;
        }
    }
}
