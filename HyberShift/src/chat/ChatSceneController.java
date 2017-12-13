package chat;

import java.awt.Graphics;
import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import application.Main;
import chatsocket.ChatSocket;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;
import com.jfoenix.controls.*;

import dataobject.UserInfo;

public class ChatSceneController implements Initializable {
	@FXML JFXListView<String> lvMessage;
	@FXML JFXTextArea taEdit;
	@FXML Label lblUsername;
    @FXML private JFXButton btnRealtimeBoard;
    @FXML private JFXDrawer drawer;
	
	ChatSocket chatsocket;
	Socket socket;
	UserInfo userInfo = UserInfo.getInstance();
	ObservableList<String> itemList = FXCollections.observableArrayList();
	
	public ChatSceneController(){
		
		//lvMessage.setItems(itemList);
		chatsocket = ChatSocket.getInstance();
		socket = chatsocket.getSocket();
			
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
	
	@FXML
	public void onActionBtnCreateRoomClick(){
		System.out.println("btnCreateRoom click");
		Main.showCreateRoomScene();
	}
	
    @FXML
    void onActionBtnRealtimeBoardClick() {
    	if (drawer.isShown()){
    		Platform.runLater(new Runnable() {
				@Override
				public void run() {
					drawer.close();
				}
			});
    	}
    	else{
    		Platform.runLater(new Runnable() {
				@Override
				public void run() {
					drawer.open();
				}
			});
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
	
	private void updateUI(){
		//update Username label when signing
		lblUsername.setText(userInfo.getFullName());
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//update UI
		updateUI();	
	}
	
	
}
