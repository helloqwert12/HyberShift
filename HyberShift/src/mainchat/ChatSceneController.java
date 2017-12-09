package mainchat;

import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import chatsocket.ChatSocket;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextArea;

import dataobject.UserInfo;

public class ChatSceneController {
	boolean isTyping = false;

	@FXML
	JFXListView<String> lvMessage;
	@FXML
	JFXTextArea taEdit;
	
	ChatSocket chatsocket;
	Socket socket;
	UserInfo userInfo = UserInfo.getInstance();
	ObservableList<String> itemList = FXCollections.observableArrayList();
	
	public ChatSceneController(){
		
		//lvMessage.setItems(itemList);
		chatsocket = ChatSocket.getInstance();
		socket = chatsocket.getSocket();
		
		//set event for taEdit text change
		taEdit.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				//Emit to 
				socket.emit("is_typing", userInfo.getFullName());
				isTyping = true;
			}
		});
		
		//set event for socket
		socket.on("new_message", new Listener() {
			
			@Override
			public void call(Object... args) {
				
				JSONObject msgjson = (JSONObject) args[0];
				try {
					String msg = msgjson.getString("sender") + " : " + msgjson.getString("message");
					//Update listview message
					Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							lvMessage.getItems().add(msg);
						}
					});
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).on("is_typing", new Listener() {
			
			@Override
			public void call(Object... args) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@FXML
	public void onActionBtnSentClick(){
		System.out.println("btnSent clicked");
		sendMessage();
	}
	
	@FXML
	public void onKeyPressedBtnSentClick(KeyEvent keyevent){
		if (keyevent.getCode().equals(KeyCode.ENTER))
        {
			sendMessage();
        }
	}
	
	private void sendMessage(){
		if (taEdit.getText().trim().length() == 0)
			return;
		
		// Get name of the user from server
		JSONObject msgjson = new JSONObject();
		try {
			msgjson.put("sender", userInfo.getFullName());
			msgjson.put("message", taEdit.getText().toString());
			
			// Emit to server
			socket.emit("new_message", msgjson);
			//sendMessage(taEdit.getText().toString());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		taEdit.clear();
	}
	
	
}
