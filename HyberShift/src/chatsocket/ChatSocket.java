package chatsocket;

import java.net.URISyntaxException;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.*;

public class ChatSocket {
	private static Socket socket;
	private static ChatSocket instance = null;
	
	public ChatSocket(){
		try {
			socket = IO.socket("http://hybershift-server-helloqwert12.c9users.io");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ChatSocket getInstance(){
		if (instance == null)
			instance = new ChatSocket();
		return instance;
	}
	
	public static Socket getSocket(){
		return socket;
	}
}
