package ru.techMail.LightServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;

public final class Files {
    static private HashMap<String, byte[]> files = new HashMap<String, byte[]>();

    public static String getContentTypeByPath(String path) {
        if (path.equals("/")) {
            path += ServerSettings.directoryIndex;
        }
        String i = URLConnection.guessContentTypeFromName(getFileNameByPath(path));
        if (i == null) {
            String[] temp = path.split("/");
            if (temp.length > 0) {
                String ext = null;
                String name = temp[temp.length - 1];
                for (int j = name.length() - 1; j > -1; j--) {
                    if (name.charAt(j) == '.') {
                        if (j + 1 != name.length()) {
                            ext = name.substring(j + 1);
                        }
                        break;
                    }
                }
                if (ext.equals("css")) {
                    i = "text/css";
                }
            }

        }
        return i;
    }

    public static String getFileNameByPath(String path) {
        String[] splitPath = path.split("/");
        if (splitPath.length > 0) {
            return splitPath[splitPath.length - 1];
        } else {
            return "";
        }
    }

    static public byte[] getContentByPath(String path) {
        if (files.containsKey(path)) {
            return files.get(path);
        } else {
            File file = new File(ServerSettings.documentRoot + path);
            if (file.isDirectory()) {
                file = new File(ServerSettings.documentRoot + path + ServerSettings.directoryIndex);
            }
            if (file.isFile()) {
                byte b[] = new byte[(int) file.length()];
                FileInputStream inputStream;
                try {
                    inputStream = new FileInputStream(file);
                    inputStream.read(b);
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                files.put(path, b);
                return b;
            } else {
                return null;
            }
        }
    }
}
