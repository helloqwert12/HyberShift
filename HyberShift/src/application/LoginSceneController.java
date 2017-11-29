package application;

import java.io.IOException;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;

import chatsocket.ChatSocket;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

public class LoginSceneController {
	Main main;
	
	@FXML private TextField tfEmail;
	@FXML private TextField tfPassword;
	@FXML private Button btnSignin;
	@FXML private Hyperlink hlRegister;
	
	ChatSocket chatsocket;
	Socket socket;
	Runnable socketRunnable;
	
	public LoginSceneController(){
		initSocket();
		Thread socketThread = new Thread(socketRunnable);
		socketThread.start();
	}
	@FXML
	public void onActionBtnSigin(){
		System.out.println("BtnSigin clicked");
		
	}
	
	@FXML
	public void onActionHyberlinkRegister(){
		try {
			Main.showRegisterScene();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initSocket(){
		chatsocket = ChatSocket.getInstance();
		socket = ChatSocket.getSocket();
		socketRunnable = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//set up event
				socket.on(Socket.EVENT_CONNECT, new Listener() {
					
					@Override
					public void call(Object... args) {
						// TODO Auto-generated method stub
						System.out.println("Client connected to server");
					}
				}).on(Socket.EVENT_DISCONNECT, new Listener() {
					
					@Override
					public void call(Object... args) {
						// TODO Auto-generated method stub
						System.out.println("Client disconnected to server");
					}
				});
				
				socket.connect();
			}
		};
	}
}
