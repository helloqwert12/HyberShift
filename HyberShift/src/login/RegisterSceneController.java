package login;

import org.json.JSONException;
import org.json.JSONObject;

import application.Main;
import chatsocket.ChatSocket;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;

import dataobject.UserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class RegisterSceneController {
	@FXML TextField tfEmail;
	@FXML TextField tfName;
	@FXML TextField tfPassword;
	@FXML TextField tfConfirmPassword;
	@FXML TextField tfPhoneNumber;
	@FXML Button btnConfirm;

	
	Socket socket;
	UserInfo userInfo;
	
	public RegisterSceneController(){
		
		ChatSocket.getInstance();
		socket = ChatSocket.getSocket();
			
		socket.on("register_result", new Listener() {
			
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
                String message;
                try {
                    message = data.getString("message");
                    System.out.println(message);
                    Platform.runLater(new Runnable(){
						@Override
						public void run() {		
							new Alert(AlertType.INFORMATION, message).show();
						}       	 
                    });
                   
                } catch (JSONException e) {
                	e.printStackTrace();
                    return;
                }	
			}
		});
	}
	
	public void onActionBtnConfirm(){
		System.out.println("BtnConfirm clicked");
		
		if (!isValid()){
			new Alert(AlertType.WARNING, "Something went wrong with your information. Please check again").show();
			return;
		}
		//Push data
		userInfo = UserInfo.getInstance();
		
		userInfo.setEmail(tfEmail.getText().toString());
		userInfo.setPassword(tfPassword.getText().toString());
		userInfo.setPhone(tfPhoneNumber.getText().toString());
		userInfo.setFullName(tfName.getText().toString());
		//Convert to JSONObject
		JSONObject userjson = new JSONObject();
		try {
			//userjson.put("userid", userInfo.getUserid());
			userjson.put("email", userInfo.getEmail());
			userjson.put("fullname", userInfo.getFullName());
			userjson.put("password", userInfo.getPassword());
			userjson.put("phone", userInfo.getPhone());
			userjson.put("linkavatar", userInfo.getLinkAvatar());
				
			socket.emit("register", userjson);
			//Main.showMainFromRegister();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private boolean isValid(){
		System.out.println(tfEmail.getText().trim().length());
		if (tfEmail.getText().trim().length() == 0)
			return false;
		if (tfName.getText().trim().length() == 0)
			return false;
		if (tfPassword.getText().trim().length() == 0)
			return false;
		if (tfConfirmPassword.getText().trim().length() == 0)
			return false;
		if (tfPhoneNumber.getText().trim().length() == 0)
			return false;
		if (!tfPassword.getText().toString().equals(tfConfirmPassword.getText().toString()))
			return false;
		return true;
	}
}
	