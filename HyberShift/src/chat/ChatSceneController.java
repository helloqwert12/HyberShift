package chat;

import java.awt.Graphics;
import java.awt.Point;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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

import dataobject.UserInfo;

public class ChatSceneController implements Initializable {
	//JFX controls
	@FXML JFXListView<String> lvMessage;
	@FXML JFXTextArea taEdit;
	@FXML Label lblUsername;
    @FXML private JFXButton btnRealtimeBoard;
    @FXML private JFXDrawer drawer;
    @FXML private Canvas canvas;
    
    //Board drawing
    GraphicsContext gc;
    PenDrawing penDrawing;
    DrawState drawState;
	
    //Socket
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
		});
		
		//penDrawing
		penDrawing = new PenDrawing();
		drawState = DrawState.ON_UP;
	
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
	
}
