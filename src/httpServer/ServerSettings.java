package httpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class ServerSettings {
	public static String documentRoot =  System.getProperty("user.dir");;
	public static String directoryIndex = "index.html";
	public static void init() throws IOException, ParseException {
		File file = new File(System.getProperty("user.dir") + "/settings.ini");
		FileInputStream fileStream = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];
		fileStream.read(buffer);
		StringBuilder bufferString = new StringBuilder();
		for(int i = 0; i < buffer.length; i++) {
			bufferString.append((char)buffer[i]);
		}
		JSONParser parser = new JSONParser();
		JSONObject jsonSettings = (JSONObject) parser.parse(bufferString.toString());
		
		documentRoot = (String) jsonSettings.get("DocumentRoot");
		directoryIndex = (String) jsonSettings.get("DirectoryIndex");
	}
}
