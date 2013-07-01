package httpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class HttpRequestHeader {
	public String[] httpHeader;
	public String path;
	
	public void getHttpHeader (Socket client) {
		BufferedReader inputFromClient = null;
		try {
			inputFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		char[] temp = new char[4096];
		try {
			inputFromClient.read(temp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder tempString = new StringBuilder();
		
		for(int i = 0; i < temp.length; i++) {
			tempString.append(temp[i]);
		}
		httpHeader = tempString.toString().split("\n");
		try {
			path = httpHeader[0].split(" ")[1];
		}
		catch (Exception e) {
			System.out.println("Ошибка заголовка");
		}
		
	}
	
	public void print() {
		for(int i = 0; i < httpHeader.length; i++) {
			System.out.println(httpHeader[i]);
		}
		System.out.println();
	}

}
