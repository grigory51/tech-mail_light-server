package httpServer;

import java.net.Socket;

public class WebClient extends Thread {
	private Socket client = null;
	
	public WebClient(Socket client) {
		this.client = client;
	}
	@Override
	public void run() {
		HttpRequestHeader requestHeader = new HttpRequestHeader();
		requestHeader.getHttpHeader(client);
		byte b[];
		b = Files.getContentByPath(requestHeader.path);
		
		if (b != null) {
			HttpResponse.sendOK(client, b, Files.getContentTypeByPath(requestHeader.path));
		}
		else {
			HttpResponse.sendNotFound(client);
		}
	}

}
