package chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import application.Main;
import chatsocket.ChatSocket;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import dataobject.ListRoom;
import dataobject.Room;
import dataobject.UserInfo;

public class CreateRoomSceneController {
	@FXML
	JFXTextField tfRoomName;
	@FXML
	JFXTextField tfMembers;
	@FXML
	JFXButton btnConfirm;
	
	//Socket
	Socket socket;
	
	//User
	UserInfo userInfo;
	
	//Room
	ListRoom listRoom = ListRoom.getInstance();
	
	public CreateRoomSceneController(){
		userInfo = UserInfo.getInstance();
		socket = ChatSocket.getInstance().getSocket();
		
		socket.on("create_room_result", new Listener() {
			
			@Override
			public void call(Object... args) {
				JSONObject jsoninfo = (JSONObject) args[0];
				JSONArray invalid;
				JSONArray jsarrMembers;
				try {
					invalid = jsoninfo.getJSONArray("invalid");
					jsarrMembers = jsoninfo.getJSONArray("members");
					Room room = new Room();
					for(int i=0; i<jsarrMembers.length(); i++){
						room.addMemebers(jsarrMembers.getString(i));
					}
					//Add to listRoom
					room.setName(jsoninfo.getString("room_name"));
					room.setId(jsoninfo.getString("room_id"));
					listRoom.addRoom(room);
					
					if (invalid.length() == 0){
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								new Alert(AlertType.INFORMATION, "Create room successfully!").show();
								Main.showMainChatScene();
							}
						});
					}
					else{
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								new Alert(AlertType.INFORMATION, "Create room successfully, emails " + invalid + " is not valid.").show();
							}
						});	
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).on("room_created", new Listener() {	
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						JSONObject object = (JSONObject)args[0];
						try {
							String roomId = object.getString("room_id");
							String roomName = object.getString("room_name");
							System.out.println("roomName: " + roomName);
							listRoom.addRoom(new Room(roomId, roomName, null));
							System.out.println("Create room form: " + listRoom.getListRoomName());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				
			}
		});;
		
	}
	
	@FXML
	public void onActionBtnConfirmClick(){
		System.out.println("btnConfirm click");
		
		//Check valid
		if (!isValid()){
			new Alert(AlertType.INFORMATION, "Please fill all fields").show();
			return;
		}
		
		//Get text from tfRoomName and tfMembers
		String roomName;
		String[] members;
		//--tfRoomName
		roomName = tfRoomName.getText().toString();
		
		//--tfMembers
		members = tfMembers.getText().replaceAll("\\s+","").split(",");
		
		
		try {
			JSONObject roomjson = new JSONObject();
			
			roomjson.put("room_name", roomName);
			roomjson.put("creator_name", userInfo.getFullName());
			roomjson.put("creator_email", userInfo.getEmail());
			roomjson.put("members", members);
			
			//Emit to server
			socket.emit("create_room", roomjson);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isValid(){
		if (tfRoomName.getText().trim().length() == 0)
			return false;
		if (tfMembers.getText().trim().length() == 0)
			return false;
		
		return true;
	}
	
	
}
