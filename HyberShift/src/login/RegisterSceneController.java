package login;

import org.json.JSONException;
import org.json.JSONObject;

import chatsocket.ChatSocket;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;

import dataobject.UserInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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
		socket = ChatSocket.getInstance().getSocket();
		socket.on("register", new Listener() {
			
			@Override
			public void call(Object... args) {
				new Alert(Alert.AlertType.INFORMATION, "Register successfully").show();
			}
		});
	}
	
	public void onActionBtnConfirm(){
		System.out.println("BtnConfirm clicked");
		
		//Push data
		userInfo = new UserInfo(tfEmail.getText().toString(), tfPassword.getText().toString(), tfPhoneNumber.getText().toString(), tfName.getText().toString());
		
		//Convert to JSONObject
		JSONObject userjson = new JSONObject();
		try {
			//userjson.put("userid", userInfo.getUserid());
			userjson.put("email", userInfo.getEmail());
			userjson.put("fullname", userInfo.getFullName());
			userjson.put("password", userInfo.getPassword());
			userjson.put("phone", userInfo.getPhone());
			userjson.put("linkavatar", userInfo.getLinkAvatar());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		socket.emit("register", userjson);
	}
	
	private boolean isValid(){
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
		if (tfPassword.getText() != tfConfirmPassword.getText())
			return false;
		return true;
	}
}
