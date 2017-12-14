package chat;

import java.awt.Graphics;
import java.awt.Point;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import application.Main;
import board.DrawState;
import board.PenDrawing;
import chatsocket.ChatSocket;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;
import com.jfoenix.controls.*;

import dataobject.ListOnline;
import dataobject.SenderTyping;
import dataobject.UserInfo;
import dataobject.UserOnline;

public class ChatSceneController implements Initializable {
	//JFX controls
	@FXML JFXListView<String> lvMessage;
	@FXML JFXTextField taEdit;
	@FXML Label lblUsername;
    @FXML private JFXButton btnRealtimeBoard;
    @FXML private JFXDrawer drawer;
    @FXML private Canvas canvas;
    @FXML private JFXListView<String> lvRoom;
    @FXML private JFXListView<String> lvOnline;
    
    //Board drawing
    GraphicsContext gc;
    PenDrawing penDrawing;
    DrawState drawState;
	
    //Socket
	ChatSocket chatsocket;
	Socket socket;
	UserInfo userInfo = UserInfo.getInstance();
	ObservableList<String> itemList = FXCollections.observableArrayList();
	
	//list user online
	ListOnline listOnline = ListOnline.getInstance();
	
	//typing event
	boolean isSetTyping = false;
	ArrayList<SenderTyping> listTyping;
	
	public ChatSceneController(){
		
		//lvMessage.setItems(itemList);
		chatsocket = ChatSocket.getInstance();
		socket = chatsocket.getSocket();
			
		//set event for socket
		socket.on("new_message", new Listener() {
			
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {	
					@Override
					public void run() {
						JSONObject msgjson = (JSONObject) args[0];		
							//Update listview message
							Platform.runLater(new Runnable() {			
								@Override
								public void run() {
									String sender;
									try {
										sender = msgjson.getString("sender");
										String msg = sender + " : " + msgjson.getString("message");
										int indexToAdd = getMinIndexFrom(listTyping);
										removeSenderFrom(listTyping, sender, lvMessage);
										lvMessage.getItems().add(indexToAdd, msg);
										increaseIndexFrom(listTyping, indexToAdd);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});	
					}
				});			
			}
		}).on("new_drawing", new Listener() {
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						JSONArray jsonarr = (JSONArray)args[0];
						ArrayList<Point> listPoints = convertJsArrToLstPnt(jsonarr);
						penDrawing.setListPoints(listPoints);
						penDrawing.draw(gc);		
					}
				});
				
			}
		}).on("is_typing", new Listener() {		
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						String senderName = (String)args[0];
						lvMessage.getItems().add(senderName + " is typing . . .");
						int index = lvMessage.getItems().size() - 1;
						listTyping.add(new SenderTyping(senderName, index));
					}
				});
			}
		}).on("done_typing", new Listener() {
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {		
						String sender = (String)args[0];
						System.out.println("--listTyping: " + listTyping);
						int index = getIndexFromName(listTyping, sender);
						if (index < 0) return;
						System.out.println("--index of sender in listTyping: " + index);
						removeSenderFrom(listTyping, sender, lvMessage);
						System.out.println("listTyping after remove: " + listTyping);
						decreaseIndexFrom(listTyping, index);
						System.out.println("listTyping after decrease index: " + listTyping);
					}
				});
			}
		}).on("user_online", new Listener() {	
			@Override
			public void call(Object... args) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					JSONObject object = (JSONObject)args[0];
					try {
						String name = object.getString("fullname");
						String email = object.getString("email");
						listOnline.addUserOnline(new UserOnline(name, email));
						//update list view online
						ObservableList<String> olistOnline = FXCollections.observableArrayList(listOnline.getListName());
						System.out.println(olistOnline);
						lvOnline.setItems(olistOnline);
					} catch (JSONException e) {
						e.printStackTrace();
					}	
				}
			});
			}
		}).on("user_offline", new Listener() {
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {	
					@Override
					public void run() {
						JSONObject object = (JSONObject)args[0];
						try {
							String name = object.getString("fullname");
							listOnline.removeUserOnline(name);
							//update list view online
							ObservableList<String> olistOnline = FXCollections.observableArrayList(listOnline.getListName());
							System.out.println(olistOnline);
							lvOnline.setItems(olistOnline);
						} catch (JSONException e) {
							e.printStackTrace();
						}		
					}
				});
			}
		});
		
		//penDrawing
		penDrawing = new PenDrawing();
		drawState = DrawState.ON_UP;
		
		//isTying set
		listTyping = new ArrayList<>();
	}
	
	@FXML
	public void onActionBtnSentClick(){
		System.out.println("btnSent clicked");
		sendMessage();
		isSetTyping = false;
		taEdit.clear();
	}
	
	@FXML
	public void onKeyPressedBtnSentClick(KeyEvent keyevent){
		if (keyevent.getCode().equals(KeyCode.ENTER))
        {			
			sendMessage();
			isSetTyping = false;
			taEdit.clear();
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
					
					//clear gc
					gc.clearRect(canvas.getLayoutX(), canvas.getLayoutY(), canvas.getWidth(), canvas.getHeight());
					penDrawing.clear();
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
    
    @FXML
    void onMouseDraggedCanvas(MouseEvent event) {
    	if (drawer.isHidden()) return;
    	
    	penDrawing.addPoint(new Point((int)event.getX(), (int)event.getY()));
    	penDrawing.draw(gc);
    	drawState = DrawState.ON_DRAW;
    	//emit to server
    	JSONArray jsonArrPoints = convertLstPntToJsArr(penDrawing.getListPoints());
    	socket.emit("new_drawing", jsonArrPoints);
    }
    
    @FXML
    void onMouseReleasedCanvas(MouseEvent event) {	
    	penDrawing.clear();
    }
    
    @FXML
    void onKeyTypedTaEdit(KeyEvent event) {
    	if (!isSetTyping && !event.getCode().equals(KeyCode.ENTER) && taEdit.getText().length() > 0){
    		System.out.println("is_typing");
    		isSetTyping = true;
    		socket.emit("is_typing", userInfo.getFullName());
    	}
    	else{
    		if (taEdit.getText().length() <= 0){
    			System.out.println("done_typing");
    			isSetTyping = false;
    			socket.emit("done_typing", userInfo.getFullName());
    		}
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
		
		//set canvas
		gc = canvas.getGraphicsContext2D();
		
		//update list online to lv
		ObservableList<String> olist = FXCollections.observableArrayList(listOnline.getListName());
		lvOnline.setItems(olist);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//update UI
		updateUI();	
	}
	
	private JSONArray convertLstPntToJsArr(ArrayList<Point> list){
		JSONArray jsonArrPoints = new JSONArray();
    	for(int i=0; i<list.size(); i++){
    		JSONObject point = new JSONObject();
    		try {
				point.put("x", list.get(i).x);
				point.put("y", list.get(i).y);
				jsonArrPoints.put(point);
			} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    	
    	return jsonArrPoints;
	}
	
	private ArrayList<Point> convertJsArrToLstPnt(JSONArray jsonarr){
		ArrayList<Point> lstPnt = new ArrayList<>();
		for(int i=0; i<jsonarr.length(); i++){
			try {
				JSONObject point = jsonarr.getJSONObject(i);
				lstPnt.add(new Point(point.getInt("x"), point.getInt("y")));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lstPnt;
	}
	
	//--This method only use for SenderTyping list
	//Return minimun value of index in SenderTyping class
	private int getMinIndexFrom(ArrayList<SenderTyping> list){
		int min = list.get(0).getIndex();
		for(int i=1; i<list.size(); i++){
			if (list.get(i).getIndex() < min)
				min = list.get(i).getIndex();
		}
		
		return min;
	}
	
	//--This method only use for SenderTyping list
	private void removeSenderFrom(ArrayList<SenderTyping> list, String sender, JFXListView<String> lv){
		//check if sender in listTyping, then remove typing
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getSenderName().equals(sender)){
				
				//remove in listview first
				lv.getItems().remove(list.get(i).getIndex());
				
				//then remove in listTyping
				list.remove(i);
			}
		}
	}
	
	private int getIndexFromName(ArrayList<SenderTyping> list, String sender){
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getSenderName().equals(sender)){
				return list.get(i).getIndex();
			}
		}
		return -1;
	}
	
	private void increaseIndexFrom(ArrayList<SenderTyping> list, int id){
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getIndex() >= id){
				list.get(i).setIndex(list.get(i).getIndex() + 1);
			}
		}
	}
	
	private void decreaseIndexFrom(ArrayList<SenderTyping> list, int id){
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getIndex() >= id){
				list.get(i).setIndex(list.get(i).getIndex() - 1);
			}
		}
	}
	
}
