package httpServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketOptions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.parser.ParseException;

public class HttpServer {
	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
		ServerSettings.init();
		
		ServerSocket serverSocket = new ServerSocket(8090);
		serverSocket.setReceiveBufferSize(SocketOptions.SO_RCVBUF * 32);
		
		int processors = Runtime.getRuntime().availableProcessors();
		ExecutorService pool = Executors.newFixedThreadPool(processors);
		while (true) {
			WebClient threadClient = new WebClient(serverSocket.accept());
			pool.execute(threadClient);
		}
	}
}
