package application;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;

import chatsocket.ChatSocket;
import dataobject.UserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

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
		
		socket.on("authentication_result", new Listener() {
			
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
                Boolean result;
                try {
                    result = data.getBoolean("result");
                    System.out.println(result);
                    if (result == true){
                    	Platform.runLater(new Runnable(){
    						@Override
    						public void run() {		
    							Main.showMainFromLoginScene();
    						}       	 
                        });   	
                    }
                    
                   
                } catch (JSONException e) {
                	e.printStackTrace();
                    return;
                }	
				System.out.println("Sign in successfully");
			}
		});
		
	}
	@FXML
	public void onActionBtnSigin(){
		System.out.println("BtnSigin clicked");
		if (!isValid()){
			new Alert(AlertType.WARNING, "Something went wrong with your information. Please check again").show();
			return;
		}
		
		//Convert to JSONObject
		JSONObject userjson = new JSONObject();
		try {
			userjson.put("email", tfEmail.getText());
			userjson.put("password", tfPassword.getText());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		socket.emit("authentication", userjson);
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
	
	private boolean isValid(){
		
		if (tfEmail.getText().trim().length() == 0)
			return false;
		
		if (tfPassword.getText().trim().length() == 0)
			return false;
		
		return true;
	}
}
