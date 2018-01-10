package chat;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.control.textfield.AutoCompletionBinding.AutoCompletionEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import Tools.ImageUtils;
import application.Main;
import board.DrawState;
import board.PenDrawing;
import chatsocket.ChatSocket;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;
import com.jfoenix.controls.*;

import dataobject.Journal;
import dataobject.ListJournal;
import dataobject.ListMessage;
import dataobject.ListOnline;
import dataobject.ListRoom;
import dataobject.Message;
import dataobject.Room;
import dataobject.SenderTyping;
import dataobject.UserInfo;
import dataobject.UserOnline;


public class ChatSceneController implements Initializable {
	//JFX controls
	@FXML JFXListView<Message> lvMessage;
	@FXML JFXTextField taEdit;
	@FXML Label lblUsername;
	@FXML Label lblRoomName;
    @FXML private JFXButton btnRealtimeBoard;
    @FXML private JFXButton btnPlan;
    @FXML private JFXDrawer drawer;
    @FXML private Canvas canvas;
    @FXML private JFXDrawer drawerPlan;
    @FXML private JFXListView<String> lvRoom;
    @FXML private JFXListView<String> lvOnline;
    @FXML private JFXListView<Journal> lvPlan;
    @FXML private AnchorPane pnlPlan;
    @FXML private Circle cimgAvatar;
    
    //Plan
    @FXML private JFXTextField tfNewTask;
    @FXML private JFXTextField tfPerformers;
    @FXML private JFXButton btnCreateTask;
    
    //Test
    @FXML private Button btnImage;
    @FXML private ImageView imgview;
    @FXML private Button btnChooseImg;
    
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
	
	//list room
	ListRoom listRoom = ListRoom.getInstance();
	Room currRoom; // current Room
	
	//list message
	ListMessage listMessage = ListMessage.getInstance();
	
	//list journal
	ListJournal listJournal = ListJournal.getInstance();
	
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
								try {
									String sender = msgjson.getString("sender");
									String msg = sender + " : " + msgjson.getString("message");
									String id = msgjson.getString("id");
									String imgstring = msgjson.getString("imgstring");
									Message message = new Message(id, msg, sender, imgstring, 0);
									if (id.equals("public"))
										System.out.println("public has new message");
									else{
										Room tempRoom = listRoom.getRoomById(id);
										System.out.println(tempRoom.getName() + " has new message");
										// if user is in current room, then display
										if (currRoom.getId().equals(id)){
											int indexToAdd = getMinIndexFrom(listTyping);
											removeSenderFrom(listTyping, sender, lvMessage);
											if (indexToAdd == 0)
												lvMessage.getItems().add(message);
											else
												lvMessage.getItems().add(indexToAdd, message);
										
											increaseIndexFrom(listTyping, indexToAdd);
										}
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});	
					}
				});			
			}
		})
		.on("new_drawing", new Listener() {
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						JSONObject object = (JSONObject)args[0];
						String roomId;
						try {
							roomId = object.getString("room_id");
							if (currRoom.getId().equals(roomId)){
								if (drawer.isHidden()) return;
								JSONArray jsonarr = object.getJSONArray("points");
								ArrayList<Point> listPoints = convertJsArrToLstPnt(jsonarr);
								penDrawing.setListPoints(listPoints);
								penDrawing.draw(gc);		
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				});
				
			}
		}).on("is_typing", new Listener() {		
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						JSONObject object = (JSONObject)args[0];
						
						try {
							String senderName = object.getString("sender");
							String id = object.getString("id");
							String imgstring = object.getString("imgstring");
							if (currRoom.getId().equals(id)){
								lvMessage.getItems().add(new Message(id, " is typing ...", senderName, imgstring, 0));
								int index = lvMessage.getItems().size() - 1;
								listTyping.add(new SenderTyping(senderName, index));
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					}
				});
			}
		}).on("done_typing", new Listener() {
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {		
						JSONObject object = (JSONObject)args[0];
						try {
							String sender = object.getString("sender");
							String id = object.getString("id");		
							if (currRoom.getId().equals(id)){
								int index = getIndexFromName(listTyping, sender);
								if (index < 0) return;
								
								removeSenderFrom(listTyping, sender, lvMessage);
								decreaseIndexFrom(listTyping, index);
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
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
							ObservableList<String> oroomName = listRoom.getOListRoomName();
							System.out.println(oroomName);
							lvRoom.setItems(oroomName);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				
			}
		}).on("room_change", new Listener() {
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						lvMessage.getItems().clear();
						JSONObject object = (JSONObject)args[0];
						try {
							String id = object.getString("id");
							String sender = object.getString("sender");
							String message = object.getString("message");
							String imgstring = object.getString("imgstring");
							int timestamp = object.getInt("timestamp");
							if (currRoom.getId().equals(id)){
								Message msg = new Message(id, message, sender, imgstring, timestamp);
								listMessage.addMessage(msg);
								
								lvMessage.setItems(listMessage.getOList());
							}
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				});
			}
		}).on("new_image", new Listener() {
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {		
						try {
							imgview.setImage(ImageUtils.decodeBase64BinaryToImage((String)args[0]));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		}).on("create_task", new Listener() {
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {	
					@Override
					public void run() {
						JSONObject object = (JSONObject)args[0];
						Journal journal = new Journal();
						try {
							//if not the current room, then stop
							if (!object.getString("room_id").equals(currRoom.getId()))
									return;
							
							//format data from calendar
							Date date = new Date(object.getInt("start_day"));
							DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
							String dateFormatted = formatter.format(date);
							
							journal.setId(object.getString("task_id"));
							journal.setWork(object.getString("work"));					
							journal.setStartDay(dateFormatted);
							journal.setEndDay(String.valueOf(object.getInt("end_day")));						
							journal.addPerformer(object.getString("performers"));
							
						
							//add to listview
							listJournal.addJournal(journal);
							lvPlan.setItems(listJournal.getOListJournal());
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
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
					pnlPlan.setVisible(false);
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
    	JSONObject object = new JSONObject();
    	try {
			object.put("room_id", currRoom.getId());
			//emit to server
	    	JSONArray jsonArrPoints = convertLstPntToJsArr(penDrawing.getListPoints());
	    	object.put("points", jsonArrPoints);
	    	socket.emit("new_drawing", object);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
    
    @FXML
    void onMousePressedCanvas(MouseEvent event) {
    	if (drawer.isHidden()) return;
    	penDrawing.clear();
    }
    
    @FXML
    void onMouseReleasedCanvas(MouseEvent event) {	
    	penDrawing.clear();
    }
    
    @FXML
    void onKeyTypedTaEdit(KeyEvent event) {
    	if (!isSetTyping && !event.getCode().equals(KeyCode.ENTER) && taEdit.getText().length() > 0){
    		isSetTyping = true;
    		JSONObject object = new JSONObject();
    		try {
				object.put("sender", userInfo.getFullName());
				if (!userInfo.getAvatarString().equals("null"))
					object.put("imgstring", userInfo.getAvatarString());
				else
					object.put("imgstring", "null");
				if (currRoom == null)
					object.put("id", "public");
				else
					object.put("id", currRoom.getId());
				
				socket.emit("is_typing", object);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    	else{
    		if (taEdit.getText().length() <= 0){
    			System.out.println("done_typing");
    			isSetTyping = false;
    			JSONObject object = new JSONObject();
        		try {
    				object.put("sender", userInfo.getFullName());
    				if (!userInfo.getAvatarString().equals("null"))
    					object.put("imgstring", userInfo.getAvatarString());
    				else
    					object.put("imgstring", "null");
    				if (currRoom == null)
    					object.put("id", "public");
    				else
    					object.put("id", currRoom.getId());
    				
    				socket.emit("done_typing", object);
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
    @FXML
    void onActionBtnPlanClick(ActionEvent event) {
    	if (pnlPlan.isVisible())
    		pnlPlan.setVisible(false);
    	else
    		pnlPlan.setVisible(true);
    }
    
    @FXML
    void onActionBtnCreateTaskClick(ActionEvent event) {
    	// Xử lý task ở đây
    	Journal journal = new Journal();
    	
    	//Check valid data
    	if (tfNewTask.getText().replaceAll("\\s+","").split(",").toString().length() == 0 || 
    			tfPerformers.getText().replaceAll("\\s+","").split(",").toString().length() == 0){
    		new Alert(AlertType.WARNING, "Look like you don't complete all stuff!").show();
    		return;
    	}
    	
    	JSONObject object = new JSONObject();
    	try {
    		object.put("room_id", currRoom.getId().toString());	//use room id to push to database
			object.put("work", tfNewTask.getText().toString());
			object.put("performers", tfPerformers.getText().toString());
			object.put("start_day", Calendar.getInstance().getTimeInMillis());
			object.put("end_day", 0);
			//emit to server
			socket.emit("create_task", object);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	
	private void sendMessage(){
		if (taEdit.getText().trim().length() == 0)
			return;
		
		// Get name of the user from server
		JSONObject msgjson = new JSONObject();
		try {
			msgjson.put("imgstring", userInfo.getAvatarString());
			msgjson.put("sender", userInfo.getFullName());
			msgjson.put("message", taEdit.getText().toString());
			msgjson.put("timestamp", Calendar.getInstance().getTimeInMillis());
			if (currRoom == null)
				msgjson.put("id", "public");
			else
				msgjson.put("id", currRoom.getId());
			
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
		pnlPlan.setVisible(false);
		
		lblRoomName.setText("Hybershift public chat");
		
		
//		ListMessage test = ListMessage.getInstance();
//		Message msg1 = new Message("msg1", "test 1", "Tran Minh Quan", "/9j/4AAQSkZJRgABAQAAAQABAAD//gA7Q1JFQVRPUjogZ2QtanBlZyB2MS4wICh1c2luZyBJSkcgSlBFRyB2NjIpLCBxdWFsaXR5ID0gOTUK/9sAQwACAQEBAQECAQEBAgICAgIEAwICAgIFBAQDBAYFBgYGBQYGBgcJCAYHCQcGBggLCAkKCgoKCgYICwwLCgwJCgoK/9sAQwECAgICAgIFAwMFCgcGBwoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoK/8AAEQgBLAH+AwEiAAIRAQMRAf/EAB8AAAEFAQEBAQEBAAAAAAAAAAABAgMEBQYHCAkKC//EALUQAAIBAwMCBAMFBQQEAAABfQECAwAEEQUSITFBBhNRYQcicRQygZGhCCNCscEVUtHwJDNicoIJChYXGBkaJSYnKCkqNDU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6g4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2drh4uPk5ebn6Onq8fLz9PX29/j5+v/EAB8BAAMBAQEBAQEBAQEAAAAAAAABAgMEBQYHCAkKC//EALURAAIBAgQEAwQHBQQEAAECdwABAgMRBAUhMQYSQVEHYXETIjKBCBRCkaGxwQkjM1LwFWJy0QoWJDThJfEXGBkaJicoKSo1Njc4OTpDREVGR0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoKDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uLj5OXm5+jp6vLz9PX29/j5+v/aAAwDAQACEQMRAD8A/fykLovVhUd1MIkzmuV8afEHQ/B2jXfiHxFrNrp9hYW0lxfX17cLFDbwopZ5JHYhURVBJYkAAEmgDrDPEOsgo+0w/wB8V853X/BQn9khWxF+1L8PG/3fGtif/atQD/goT+ykx+X9pzwAfp4zsv8A47V8k+zJ54dz6T+0w/3xR9ph/vivm4f8FBf2V2+7+0v4CP08Y2X/AMdpw/b+/ZdPT9pLwJ/4WFl/8do5J9mHPDufR/2mH++KPtMP98V85L+3z+zE3C/tG+Bj9PF1n/8AHacP28/2aG+7+0R4IP08WWf/AMdo5J9mHPDufRf2mH++KPtMP98V87D9u39m1vu/tB+Cj9PFdp/8cp3/AA3R+zkenx/8Gf8AhU2n/wAco5J9mHPDufQ/2mH++KPtMP8AfFfPK/ty/s6t934+eDT9PFFp/wDHKeP23v2e2+78d/CB+nia1/8AjlHJPsw54dz6D+0w/wB8UfaYf74r5+H7bPwAPT45eET9PEtr/wDHKcP20/gM33fjb4UP08R23/xyjkn2Yc8O57/9ph/vij7TD/fFeBD9s34Ft9340eFj9PEVt/8AHKUftk/A89PjJ4XP/cwW/wD8XRyT7MOeHc98+0w/3xR9ph/vivBR+2J8Ez0+MPhk/wDcft//AIunD9r/AODB6fFzw3/4Prf/AOLo5J9mHPDue8faYf74o+0w/wB8V4SP2uvg6enxY8On/uOQf/F0v/DW3whPT4q+Hv8Awdwf/F0ck+zDnh3PdftMP98UfaYf74rwwftZfCVvu/FHQD9Nag/+LpR+1d8KD0+J2g/+DmH/AOLo5J9mHPDue5faYf74o+0w/wB8V4eP2qvhYenxK0I/TWIf/iqX/hqb4YHp8R9E/wDBvD/8VRyT7MOeHc9v+0w/3xR9ph/vivER+1J8Mj0+Iuin/uLRf/FUv/DUPw1PT4haN/4NYv8A4qjkn2Yc8O57b9ph/vij7TD/AHxXiQ/af+Gx6fEHRv8Awaxf/FUv/DT3w3/6KBo//g0i/wDiqOSfZhzw7ntn2mH++KPtMP8AfFeJ/wDDTnw5PTx/o/8A4NIv/iqX/hpv4d/9D7pH/gzi/wDiqOSfZhzw7ntf2mH++KPtMP8AfFeKf8NNfDv/AKH3SP8AwZxf/FUf8NNfDv8A6H3SP/BnF/8AFUck+zDnh3Pa/tMP98UfaYf74rxT/hpr4d/9D7pH/gzi/wDiqP8Ahpr4d/8AQ+6R/wCDOL/4qjkn2Yc8O57X9ph/vij7TD/fFeKf8NN/DodfH2kf+DSL/wCKpP8Ahpz4cnp4/wBH/wDBpF/8VRyT7MOeHc9s+0w/3xR9ph/vivE/+GnvhwOvxA0f/wAGkX/xVIf2n/hsOvxB0b/waxf/ABVHJPsw54dz237TD/fFH2mH++K8R/4ah+Gn/RQ9F/8ABrF/8VR/w1J8Mh1+Iui/+DaL/wCKo5J9mHPDue3faYf74o+0w/3xXiB/an+F46/EjQx/3F4f/iqaf2q/hWDg/EzQv/BxD/8AF0ck+zDnh3PcftMP98UfaYf74rw0/tW/ClfvfE3QR9dZh/8Ai6af2svhIOvxR8Pj661B/wDF0ck+zDnh3PdPtMP98UfaYf74rwo/tb/CBfvfFbw8PrrcH/xdNP7XfwcXr8WfDg+uuQf/ABdHJPsw54dz3f7TD/fFH2mH++K8HP7X/wAGF+98XPDY+uvW/wD8XTT+2L8Ex1+MPhkfXX7f/wCLo5J9mHPDue9faYf74o+0w/3xXgZ/bK+BynDfGbwuPr4ht/8A4umn9s/4Ejr8avCo/wC5itv/AI5RyT7MOeHc9++0w/3xR9ph/vivn8/tqfAQdfjd4TH/AHMlt/8AHKa37bX7P6/e+OfhEfXxLa//AByjkn2Yc8O59BfaYf74o+0w/wB8V89n9uD9nodfjx4PH/cz2v8A8cpD+3H+zsOvx78HD/uaLX/45RyT7MOeHc+hftMP98UfaYf74r54P7dH7OK/e+P/AIMH18U2n/xymn9u79mwdf2hPBQ/7mu0/wDjlHJPsw54dz6J+0w/3xR9ph/vivnRv29P2Zl+9+0T4IH18W2f/wAdpp/b6/ZhX737R3gYfXxdZ/8Ax2jkn2Yc8O59G/aYf74o+0w/3xXzef8AgoB+y2vX9pPwGPr4wsv/AI7SH/goL+ysv3v2l/AQ+vjGy/8AjtHJPsw54dz6RFxCf4xSiWM9HFfN0H/BQj9k8yYk/ae+H6+7eM7H/wCO10XhD9uT9lPxXrdn4b0P9pv4fXuo6hdR21hYWvjOxkmuZ3YKkUaLKWd2YhQoBJJAFLkn2Dnh3PcaKq2F4s6gg9atVJRm+IJzBbswPavlX/goF4okT9l74l2QlP73wHrCEZ9bKYV9Q+LiRaPg9jXxj/wUHkkP7Pnj5PMbB8HaoCM/9OslXT+NepM/gZ+ItvEc1et4snGKht4sNx6+lX7W33A819KfOkttGSOnStO3izt+gqvBbHb15+laFvD0H0oAsWcZ3j/CtC2iI6+tV7OI7x/hWnZ2u48kj8KAJrSLCDjvWhFFlQMVFbW21Que/pWhb2mQMnH4UAFnasCDj8K07W3I5P8AKm2VryMH9K0oLfbg+vtQAkFq20cHmtK2tSAMjoKZBDjFaFsnT5e1ADra1YkcY44q9bWxJ6dKS1j3MBjtV+2hoALe2JAGOvQVehtmyBg0W0XA/wAKvW8PbFABBatnpVyO1II4/SnW8XIP9KuxW+6gCO2tWABwatw2xBzg1NBa5HX9KtQ2nPBoAZBakYJq1Han3/KpobcjBq1Ba7uf6UAQQ2pwDg9anitCeMGrcViNo+brViKyA53dKAKkdic8U9rJuAR+NaMUIJ6fpUv2bcRjGBQBmxWLDqKsDT2wOP0q9Ha8cGpxb4AHl9qAMr+z29P0o/s9vT9K1fs//TOj7P8A9M6AMr+z29P0o/s9vT9K1fs//TOj7Pn/AJZ0AY0li3XFMWwYA8VsyWnHP8qj+zhSQcc+1AGM9g4PI4qvJasOMGt2SEf3e1VZLIdd1AGMbU8nn8qhktSe3WtiWy2g4aoJLX3H5UAYk9qQx/wqlLaN5mADW7cWvJwapy2pDdf0oAyLi2PUD8apXFq3oenpW3cwcYH8qpz2+49e1AGDcWrZ4BqrPakjoa257U/5FU54cA/4UAYdxasV79ao3NqTkY5rcuYOPxqjcwjnj9KAOfu7VhIeDVC4tW5XvW9dwjecVn3EOM/SgDDubYgZC1n3lqTjr0reuIcrx3rPvIMYI9KAMCe2YnofyqncQnBBzW3PDzms27gYDn1oAxryMhPxrOuIq2bmLeNuO9ULi27E0AYl5Ed54/Os66iO1j7VuXdpkk5/Ss26gJyuaAMS4iJGKo3ER64rZu7QquN2eewrPuIcfl6UAYOs32n6LaSalql5Hb28ePMmlbCrkgDJPuQPxr0D9jnMH7YHwqnHGz4laE35ahAa8h+PkW34Waqf+uH/AKPjr2D9kqIj9rD4YnJGPiJopH/gdDWc3pJeX+ZpBe9F+Z/Rv4PvmurdWLZ4rpK4v4dMxs48sT8vrXaDoK+bPoDE8Xf8eb/Q18Y/8FBBn9n/AMej/qUNT/8ASWSvs7xd/wAeb/Q18Y/8FBOfgB49/wCxQ1P/ANJZKun8a9SZ/Az8VreL5s4rQsoRzmqVkh3Vp20f4cV9KfOlq3j4q/bw9Kis4hjpV+1iGQNtAEtlGN4+la1jFyOKrW8IA4A/KtGyjJx8tAE8EfIyf0rTtIflBqpBEMjitS1j4AIFAE1nHhhz3rVs4A/aq9lCN33R+VaUMWBwAKAJba1HU1egtQMZ7U2zi6HFXbSA78H1oAls7b5wQO1aNtag0y1tzkEjtWhbQHgYoAW3tRjp+lXre0G2m28HHTvV+2tzlRigBbe2XOcfpV23tQen51Jb24DZCj8qu29tk/dH5UANgtlOB6VahtlLDtU9vbA87R78VZgthkYQUARxWwIxVu2tlwBU8dsAMFR+VWra2GR8o/KgCOG2G0DH0qxHbD/HirMFsNuCo/KrEdqNwylAFOO1H+FTJbDg+lX47Ve6Cphar3QfSgDPjtlNWlsBtHy9vSrkdquPu08Q4GMUAUfsA/umj7AP7pq+ICTjmnfZT6mgDO+wD+6aRrEAE7a0hb4HIpfs47rQBjSWozURtQOa2ntVyPkFRNar/cFAGI9qM4qJ7YdR2FbUlqpP3BVaW2XJyooAxp7VduPeq8lsAf5Vsz2w2Y2jr6VXkthk/KOnpQBiT2o5z/KqM1sM1vXFt1wo+mKpS2wJPyD6UAYlzbDbtxVSS1HJx+lblxa/KcKPfiqM9uR0FAGLPajOKoT2wGSelbc8GGxiqNxAQPagDEuLYbao3NsvXNbd1bnHTvVK4gJOcfpQBg3dsrMRWdcWwwcjtW/d2+CcrWXcwEE56fSgDHuLUYIP8qzry2BIyPxrcuI/aqFzEPQdKAMKe1Gcis67tQQR71t3cZ3EYqhdxcE4/SgDn7u3CDPvWfcxdsVt3cWQRisy8ix0oAyLqIcisu5iBY1u3EfBOBWVdx8n/CgDIvIsCsy5jGfwrbuI8jp9c1QuYRjgD8qAPNvj9Hj4Var/ANsP/R8dewfsmx4/at+Gf/ZQtG/9Loa8l/aDjI+FOrcf88P/AEfHXsP7KEeP2qfhmSOnxA0b/wBLoayn9r0/zNIbx9f8j+hn4c/8ecf+7XaL0H0ri/hz/wAecf8Au12i9B9K+cPoDE8Xf8eb/Q18Zf8ABQEZ+AXjsf8AUo6n/wCkslfZvi7/AI83+hr4z/b+GfgL46H/AFKWp/8ApLJV0/jXqTP4Gfi/ZwncDWlbJt6jqKq2cXzCtGGI19KfOl2yUFQa0LWLkYHWqtlFwM1pWkQyP8KALkELBR9Kv2UZ4wKggjAA4q/ZRHI4/SgC1bW7MR8tadrA+B8o4qtaQkgCtS1i+UdPyoAns4TuBrSt4GY/KKq2cXzYrTtYiDQBas7Zgo+X9a0LWAbhkVFZxcZq/axAN/8AWoAtQRBAGfp9KvWsakggfpUUMG4BQK0LK0BPAoAkghHp3rQtrcnHHQ1FBakEYWtS1tOBgUALbRqDjHf0q/axKTwO1RW9p82PetGztc5GKAHW0AxytXLe1O4EL9Kdb2hx+FXre05BI4+lAEcNq/GUq3bWrBgSv0qaK145q3bWp9OnTigCOG1YgHZViK1ORhe9WYbX5RmrEdoeCBQBXjtXzyn5VMtqxH3atxWuTkVMlqTwRz9KAKkdo/8Adpwts8Bavx2me36UqWnzcCgCnHZkNlk4qT7Mn/PMfnV1LQkcj9Kd9jP90/lQBnNa88JSfZT/AHP5VpfYz/dP5Un2P2/SgDLltX7JULWr44SteS0PPFQtaYHSgDIe1OeFqtJavz8tbT2nOSOPpVaS0Pp+lAGNJZuw5Wq0tq45K/lW21ocng1VuLQ5xjj6UAYs9qxOQvFUJrVgSdvFb8trgHI4+lUbm06jFAGJcQYPT9Ko3EA9K27m14zjFUri09B2oAw7iEAnIqjcQDbjFbdxaZOQP0qlPacHj6UAYVzCAOR3qhcxoRjFbl5akJ071m3Fp6jjtxQBj3MAOeKy7y3JyAPpW7dwFSVrMuIvmNAGHc27jPy1QuIGPRa272LIxxWdNFjNAGDd27lj8vNULuE7Sa27qL5v/rVmXcQ2nigDDuYS3RazL22fso/OtyeIZPFZ17FigDDuISAQRWZdwnnH4VuXUROazLiL5jQBjXMJA6c1n3SgDjPStm9iyMcVl3UXP4UAeb/tCgf8Ko1YEf8APD/0fHXsP7Kaj/hqf4aHH/NQNG/9Loa8i/aHjI+E+rHH/PD/ANHx17D+yrFj9qX4anH/ADP+jf8ApdDWU/ten+ZpDePr/kf0HfDn/jzj/wB2u0XoPpXF/Dn/AI84/wDdrtF6D6V84fQGJ4u/483+hr4z/b9/5IN45/7FLUv/AEmkr7M8Xf8AHm/0NfGn7fQz8CfHA/6lPUv/AEmkq6fxr1Jn8DPxrswc9O9adqpOMjpVOzQBvxrUtYx37elfSnzpcs4s4OK0bRSGAqrZoK0LSP5s+/pQBdtV3YBrSsIgMVUtIwccGtSyiAI4/SgC1axZAx61q2kSlQMVTtY+nHfvWpaoMDPagC1Z2yFh2FaVvCqYIzVayjBetKGMYFAFmzi6HBrStIFyCQfzqrZxHbkLWpaxjjI9KALVtAAAcfrWjZRAYwKrQx/KBjFaNlFjFAFm3gUjJBrQt49qgCoLaLI6GtC3gJAG04+lAElrEWIJFaEEBQfKOtJZW/zDI4rRhtwcADP4UANt0bHStGCE4GR2pltbHGSv6VpW8CkKPXFACRQcAYq1bQY7VJFAuMVbtrcYHH40ANhhyoqxFD2x3qeGBdo471Zit+eFz+FAEEcIyKsQ24YnI/WrEdtzjaevpViK2APA59KAK0VqpFTLYxgZ2kfjVpLdcc/yqwsA2j5e1AGeLRB0U/nS/Zh6H860Ps4/u0v2Y/3DQBnfZh6H86T7Mp7H860vsx/uGk+zf7FAGVLaqOg/Wontl64/WtaS2HUg/lUL2wz8wxQBkyWqZ6frVV7VeuDW1JbjvVaS3AzxxQBjzWwUZH41SuYcHJrcmt12cHv6VTubcdSKAMWWDjGKpXEGcgitua3TaRVK5gUg5x0oAwrmD5elUriAk9O1btxbqVII4HcCqNxb89PyoAwp4ecAd6zrhG5HvXQT267sf0rNurYdh39KAMKeLcCrCs+7j29q3buDC5Ayc1lX0RB5U/hQBj3MCvlmPNZdxbIGwR9Oa3biEAEf0rMuIjuPy0AY17bpjAB/Osy5hVTgZ6Vu3kQxj+dZd1EB27UAYd3EN2cVl3ceAetbl3FySay7uMYIINAGLPENx4rOvItueK2Z4wCeKzb1Bn/CgDEuoRk4rLuY8EnFbtzEOf8ACsq7jGWwe1AGLeqxGMVnTpnr6VsXsfyms6ZBj8KAPM/2iogPhNq5H/TD/wBKI69f/ZXUj9qT4bf9j/o//pdDXk/7RsYHwj1c/wDXv/6UR169+yzGB+1F8N/bx9o//pbDWU/ten+ZpDePr/kf0B/Dn/jzj/3a7Reg+lcX8Of+POP/AHa7Reg+lfOH0BieLv8Ajzf6GvjX9vgZ+BXjgf8AUqal/wCk0lfZXi7/AI83+hr41/b3/wCSGeN/+xU1L/0mkq6fxr1Jn8DPx1s48MOK1LaPr9KpWS/PyK07YY6DtX0p86XbOLI71qWkfI/wqnZAbRkVpWo6cdhQBftIeRWnZRciqdooBHHatKyByOKALcEZDAVpWsROOaqW0ecEgVp2cZ4oAuWUfzZrTtYsmqlnHyCMVqWkXPzH2oAt2Ufy9a07WLOPpVS0iGABitSzhyRk0AWoIztHA6VoWURyKggi4GMVoWEWTgn9KALdpEeOa1LOLgH86p2seAMetalpCQAfzoAs28eDWhZRZzzVa1j3N0FaVpAeeaAJ7ePI5/SrtvbliKit4jjIrStos4HHQUAFvbnd+FXra3OQuKW2iyau20XPSgBkdsauwWxG2nRxc5wOKuRQ7VzgGgBkVuRzUy25yOOtTQjcQNtWFi47UAVktjg1YW3GBle1Txw4qZY8jAAoAqRwDd0xUnkD+8atrbnPGKd5Df3RQBS8gf3jSGAY61e8hv7oo8hh/CKAM17c4qvNbZbPtWvJFxnAqvLD/CRQBkSW5z0qC4tjt4HStV4uegqKe2ABOf0oAw5Lc9f51TubY7f6VtzW23kkVTuYsc8UAYVxbEtj2qjPbnkkdK3biHJIxVC5hBU0AYtxH8v+FUbiI1sXNscYzzVK4t8fxDpQBjTxYNZ93GSpAHete5Ta1ULqIFecUAYk8XJrPvYj6VszQ8kE1nXkXrj8RQBh3MZyc1l3CfNzW5dxkZrLuosEnj34oAyLyLHTisu6i/lW1djvjtWZdDOOO1AGJdREMazLuLgnFbV2PnPFZl4F2HgUAYl3Hxx61l3sWT0rauhx071mXy+goAxrmMgkE1lXcRya2roDLcVl3a8HigDFvYztrOmjrYuwOhFZ04GDxQB5p+0dGR8ItX/7d/8A0ojr139luPH7UPw4P/U+6P8A+lsNeT/tHjPwh1j/ALd//SiOvXf2XR/xk98OcD/mfdI/9LYqyn9r0/zNIbx9f8j9+vhz/wAecf8Au12i9B9K4v4c/wDHnH/u12i9B9K+cPoDE8Xf8eb/AENfG37eoz8DvGw/6lXUf/SaSvsnxd/x5v8AQ18b/t6f8kP8bf8AYq6j/wCk0lXT+NepM/gZ+QFlF8wOK07WLnpWfZ9R9a1LPr+FfSnzpoWcZA6Vp2kQOAfTmsa81nTdB0/+0dVuRFEMAHBJZjwFUDliewHJqO18Y6uALlfAGtG3x/rAIN+PXy/N3/pn2rCpiaNKXLJ69km9O7snZebMp1qcJcrevzf5HZWkRyOO1adjHkgVh+HdYstesE1LTJy8TEr8ylWVgcFWVgCpBBBBGRW7Yq2fvGtYyjOKlF3TNIyUldPQ0rWLgZxWraRDArLtFbA57+tatkrbl57iqGaVnFg8Y61qWsYPQdqpWgG7oOtO8T+JdP8ABfhu78U6lBNJb2UXmSpbqC7DIHAJA7+oqZzjCLlJ2S1FKSjFt7I3rOIYwAK1LOM5BxVCzjbg1qWaNkVQy9bxZXtWhYxcg4/OqtpGeAa07KPODgUAWrWPgfWtS1i+UcVRgjxwM1qWqNgcUAWbKMBua1LaIY6VUsY8P90VpwxkDgAcUATW8QxmtO2i4X6VVtIcjkVp28QG3gUAS20fIwPzq9axDpUUEfI4HSr1tH04FADo4sEVchiyBkUkMIKjK1et4F4yo9qAEtrcZyR0q3HbDHPfpToosHoOtWbeJmJwKAI47YdSKlS15qeO3IA4qZbc5HFAEC22RwKX7IfQVcSBiOgpfIb+6KAKX2Q+go+yH0FXfIb+6KDAwGdooAzpLYdh2qvNa47DNakkfXgdKrzxE8be1AGW9sM5xVe4iwvIq/cROD1qCeL5fmFAGTcxcfjVC5iHWtiaIHhlFVLmEZ5UUAYs8XJqhPGOeK27mEc/IOtULmEBThe1AGLcxjGCKo3EZ6YHSte4j68VQuY/9ntQBh3kXJqhdRYHTtWxeR84wKoXcXU7R0oAxJoyGPFZ17GDmte7j9Bg1mXsZoAyLqIZIrKu4sk8DpW1dKctk1lXaHJ57UAY97Fhf8azLqLPbtWxfIxXFZdyjD8qAMa7jG7pWbeRZUjA61r3ancTzWfcqNvIoAw7qLHasy9hz0FbV8vHyjnNZN4rAnigDHuYuuayruI5Nbl0oAwQKy7pV54HSgDEvIhjHtWXeLtGK3LxVx0FZl0iN1UdPSgDy79o/wD5JDq//bv/AOlEdexfsvR4/ac+HLD/AKHzSP8A0tiryP8AaSRR8INYwo62/wD6UR16/wDswf8AJzXw6/7HzSP/AEtirKf2vT/M0hvH1/yP3y+HP/HnH/u12i9B9K4v4c/8ecf+7XaL0H0r5w+gMTxd/wAeb/Q18b/t5/8AJD/G3/Yraj/6TSV9keLv+PN/oa+N/wBvL/kiHjX/ALFbUf8A0mkq6fxr1Jn8DPyEs1bjitSzRs9O1Z1n1H1rUs+o+tfSnzpS8kX/AMQ7S2u1DJY6W1xAh6ea77N31Cggem810V9rzaPLFEugaleb0zusoA4XnocsMGsXWtI1R7218SeHhG17ZqyNBK+1bmFsFoy38JyoKnoCOeCau2njm8VViPgPXTcAY8kWybSf+um/Zj3zXmc/sJ1Iybi27p2vdWXlurWtvpc4ub2Uppuzbve177f8Nbcvr8QbV/Ddxq+l6Tcfaor5bCKyu0EbNcsyKqnBOBlwSfQGtK28N/EVLb7dbfEFXvQNwtJdNiFmzf3cAeaB2zvJHXB6VhWXgzxDc+GLySUW8GqXGsjVbWAyF44pFZGWJmxyCE2sQONxxnHO7a+Pdca1+y2vw31k6iRtFvIiLCH9TPu2bfcZOP4c8VkpzlZ4lyWitbmWt3fSPW1tH8luQpSetZtaaWuu/brto/8AMtR+PdT1nw9ow8NWEUOqa5PJAqXfzx2TRBvPZgMb9hQqACNzFegJrS1PT/iN4L0mXxVaeM5dbFlEZrzS72xgjE0ajLiF4lUo+ASu4uCeD1yMm18EeIfDegaFqekJHqGqaPczz3durCMXf2gu1wqFuFO99y7sD5QCRnI19X8W+J/F+jT+F/CfgbV7S7voWgkvdWtVhgslYbWkJ3HzCAchU3ZOMkDJqXKr7N+3clOyta+/Kui0b5r3Tvpa+hN58j9q2pWVrX7Lto3e+/5GvqXijXfEOsab4U8AX0Ns+oacdRudVng8z7Pa5VU2ISA0jluN3ACsSDwDjfGrS/iV4T+GOq3M/jB/EOnTwrDfQ31jDFPbh3ULLG0CorAMRuRlJ2kkNxg6t14f1jwJrul+KvCehzapaWWjjStQ0+3dRcGBCGilj3FVdlIYFcgkPxyMGr8VNZ8c/E7wFf8AhjwP4A1a2WVEa7u9WthASqureVFGW3O7EYJIChcnJOAc8Zzyw9ZVOb2lnZLmttpZLRq+9/npYnEc0qVRSvz2dkr226LZ+d/8j2W2DCJmiUMwXKqTgE+maf4O8QWniPR7W/SeyaeSzgmuYLK+W4SFpIw4Adcb1IOVbADDBAwaSxIAJJ7V45+znrd78M/Auj6lqfg7XNRg17wR4fn0qTRdKkuhLNHp0cL2zlARA3yIweYpERIPn+Vse45WaPWSume4X/xI+HPhqx0/U/Efj/RNPttUCnTLi91WGKO7DAEeUzMBJkEEbc5BHrS6B8Y/BC+EJfGnjHXdO8PWMevahpYn1fU4oY3ktb2e1yHcqMuYC4XqAcc4zXj/AMCLHV/hR4d0ab4sfDDWJ7vUPh1oWnxtp+jSaibR4bMJcaZIIVcxATF33uFifzSC2UwLfwV8F+NPhRpuh+Lbv4Halc21nDr1pB4e0+e1kutBE+sT3EfliWZEljlhMaMyOW/cxYVlZmWedsrlSPcNQ+LXwl8PaVD4g8QfFDw7YWFxI6QXt5rdvFDIyMUdVdnAJVlZSAeCCDyK19O+KPwwufEtv4JtviNoD6zdQrNa6SmsQG6mjZPMV1iDb2Up8wIGCvPSvGP2ZvCSah4p8P8AxCtfCq2tlHpPiZIIGttjaK91rUU39nspAMcqCNkdB8oaJlUlQpO9oXwhvtB+AFx4Q0HwQtrdRfE6fU7KyggVSkK+J2uIZlA6KtqsbL/djVVGAAKFKTFyx2PYX+IPw+0rxXbeBdU8daPba3eIHtNHuNTiS6nU5wUiLb2HB6DsauXfxQ+GGkeIbfwhqvxF0G21a7n8m10u41eBLiaXA+RIywZm+ZeAM/MPWvnSX4Z+NJNP8Y/CHxddeK/tHinxRqdwraN4Hguo7y3ubuSS2uV1F18qKSCBoUHnyRyRNagRgqsRb0lPgolx4M+NVpe+B45rvxhqN08byW6+ZqajSbWGFs9Th42C9NrAkYPNHNJ9B8sUd/pXx9+EFz8Xb34Gf8J3paeJbCxtLl9Ok1O3Ekv2hrgLEke/zGkUWzOy7eFkjPO7j0ODacDHbmvIvBcmueF/jd/a3i7Q9Xf/AISXwLoNhb3trpM1xEt7az6lJcJPJEjLbYF3EQ0pRWywUkqRXKeMP2tP2sPDPjXV/D3hv/gn5ret6fYapcW1jq8XipI0voUkZUnVPsrbQ6gMFycbsZPWsMRjKOEipVb6vpGUv/SUzmxFelh0nO+vZN/kmfTFrHkjAq9bRH06V8pwftrftpLjb/wTN8Qn6eMk/wDkOrUP7bn7a6/d/wCCYniI/wDc5p/8h1yf23gP7/8A4Lqf/IHL/aWF/vf+AT/+RPrC3iJA/wA4q9BGccjpXyXD+3D+24MY/wCCXviM8/8AQ6p/8h1ai/bo/biA4/4JaeIzj/qdk/8AkOj+28B/f/8ABdT/AOQD+0sL/e/8An/8ifWsUeSMDNXLSHGSy49K+RIf27P25geP+CV/iQ/9zun/AMhVai/bx/brX7v/AASm8Sn/ALnlP/kKj+28B/f/APBdT/5AP7Swv97/AMAn/wDIn17HGpHSpo4MgcfSvkJP29P278cf8EovEx/7npP/AJCqZP2+f28uMf8ABJzxMcf9T0n/AMhUf23gP7//AILqf/IB/aWF/vf+AT/+RPr1YcdhS+V7CvkZP2+/29s4H/BJjxMf+57T/wCQaf8A8N9/t7/9IlfE/wD4Xaf/ACDR/beA/v8A/gup/wDIB/aWF/vf+AT/APkT63EDEZCimSR/IeBXyUf2/f29sY/4dLeJ/wDwu0/+Qajb9vv9vU8/8OmvE4/7ntP/AJBo/tvAf3//AAXU/wDkA/tLC/3v/AJ//In1hJAxPC1EYDggrXyg/wC3z+3l3/4JO+Jh/wBz0n/yFTG/b2/bwI5/4JQeJf8Awuk/+QqP7bwH9/8A8F1P/kA/tLC/3v8AwCf/AMifVE8APUVSni4Ix0r5bl/bx/brc5b/AIJT+JR/3PCf/IVVZf26/wBuVuG/4JYeJB/3O6f/ACFR/beA/v8A/gup/wDIB/aWF/vf+AT/APkT6hkiOelVLiM5wB+NfML/ALc37cBPP/BLbxGP+52T/wCQ6ry/tw/tuNw3/BL3xGP+51T/AOQ6P7bwH9//AMF1P/kA/tLC/wB7/wAAn/8AIn0vdJtJ4qhdKMEY7V813H7bX7arMS3/AATH8Qr/ANzmn/yHVSb9tP8AbPOd3/BNDxCP+5yT/wCQ6P7bwH9//wAF1P8A5AP7Swv97/wCf/yJ9G3KYHI/CqFyFPIHSvnW4/bM/bJYfN/wTa19fr4wT/5EqpJ+2L+2Ec7v+CcevD6+Lk/+RKP7bwH9/wD8F1P/AJAP7Swv97/wCf8A8ifQV4BnOKoXajaa+f7j9r39rpjlv+Cd+ur/ANzan/yLVWf9rj9rVlw3/BPfXB9fFa//ACLR/beA/v8A/gup/wDIB/aWF/vf+AT/APkT3a5QHoKzL+EjtXh8/wC1j+1aeW/YC1tf+5pX/wCRapXf7Vn7Urrh/wBg7Wl5/wChoX/5Go/tvAf3/wDwXU/+QD+0sL/e/wDAJ/8AyJ7VdRcnisq7iI3cCs34SeOfG/xC8JPr3j/4X3PhG+F48Q0q6vRcMYwqkSbgicEkjGP4a17vo31r0qVWFampx2fdNfg7NfM7ITjUgpR2fy/B6mReRHHA7Vl3URHbtWzd9T9Ky7vqfpWhZjXcXJOKybqJwCNtbd31P0rLu+p+lAGLdxMMlhxWXeKD1FbN/wD6o/UVj3lAGTeo284FZd0rc8dq2Lvq30rKuu/0oAyLxGA5Has24RvTtWtfdfwrMuP6UAeY/tJqR8INZJHe3/8ASiOvXv2YVYftNfDrj/mfNI/9LYq8j/aV/wCSPaz9bf8A9KI69e/Zh/5OZ+HX/Y+aR/6WxVlP7Xp/maQ3j6/5H73/AA5/484/92u0XoPpXF/Dn/jzj/3a7Reg+lfOH0BieLv+PN/oa+OP28Bn4JeNAe/hfUf/AEnkr7H8Xf8AHm/0NfHH7d//ACRPxp/2K+o/+k8lXT+NepM/gZ+RNpGuce9almgyKzbT7341p2fUfWvpT5007ONTgVqWca5HXisyz6j61qWfUfWgDTtI1JUZPStOxjUADJ61m2nVfpWpY9vrQBpWgwAB61rWnOPqKybXnGPWta07fUUAatjGN/U1qWyL+lZlj9+rHh/XtL14XD6XNJItrdSW0rtbui+ZGxVwpYAOAwKkrkZBGcg0AX9b8JaP4x0WTQtba8FvKR5n2LUp7Vzjt5kDo+D0Izgjggit3R9NsdLsINM021jgtraFIreCFAqRoq4VVA4AAAAFVLL7grStu30oA1bONeDk8rWV4l+G114l1u38TeH/AB9rHh7UYLVraSfS1t5EuYSwYJJHcRSIdrZKsoVhuYZIYg69p0X6VpWPX8KTVw2PnLxV+3X8CP2SfiVdfswt4D+KfjPxPZWn9t6xP4Z8JHVZZvtcrSNcSmAoF3O5GFjVFyqKAoCizbf8FYfhkmM/slftDn6fCG7/APiq5f4Q/wDKbH4of9kX0n/0pir7Ts+1YQ9pO9naza2N5ezja6voup4x+z3/AMFA/BPx/wDiVafDHRP2e/jF4fuLuGWVdU8YfDu406wjEaFyHndiFY4wo7kgV3GiftifCK8/aovf2ONYj1bRfGtvoy6rpcGs2SxW2t2ZzukspQ5E+zDbl4YbH4+R8eh2fUfWvEf2/v2LW/a0+G+na78OfEP/AAjPxT8C3n9r/DPxjD8r2F+mG8mRgCTbzbVR1II+621tu06P2kY3TuyF7OUrPRH0jafdH0qDxt488E/CrwRqvxL+I/iO20jQtC0+S+1bU7yTbFbQRqWd2PsB0HJOAASQK8I/4J4ftqJ+118Mr/SvHvh3/hGfih4Gvf7H+JvgyYbZNN1BMr5qKSSbebazxtkjhl3Ns3Hw74g3+o/8FkP2oZ/2fPCOoTj9mv4Ua3G3xG1qzlKR+OtdhYOmlQyKfntYWAaRlOGPI6wSUnVXKnHVvb+vLqCpvmalolv/AF+R9T+Cv28fhB4t/Yz1H9utPC3izTfA2n6Ld6sn9raOkN7eWMCkm4hg8w5RwrbCxXeBuA2lWPlHh3/guF8AvEOkWniHQP2U/wBovULC+to7mxvbH4P3MsNxC6hkkR1cq6MpBDAkEEEV3/8AwU/0zTdF/wCCYvxn0fR9PhtLS0+Fmpw2trbRBI4Y1tWVUVVwFUAAADgAV2X/AATo/wCTBvgf/wBkf8M/+mq2qW6jnyp9OxSUOTma6njU/wDwXf8A2N/CEsMvxl+GXxn+HumTSpGdd8a/Cq+tbOMscDc6B26+imvsn4afEPwD8XPAul/Ez4X+L9P1/QNatFudK1jSrpZoLmI9GR14PIII6ggg4IIpNd8M+HPGnhy98JeL9Cs9U0vUrV7bUNO1C2WaC5hdSrxyIwKupBIIIwa+Dv8Ag3ztpPAGn/tI/s3eF7+a68D/AA5/aE1nTPBLyTGRIbXeVMKOScqBEjnHG6Vm/iJLUqkaijJ3uJxhKDklax32kf8ABdH9m7xFf6ta+Av2a/j/AOKbfRtaudKvNT8LfCie/tPtUDlJEWWGQqSD24OCCQM1rRf8FsfhKp5/Yi/akPHb4GXv/wAVXM/8G96A/sn/ABFIHT9oHxX/AOjoa++raPn8Kmn7WcFLm38hz9nCTVvxPJf2QP2xPCf7Ymk63q3hf4MfE3wauh3EMM0PxJ8FTaLJdGRXYNAspPmquwhiOhK+teTfFz/gtL+zX8Iv2gPGP7NkXwQ+NHi/xF4Cura38St4D+HUmq21q88CzRZeKTKhkJxuC5KNjO019g28YxgD618I/wDBM1M/8FcP29x6eI/Av/pqvKqbmuVJ6t/o2KCg+ZtbL9UjXf8A4Lt/AHToWvNT/Y6/adtbeMZlubj4I3ipGvdmO/gCvef2Lv8AgoZ+yR/wUA8L6h4k/Zg+KsOtS6NKsWvaLd2ktnqOlyNkKJ7adVdQSrAOAUYqwDEqce3RRgEZr81NY8L+G/Bv/B0N4ef4EWEFrP4g/Z8u734wwaUoCSN9omW3nuQvAlZo9PGW5IER/iyVJ1KbV3dN22GlTmnZWaVz7U+Mn7Xfwi+CH7Qvwq/Zl8cR6sfEvxjn1eHwgbKzWS2DabbxXFx57lwYxslTbgNk5HFQ/tp/tefCH9g/4Aap+0r8cItYk8OaRdWlvdrodktxcl7idII9sbOgI3uueeBmvmf/AIKTJj/gsd+wAMddW+In/pnsaZ/wcyIB/wAEjPG5/wCpj8Pf+nW2olUkozfb/JMcacXKC7/5tGrL/wAFt/hDj5f2Hf2qB9fgVff/ABVQSf8ABbb4RnkfsP8A7U3/AIYu9/8Aiq+4riMEYGPzqpIg7n9arlq/zfgRzU/5fxPMPFX7Q/g3wl+zDf8A7WXijQ9d07w9pfgKXxbqOm32mGHU7WyjsjeSQyW7kFLhY1KmJiMOCpI61xP7En7ef7OH/BQz4QzfGT9nDxLdXVjaalJYapp2p24gvdPnXkLNEGbaHQq6sCVYHrkMBb/4Kbov/DuL9oHnkfBLxX/6Z7qvzo8M/CT4gf8ABO39nT4A/wDBXT9lbwvc3/h+9+B3hCz/AGkfh/pi8avpY0m1A1qGPoLm3zuduMj5iQrXDNM6k4TXa2v+ZcKcZw876H6zToq8j1ryv9pv9qH4Zfsr6J4W134oJqRg8YeOdN8J6R/ZloJm/tC+Z1h8wFl2x5Q7m5I9DXafC/4rfD/45/DPQ/jB8KfE9vrPhzxHp0V9pGp2rZSeF1yDg8qw5DKcMrAqQCCK+Qf+C3X/ACTT4CD/AKup8F/+jbirqS5abkjOnFSqKLPsG4jUsSfSvnH9qX/gp3+xN+yP4mX4e/Fj4wxTeLJdoh8HeG7GbVNUZmGVVoLZXMJYcjzSmRyM1X/4K5/tX+OP2Rf2NdW8V/CKLf478V6tZ+FfAahQSNUvmKJIoPBdIlmkUEEF41BGCaX9gH/gnV8IP2F/hpBHZabDrvxE1eH7T47+IepL5+o6xfyfPOfPky6w+YW2x56fM25yzFSlNz5Y/McYxUOaR5sf+CwXge9i+32v7Bn7U0mnnn+1V+C1wbfb/e3CXOPwr0v9mP8Abl/Z9/a+uda0f4Q6rrUWseG44H8QaD4j8NXem3lgJi4iLpcRqGDGOQZRmxtOccV7peAY6VlXkabxLsG7bjdjnHpTjGonq7/ITcGtF+J8tfHP/gqJ8D/gr8edb/ZzufhR8UfFPiXw9Z2t1qsXgjwTJqkcEVxEskbExvkAhgMlQM5HNcvP/wAFbfhiwx/wyL+0V+Pwfu//AIqs39nj/lNh+0Z/2T/wt/6Tx19gXXT8TUR9rO7v1fQuSpwsrdF1PnX4F/t4+DP2iPiAvw60P4CfFzw7O9pLcDUfGXw/n02yATGUMzkjec8L3wa9fvo1IwfWtS76n/erNveg+taxUktXcyk03ojJuo1yw561mXcS8jnmtW6+831rMu+p+lUIybtF9+lZd2grWu+p+lZd31P0oAybqNScc1k3nBIrYufvVj3n3jQBl3oyhB9RWTexqCa1rz7n41l3vU/WgDLvI1+Y81k3aABj7VsXfRvrWRd9G/3aAMm+6/hWZcf0rUvqy7n+lAHmf7Sv/JHtZ+tv/wClEdevfsw/8nM/Dr/sfNI/9LYq8i/aVB/4U/rPHe3/APSiOvXv2Yv+Tmfh3/2Pmkf+lsVZT+16f5mkN4+v+R+93w5/484/92u0XoPpXF/Dn/jzj/3a7Reg+lfOH0BieLv+PN/oa+Mv+CgN/BpXwC8eapdRzvFbeENTlkW2tnmkKrayEhI4wzyNgcKoLE8AEnFfZvi7/jzf6Gvjj9u//kifjT/sV9R/9J5KqHxomfws/FCb9o/4aaPCt1qtl4utonuIoVkn+HetIpkkdY40BNpyzOyoo6lmAGSRWvaftBeA1xnQPG/Xt8MtdP8A7Z1m/HP/AJEew/7Hfw1/6fLGvSbPqPrX0Xv81rr7v+CeDaHLe34/8A5q0/aI8AqRnw/46/D4X68f/bKtG0/aO+HykZ8PePPw+Fevn/2xrqLPqPrWpZ9R9aq0+6+7/gk3h2/H/gHK2v7Sfw7Urnw54/8Aw+FHiE/+2NcpZ/FPwolmttP8OvEt1JHcrJezy/DHxKG1tQHH+k40o7fmZZQP3gDIFAC8j2m06r9K1LHt9alxm+v4f8EalFdPx/4B4VY+Nfh9dhrrWfCPjN5j9hSAL8JvE7i1gj1G4uJreNv7NBEZt5hAMBQ6rtYKuBVvUfHnhi41aT7Do/j9dMyyadaQfCfxDCdKBkZzLAW0WbbId/Gwps8pcE5+X36zk2qFx3rWtXzgY7ilyS7/AIf8EfPHt/X3Hjvgf47+DPD/AI/1PXpfB3jpLG+85mkX4QeJZrmR2lVkyw0dGVAN42tJLj5FUqq4Nex8dfCWDyxa+DfHFk0EWvNZTWvwP8Tk2lze3kc1vcIg0wDzI0jxu4ZSMKcEmvf7H7/41p2/9KfJLv8Ah/wQ549vx/4B8+6R8Ufhlf6tZza58K/GFlpMN/DNcaDZfBvxZcW0xS1vY2mZW0iNXkZ7iHIZTxbqxYkKB6L+zp4u8H6j4s0/RfCeg+KrKZNDu/7Zl1j4c69pUV5L50Jgc3F/ZQxyMiecoDPvw+FBAO31Kx+4K1LWTGDjsKFCSe/4f8ETkmjZte30rS0/7wrLtZMhTjtWlYSYOcVoQfBvjbSv2rNW/wCCxvxCh/ZM8V+CtI1hfhNpLanN430+5uIHtvOT5YxAQQ+/acnjANe2QeDP+C1G0bPjX+z7j38Mar/8XXM/CFx/w+z+KLY/5ovpP/pTFX2naSAgDFc1OHNzO73f5nRUm42VlsjyH9l7w/8A8FDdJ8eXVz+1n8RPhZq/hs6S62Vt4I0a9t7tb7zYiju07FTEIxMCBzuZOwNfQdn2rLs+o+teHft//tp3n7KngDS/Bvwp8PDxN8WviBef2T8NPCMQ3Nc3bYDXUwyNtvBuDuxIB+VSVBLLteNKF2zLWpKyR8cf8FkovEeq/taSW3/BPW38UP8AGWH4bah/wuyTwRJGqHwuYV2R3Wet4Vx5IX97t8rGT5OPv7/gmjdfstXX7GHgN/2N1RPAi6Oq2EbFftKTg/6QLvH/AC9edv8ANPd8kfKVrF/4J5/sXWX7Hnwquz4s8QHxL8SPGd6dY+JfjS4O6bVtTkyzKGIBEERZljXAHLNtBdhXhXxW0zVf+CPX7UFz+1J4F024f9nP4pa1GvxW8P2ULOngzWZmCR6zBGoOy3lYhZVUYBOBk+Qi88Yypy9pLrv5f11Nm41I+zXTbz/rofSv/BVD/lGv8cP+yY6t/wCkz1m/AX9oXRf2X/8AglZ8Gfi7r3w68YeKre0+FHhK3GieBNCOpalO8unWsaeXAGXcNxGTkYzmrH/BTnWdJ8Qf8EwfjNr+g6lb3tjffCnU7iyvLSYSRTxPasySIykhlZSCCOCCDXbf8E6pN37A3wO46fB/wz/6aratXd1tO36kKyoq/f8AQ8A8S/tGf8FYv24NKm8A/ssfsm3f7PvhvVEMN98UPi/dIus2sDcMbTSYiXiuAv3WlLJk/eQgOPp39hD9iz4WfsEfs96Z8AfhZPdXscVzLf65ruokG71nUZsefeTEfxNtVQOdqIi5O3J9RtzlSa0IDgKaqNO0uZu7IlNuPKlZH5Kf8EkfC3/BVPWPg38Rbv8AY4+K3wX0bwh/wu/xMrWfj3w/qNzffaxOnmNvt3CeWRswMZyDmvrCDwD/AMHA2fk/aD/Zg/HwfrX/AMcrmf8Ag3vk2/snfEY4/wCbgfFf/o6Gvvy0YE5rGjTTpJ3f3mtWpao1ZfceefsgaP8AtiaL8Ob61/bb8Y+A9c8Vtrkj6fd/DzTbq1sk0/yYRGjpcsXMwlE5LD5drIOoNfMf/BMj/lLp+3x/2MngX/01XlfdtsQOSetfCP8AwTKYL/wVy/b4P/UyeBf/AE1XlaSVpQXn+jJg7xm/L9UY3/BRr9u3/goH8OP+ChPgf9gj4X+LPh98IPBvxVsVTwb8aPE3h6fVZ5b8KFlsoozKtstyJSqpHKpVvNh+YGQCvpv9g7/gnP8ACv8AYei8R+MoPF+uePPiV45uUuvH/wAUPGFwJtU1uVfupx8sFumSEhThQFBLbQQ//gpD+wb8OP8Ago1+y1rP7P3jaUafqm4ah4N8TRpmbQtXiU+RdIRztySjqCC0buAQSGHkP/BG3/goB8R/jv4Z8R/sV/tjW50v9oX4KTDS/G9jdSAPrlmhCQavF/z1WRSnmOvylnjkGFnQCUuWt72t9v8AIbfNR93pv/mYf/BSz/lMp/wT/wD+wt8RP/TPY1X/AODmoSH/AIJD+ORCQH/4SHw/sLdM/wBq22Km/wCClLh/+CyP/BP9v+ot8RP/AEz2NR/8HMv/ACiM8b8/8zH4e/8ATtbVE/gq/wBfZRcPjpf11ZqyfD//AIOE8Yb9ob9lzp28Ha3/APHKr3Hw/wD+DgvI3/tC/svH0x4O1r/45X3JIQe/aqlyuSDmtvZLu/vMfaeS+4+e/wDgpGuoJ/wTS+PKatJE92PgV4oF08CkI0n9jXW4qDyBnOM9qrf8E6rCw1X/AIJofAXS9Usorm2ufgb4Wiube4jDxyxto1qGRlPDKQSCDwQa0f8AgpshH/BOP9oE5H/JEfFf/pnuqpf8E23x/wAE4P2fxj/mifhT/wBM9rR/y++X6j/5c/M+Qvh7d3//AARI/a+i+A3ia9mX9lz40eIHk8AatdSFofAHiKUln0yWQ/6u0nOShbhcbj9y4kbvv+C3f/JM/gH/ANnVeCv/AEdcV9Q/tT/s3/Cj9rv4FeIv2evjV4fGoeH/ABHZGC4VcCW2kHzRXELEHZLG4V0bBwyjIIyD+Q3x/wDj78a/hVF8If8Aglh+2JeXOo+Pfhv+0n4J1DwN40kiby/GPhQXM0UF1uOf38JZIpASSehLNHI5xq/uoOPR7eXl/l9xrT/eTUuq3/z/AMz6z/4Laslr4m/ZQ1TVyBpFv+1f4W/tBnHyKS0xUt2wFWT8M19rT9DXgn/BVn9kTxF+2p+xp4j+Ffw/vBa+MdNuLfX/AANdGQJ5erWb+ZEoY8IZF8yEMeF83celc/8A8E9f+Cknwz/bM8Cw+DfGF7D4V+MHh+P7F4++HOs/6Lf2l/CNk8kUEmGkhLAsCudgYK+GHOqajWafXYys5UU103Poq86D6VmXH9K0rz7v4VlT3Nsbk2YuEMyxh2i3jcFJIDY64yDz7GtjI/NLWdH/AGxNX/4LMfHeP9j7xj4D0fUE8EeGjrMnjvTbq5ikh+yx7BELdgVYNnJPGK9bn8F/8FsMfP8AG39nv8PC+rf/ABdU/wBnuTb/AMFsf2jDj/mn/hb/ANJ46+vrk5XNc1OmpJu73fXzOipNxaVlsunkeBfs6aB+33pPiu/m/a1+IHwy1fRmsNumw+CNIvbe4S63r80jXDFTHs3jA5yRXqt70H1rTu+p/wB6sy96D610RXKrGEnzO55j8UPEv7RekeJFs/hb8LfAusabJCNt34j+Id3pdwZvmLIIYdJulKhQCG8zJyflGMnjb7x/+2RDE9xcfAz4TJHGSsjt8ZtRAUgEkEnw/wAYAOfpXc+PvhvfeI/Fs2uW8OnSrd6dDZfabtSLjSzHJLJ9otjsYGQmRe6bWhjbLY21yln8C9+vaZqus6L4dtLXTJ4C2maVZnyLryre6jWdldQFfdcKVT5tgQ/vHJG2GpX3/L/IpONtv6+8wZ/HH7Y0jSKvwM+FRMQHmhfjFqB2ZGRn/iQccc1nXPjX9sE+WT8Dvhd++H7nHxe1D5+M8f8AEh5454rT8Qfs7N/ZUVh4YvbDTvImvZDDBZII7hJNSiu4YHDIy7FjjaLlWC7gQpA2mmvwV1rT7rStSsn01LyC6Et3OwVlgT7QJmjii8gRtkcbkSBg5MmcnaFafd/h/kO8O35/5nK3fxf/AGol8XN4J/4Ux8MzqMdolzNCvxY1AiKN2ZULH+weNxVsD/Z7ZXMdz41/axb5v+FM/DLBj8wEfFi/OU/vf8gPp713Hjj4YQeLNXv7y7FpJBfxaRHNBPBuDpaXslxIrZ4YOrhcdOOeK43x78EbjxBHrGlaVDpFpDqbNJDqa25F3a/6OIhbqFXAjODlgw+SR02EnfTtNdfy/wAgvDt+f+ZjzeNv2p5iscfwf+GjM7MFVPitfEkrwwH/ABJOcd/SqV54l/avJO74LfDwfT4n35/9wldPq/wi0ZNen1vQtO06y3tpRhEFmqGM2ty8khG0DBeIrHx2XB4rdvep+tNRl1f5f5Cbj2/P/M8suvEn7VOG3fBn4fD6fEy+/wDlLWZc+Iv2oud3wd8Ajjt8Sb3/AOU1eqXfRvrWTdd/pT5X3f4f5C5l2/P/ADPMb7xD+06V+f4Q+Axz2+I16f8A3D1nT+IP2lu/wl8DdO3xDvP/AJUV6bqH3PxrKum2447Ucr7v8P8AIOZdvz/zPGvijpn7SHj/AMH3nhJvh14Is/tfl/6QPHd5Js2yK/3f7LXOduOvevR/2UNZ+O8v7Vfwzi1j4b+EYLRviFowup7bxtdSyRx/bodzIjaagdgMkKWUE8bh1Fi8lBfAHSum/ZiOf2mvh2f+p80j/wBLYqicHyt36eX+RcJLmSt18z97vhz/AMecf+7XaL0H0ri/hz/x5x/7tdovQfSvnD3zE8Xf8eb/AENfHH7d/wDyRPxp/wBivqP/AKTyV9j+Lv8Ajzf6Gvjf9vLj4I+Ncf8AQraj/wCk0lXT+NepM/gZ+K3xz/5Eew/7Hfw1/wCnyxr0mz6j615n8cWY+BrDJ/5nbw1/6fLGvSbNjkc19Gvjfov1Pn38C9X+hr2fUfWtSz6j61kWjNjrWpZM3y8npVEmvadV+lalj2+tZFqzccnpWnYM2ByetAGta9vrWtadvqKyLPlVz61r2vA/EUAa9j9/8a07f+lZFiTv6mtS2ZvXtQBsWP3BWlbdB9KybBmwRntUfjTw3e+M/BWp+FNP8TX2jT6jYyQRarpspS4tGZSBJGwIIYdQQRUybUW0rvsaUY051oxnLli2ru17Lq7LV23sjsbTov8Au1pWPX8K+U7f9gf4oPgD9vb4tLx21+f/AOO1dtv+Cf3xTfp+3/8AF1fp4hn/APjteX9dzP8A6BX/AOBw/wAz7L+wOEf+hzH/AMEVv/kTsvjT/wAE2P2fP2gPjDc/HfxTrvjXSvEd9psOn3V34X8XXGnCS3i+4hEOMjvyeoFZNt/wSG/Z2fGfjB8Zvw+K2of/ABVULf8A4J5/FZ8Y/wCChXxhX6eIp/8A49V62/4J0fFl8Y/4KKfGRfp4jn/+PVk8Rjm7vB/+Tw/zLWS8KpW/tqP/AIIrf/InffAP/gnD8F/gH8UNN+LPhL4k/E3UNQ0sTCC08RfEG8vrN/NheJvMglYq+FkJGejAEcivRNC/ZE+DenftO3/7YF/Zahqnji90ZNKs7/Vr8zRaVZjrDZxEBbcNzuKjc29+fnfd4Zbf8E3vi1IcD/go/wDGhfp4luP/AI9VyL/gmp8XWAx/wUo+Ng+nia4/+PVSxWYJWWEf/gcP8xPI+FHq85j/AOCK3/yJ9j2f3F+lN8WeBvCHxN8Han8PPH/h611bRNb0+Sy1XTL2PfFc28ilXjYdwQSK+Rrf/gmX8YHAx/wUx+N6/TxPcf8Ax6rcH/BMX4yMRj/gp18cl+nii5/+P1X13M3/AMwr/wDA4f5k/wBgcJL/AJnMf/BFb/5E9v8ABf7CXwS8Gfsf6h+w9Z33iO88A6jpN3pf2XU9bee6trK4BD28U7DcqLubYDnbnA4AA9V+D3w48M/B34ZeHPhH4Limj0bwroNno+kpcTGSRba2gSGIMx+82xFye55r5Ig/4Jc/GaTj/h6J8dh/3NNz/wDH6tQf8EsfjQ33f+Cpfx4H08VXP/x+ksXmK2wj/wDA4f5jeQ8JvfOY/wDgit/8ifblv9yr8PQfSvh6H/glX8a2HH/BVP49j6eK7n/4/VqL/glJ8bmH/KVv4/D0x4suf/j9V9dzP/oFf/gcP8xf2Bwj/wBDmP8A4Irf/Il+0/4IRfsaafqWq6l4Z+IHxg0JdY1e41O9stA+Kd9ZW5uZ3LyOIoiFBJPp0A9K0Iv+CGn7LHf46/Hz8PjVqn/xdY0X/BJ/43t/zlg/aAH08W3P/wAfqyn/AASZ+OJ5/wCHs37QX/hXXX/yRWXt8d/0B/8Ak8P8zT+xeFv+h1H/AMEVv/kT6F/Y6/Ye+Ff7FsXiCP4ZeO/H2tjxIbU3p8c+NbrWPJ8jztnk/aCfJz5zbtv3tq5+6K6L4Ofsm/B74H/HT4m/tEeBLPUI/Enxcu9NufGMtzfmSGR7GCSC3MUZGIgEkbIGcnBr5gi/4JJfHNuB/wAFbP2hB9PF11/8kVMn/BI746k/8pcP2hh9PF91/wDJFWsXmKSSwj0/vw/zJeRcJtu+cx1/6cVv/kT7uik5HNeL/EH9gL9nX4hftf8AhX9uqbT9W0j4leFNPawg1vQdUa2XUbM7gbe8iAK3KbXdfmGQG6/Km35+j/4JFfHUnB/4K6ftDj6eL7r/AOSKf/w6I+On/SXf9oj/AMLC6/8Akih4zMpb4R/+Bw/zEsh4TjtnMf8AwRW/+RPp74t/sk/Bz42/Hv4X/tKeOrPUJPE/wgn1aXwbLbX7RwxNqNvHBc+bGBiXKRJtzjacnvUH7Y/7JHwc/bm+A2qfs4/Hq01Gfw1q9za3F3Fpd+1tOXt5kmjIkUEj50XPqOK+Zz/wSJ+O46f8Fdv2iMf9jhdf/JFRt/wSN+Ow5/4e5/tD/j4wuv8A5IoeLzFp/wCyPX+/D/MP7C4TVv8AhZjp/wBOK3/yJff/AIIV/spqf+S8fH/8fjZqn/xdQTf8EMP2Vh/zXf4/fj8a9U/+Lqm//BI7465wf+Ct/wC0Kfr4vuv/AJIqJv8Agkj8cyOf+Ctn7Qh+vi66/wDkio9vjv8AoD/8nh/mX/YvC3/Q6j/4Irf/ACJ9Kan+zb8O9T/Zeu/2Q9avNav/AApf+CJ/Cl7NqGrSTX9xYTWjWshe5fLtMY2OZDk7jmtP4VfC/wAKfA/4R+F/gt4Ehnj0Pwh4dstE0aO6mMkq2lpAkEIdzy7bI1y3c818oS/8ElvjiDz/AMFZf2gT9fFtz/8AJFVpv+CUHxvUc/8ABV/9oA/Xxbc//H6tYzMk/wDdH/4HD/Mj+weE7f8AI5j/AOCK3/yJ9mXPT8a8W/ah/Yj/AGd/2tPEHgbxn8ZfB73Ws/DnxNBrnhTVrO5MFxa3EUiSeWXX78LPHGzRngmNTwRXib/8Epvjb0b/AIKsfH0/9zZc/wDx+q83/BKv41KOf+CqPx7P18V3P/x+h4zMpKzwj/8AA4f5gsh4Ti7rOY/+CK3/AMifYM/3j9K+fv2qv+Cbv7F37Y+pR+Jfjx8D9Pv9et1UW3ifTZ5dP1OLb9z/AEm2dJHC/wAKuWUdhXmlx/wS0+NMZI/4elfHg/XxVc//AB+qk3/BL34zJnP/AAVC+Op+vim5/wDj9EsXmMlZ4Rv/ALfh/mEch4Ti7rOY/wDgit/8iVm/4Ix/A6yQWdn+1J+0NFp4XaNJj+MN4LYL/d243Y7da9P/AGY/2F/2a/2OJNZv/gZ4OvbTUvEawjxBrOra9d6hd6h5W/y/MkuZXxt8x+FCj5jxXltz/wAExvjJGOf+CnPxyb6+KLj/AOP0aJ/wTh+LWh69Zazc/wDBR34z30dndxzPZ3XiS4aOdVYMY3Bm5VsYI9DShiMcpJ/VGv8At+H+YVMl4W5H/wALMX5exra/+Slz48f8EuP2bvjv8cdY/aD8ReIvHmj+Jddtba21O58K+NLnTUmjgiWONSsJGQFUdT1ya5Ob/gj7+zkq5Hxi+NH4/FjUf/iq7++8FxfFz9rv4heHPFvjnxlBp2heFvDkul6foPjrU9LghkuG1Lzn2WdxErM3kRZLAn5OO9V/i94P1D9m7RbD4o/DT4m+LZxb+INNstQ8N+JPFN1q9vq0N1eQ2zRIb2SWWKcCXfG0bj5kwyspNbOsuSVV01ypyu76+62m7W8r73t56HnxyilLEUsHHEP20402k4+5epCM4x5uZu9pJXcLJ7tLUx/gl/wTz+D37PXxGtvid4O+InxI1G+tIpYo7XxJ49vL+0YSIUJaGVirEA5BPQ817Ne9B9aofE74qfD/AOFelw6r498TQ6el1ceTZxFXkmupcZ8uGKMNJK+ATtRScAnGK4+z/aZ+C2u4gtvGT210b21tf7P1XS7qyuxJczLBB/o9xEkoV5XVA+3bk8kV1+2wtGXs3JJ9rq/3HkU8szXFUfb06E5Q25lGTW9rXStvp6nV3X3m+tZl31P0rJ+JPxh+HnwwltrXxn4j8i6vtxsdOtbWa6u7kL94x28CPK4GRkhSBkZ6ivM/Gnxx8MeOvEPgeD4c+NLkP/wnsdrrWn7J7O4WJtNv3WO4t5VSQIzRqwDrgmMEZK8TVxmHpS5XJc10rXV9Wunzub4LI8yxkPaqlJU7SfPyy5fdTfxWtq1bfc9Ou+p+lZd31P0rQuy3qelZd2x55rqPIM+5+9WPefeNal2zZ6msu76mgDLvPufjWXe9T9a077iM49RWReM3JzQBRu+jfWsm67/StG8Zst8x6Vl3TNzz2oAoah9z8aybzt9DWlfM3TJrLuST1PagDJu/vn611H7MP/JzPw6/7HzSP/S2KuXu/vn611H7MP8Aycz8Ov8AsfNI/wDS2KpqfA/Qqn8a9T97/hz/AMecf+7XaL0H0ri/hz/x5x/7tdovQfSvmT6IxPF3/Hm/0NfG/wC3nx8EPGp/6lbUf/SaSvsjxd/x5v8AQ18b/t6f8kP8bf8AYq6j/wCk0lXT+NepM/gZ+KPxwYf8INYf9jr4a/8AT3Y16TZuuevSvNPjf/yI9h/2Ovhr/wBPdjXo9n1P+7X0a+N+i/U+ffwL1f6GxaOuM5rUspFyoz0FZFn9xfpWnY/eH0FUSbFowJAHpWrYdB9ax7VgpUn0rTspORQBtWX3V+ta9r0/KsWxlyij3rVtJTgc96ANix+/+Nadv/Ssezmw3WtS1m9/zoA2LHgEn0rTtug+lZFnKMA1p2k3TkUAbNmRkc9q07PqPrWRaSjIrTsZaANe0YAjNadm6jHNYsE3INadrMMDOKANizY7hWlExx+FZFnLlhzWlFJx1oA17JjtBzV+2chgayrObgc1oWs3PFAGxaSdCRV+2l9+lZUEoCirltN0oA1reUcfN3q9BKMdayYJRgc1eglGBj8aANK3lG4DP41cicsPl5rLt5eevertvPt74oA04HIqeO5Trms6O744P61LHNnmgDSS5XPFP+0ew/OqCTheTj8ad9rX2oAvedlc5qKSZRzn8KgF2NmR61E90M4yKAJZLlfWomuFOQDUMsue9V5bnYRz2oAfNJk/KaozyggjOMd6e90OoNVLibI46+1AEUkoz1/KqlxMM4z+NPlm5qpcS4PrQBWvJMsT15qhcsRn6VZuJRk5PeqN1L8pz6UAU7tsjrWdOzAEH9at3MtULmb37UAfM998IdD+K37aXxKbWvFnizS/sHhDwv5f/CL+Lr7SvN3vqufN+yyp5uNg27s7ctjG45x/2hfgRoXwB8JP+0x4W8d+KtU1TwMBf2+l+M/EtxrVrdpuCvEi3ryNBO6sUjlhZHDsB8yllPqXxF/Zj8HeN/iHe/E2Px34z0HVNRsLaz1B/DPieaxS4itzKYQ6x/eK+dJgn++fWsuw/ZP+GVpq9nrnizxB4t8Wy6bcpc6fD4v8W3d/bW86nKyi3d/KLqcEMyEqQCCDzXhywFSUZxVOPM3JqV9VeTae17q608j9Do8SYanWo1Xiqns4wpxlRULqSjTjCcbuSjadnra6TvZtWMrRFs9S/bH8Tz+J41a+0zwTpf8AwiqTD/V2k0139skiB/iaWOFJCOcJEDwRnA/bW0/wRPpvw+1PX/JTVbf4r+HE0GQ4EjSPqUHmxg9SpiV3K9CY1JGVBHo/xT+Evg74oNZX+ufbbPU9Kd20nXNHv5LS9si4AcJLGQdjAAMjbkbA3KcDHF3X7MHw9u7201rxdrniHxFqlhqNpeWOq6/rDTy2zW9xHcIkSgLHEpkijLhEUuFAYnAxvWw2IlQqUYxTUm3dvu76q262Xkl6HnYDNcrp5hhsfUqzg6UYxcIxv8MeX3XzJcs9ZTTSd5SVnfmKHwpSxvPjh8U9V1hUbW7fW7GzgLjLxaUNOtpIFTuI2ne7Y44L7/7tcn+0lY+D1+O3we1aUxJr7eLbmG32nDy2Q066aXP95VkMOM/dLnH3jn0P4j/Bvwp4812HxadQ1XRtctbc28WueH9Re1uWgzu8mQjKzR7ssFkVgCSQASTXOWX7PXw/03W7HxXqF1q2ra3p+oJeRa3rOpvcXLMsU0Sx5PypEFnkPloFXcQxGeaKmGxEqfsVFW5+a9+nPzbW36dut+gYXNcrp4pY6VWal7F0+RR6+xdL4ua3I93pzfZt9s7G7dcHJ7V5N8BviDL4s8PadpKaFZafaQeCdCv7a2sgwWL7VBKTENxPyJ5ahe+OpNeieL9a1DRdHl1HS/DV5rE8e0Jp9hLCksmTgkGeSNBjOTlhwDjJwK8y8LfBCfSvA/huxm8Xapoms6f4R0/RtZn0G6j23kdvFgRkyxPgK7ylZECSDzDhhxj03fmVj49W5XcztA+KXxD+Ja2mmeEF0XTbpdCg1PVL3UbOa5ixPNcRQwxRJLGST9llLO0ny/IAr7js5P4deN/iJdWaeDLK00vT9d1LX/Ed1eXN4ZLy1tYrXUFify0Vonm3tcRbAWjwu4tyoQ9xD8DPDPhvTNM07wH4i1rw8+k6YmnQXem3Uckk1ojMyRSi4jlSXaWYq7LvUu+1hvbPJ+KPh/8ADj4caZpegTeHvEh08XeoXieINKur6a8s7ueUSyCSS2zOVmLyszNmPdEofLFKi0upV47IxNT+P3im11uX4bavc6LY67aXl0L/AFNdNubm0ihjW3eIpCjh5HkS5QkF0EZV1JfapeEfFnx74ii0rRfD9npcN9d+IZ9MudUu7K4Nq0cdm10LmGFmjdww2oULgK+8b3CZa18PfhnHfeHzd2mp+IdKbTdWu18N69PGIdTns5xE8xuUuIiJd84fmaMuyxRSH5/nPSJ4J0uz/suS61PUL640m6luILu/uzJI8sqOjsx6YxI2FUKq8BQAAKEpsJcqPP8AUPH3xht7DxTqVx/wjgHg2V472JbKc/2tstY7vdG3nf6IDFMiYYTYcMclQM53iL40X114n1XRvDmp6ZYx6M8UUo1PTLqd72V7eK42q0JAgQJMg3nzCWLfIAoL+hal4K0K6tdfs5I5PL8SOz6mBJ94tbR2x2/3f3cSD65Peuc1j4Y2Muoz6jonijWNHa8jjTUo9MuI1W82II1Zt6MUfYqp5kZRyqqC3yrhtSFeJytj8QPiP491cw+F9P0vSbVPD+n6iw1m1mln824EpNuUV49m3yxlznk/cPbJg8UeN/F3jXwpq+ja1Z6dY6j4Wvbm70ueykn2ypNZBxvWaNWYb2VX2cAucHfgehw+G9M0rU7jWLQSme6toIJWlmZ8pFv2ctkk/O2SSSe9c3cfDjS7aLSl0XVr7T5NIikitp7V42doZGRpIm8xHBVjGhJADDYMMOctxlYOaNy9duu88966n9mFl/4aZ+HXP/M+aR/6WxVyd398/Wup/Zg/5Oa+HX/Y+aR/6WxU6nwP0FT+Nep++Xw5/wCPOP8A3a7Reg+lcX8Of+POP/drtF6D6V8yfRGJ4u/483+hr42/b0/5If42/wCxV1H/ANJpK+yfF3/Hm/0NfG37e3HwN8bn/qVdR/8ASaSrp/GvUmfwM/E/43sD4HsOf+Z18Nf+nuxr0i0YA9e1eY/GyQnwTYDP/M6eG/8A092Nej20hHPtX0a+N+i/U+ffwL1f6G3aOu0DPQVp2bAFfpWNZSEqDitK1kIxg44FUSbdsVbAz2rSsFXPU1kWjnI+nNaVlJjFAG1aYChQfzrVtGG0DP61h2shIAyPxrUtJSQOaANu0I3Zz3rTtGA6n86xbOTkE1qWshPQ9aANqzI4Oa1LNlyOaxLSQ45IrUs5ORzQBtWrhRnIrSspckdKxoJMAcitCwkJIx+tAG1bNkAk961bVl2jmsO2kxjPrWpayjaPmoA2LKYh8jHWtOCXI5FYdnLg5zWpayk85oA17WYqMjFaVq4GDxz1rEt5cDJNadrL0Ge1AGvbTnpxV62mzjpxWRbS5I+lXraXnGaANSK4YYAxV2C5bAwR71kxyD1q5HLhc5oA17acdjVuObjIP1rFt7ltwq4lw2OD+tAGpFNkdanS6IONwrKjuGzUiXBzkn9aANcXW4YJFHnr/eFZ4uT3I/Ol+0j++KAL/wBoGMbhSGVCc7hVH7SP74pPtP8AtigC5JcdenSq1xKrYO6oZLk46/rVeW5J4znigCWR1Hc1BO4I+U/lUL3Rz1/WoJrggE5oAS5lCg7W5zVC5uW6jGafNPu44qlcy980AR3FyxJ6VRuLhiCCO1STyjJGao3EpwTmgCK4mxzWfdTkYAI6VPcTccN+NZ93L05HSgCvcyBjjis+7l69KsXEvPBrPupDz0oAp3Mm44zWbeHrzVuaT5jis69kzkg/nQBTumGTyfwrKuiOee1XrqXGef1rKu5OTQBTvmUrgetZdywHftV28kyMVmXUh47cUAUbthu6n86zrqXauOOtW7uQ7qzLyRth4FAFK9k3Dk1l3jKOtXLqU/rWZfSEck4+lAFO5k4wMcVl3c2CeR0q5cueay7t+pzQBUvJTjtWVeTsv3cdKu3jkjFZtyxYc0AZ91O5btXW/svsD+018Ouf+Z80j/0tirj7n71dX+y8+f2nvhyD/wBD5pH/AKWxVNT4H6FU/jXqfvx8Of8Ajzj/AN2u0XoPpXF/Dn/jzj/3a7Reg+lfMn0RieLv+PN/oa+Nf29+PgX43P8A1Kmpf+k0lfZXi7/jzf6GvjX9vn/khXjj/sU9S/8ASaSrp/GvUmfwM/Eb42SD/hCrDn/mc/Df/p7sq9HtpM9u1eZ/Gpj/AMIXY8/8zn4c/wDT1ZV6Nak889q+jXxv0X6nz7+Ber/Q2rKXAArTtJMkYHpWLZliAN1aVpJyBjpVEm7aS8jnNadlIMjtWHZykkVqWUmCMUAbEEuGHStO0kAwcVjWzgkEH860rXpkHvQBt2UoLdK07WTB444rFsiS+M1pwMV59qANuzkJXtWnaSD5eccViWUyhe+a07WU8Y9KANuCX5Rn0rQsZACO1Y8E2VGB0FaFlJgjFAG5aSDA+talo42/0rCtZgAMgmtK1uhwuDQBtWUuWAxWnayDpWJayBTnqPWtK1lzyAelAGxbS5FaFtcg4HTisa3m45HT0q7AzHHPWgDatrkZxV62uRuArEgkIIOau203Qd6ANmO5Gee1XYbgDHvWHHMRV23nxjrQBtJcj8qsQ3IB69axoroA4INWIroA55/OgDZS5Ap32oegrMjuuOhNTC4JGc0AX0uct0qTzz/dFZqT/NyfyqTz1/vGgC955/uik88j+EVQN0oOMmj7WvbNAFx7oEZ4xVeW45296ryXX+yfpVeS43tlf50AWJLoZ7VBPcgAmq8k/OBVea43DHcUAPkuB1qpc3IwTTZpCBuz+tU7mbjrQA+aUHLCqF1KCp57U97jamMEmqNzcqQR7dqAILmT5cYqlcSjrntUtxKCMY5HTFULq4UY70ARTyDPX8Kz7uX5TzU09wASNprOu7oYI20AV5pBkms+9lC9P1qaa5GTkGs6+ulBwFoArXUgJOBWXcS5fAFXLmQHJxWXcSjcRQBBeSCsy6lHTOOKt3kmeDk1l3cuOPagCndyDcTWZeS5UgYq3dyck9ay7uU80AVLqTAzjvWXey54Jq5cvu49D3rNvmOeDQBSupOWxWXdydRmr1z3rKuycsM0AU7yXAzWbNLnNW70jacGs6Y9eaAKl3IA2K6n9lx8/tP/AA5H/U+6P/6WxVyF2eSc11X7LRP/AA1F8OBn/mfdH/8AS2GpqfA/Qqn8a9T+gH4c/wDHnH/u12i9B9K4v4c/8ecf+7XaL0H0r5k+iMTxd/x5v9DXxp+34cfAfxyf+pT1L/0mkr7L8Xf8eb/Q18Z/t+/8kG8df9ilqf8A6TSVdP416kz+Bn4ffGiX/ii7H/scvDn/AKerKvRbWXuP0rzb4zsP+EMsR/1OPh3/ANPNlXoVvIFxjtX0a+N+i/U+ffwL1f6G3Zy+pq/ayfPWRZy5wd1aVo/IOKok2bSUkjrWnYyng/yrGgkwBj0rQsJcEYOaAN21m4GfWtS0k4BrCtZcgZbFatrK20c0AbdnJlhWlDIQB7Vg2k7bhl607WZsjLE0AblnNxitS0lJxg1h2cvQ5/CtC0ncEYJoA34JQUHNaFlJjHrWJbXBGMsa0rGfkZNAG3bynaKv28pXFY9tOuByetXraYZAz3oA27a6w2BWhZ3R5/xrGt5U3AZq9byqDgGgDcgusgH25q9BdHAx7VhwTgr1q/bzg4wTnAoA2orngc/WrdtckDFY8U/AANWrW4H96gDYiujwKuQXJ2gd6yIplKirMVxgY3UAa0d0QR9anguiSTWXFP0ycVNHcEHKscUAa8dySKsLdnAx6VjR3JIxuNTLdtkDzDQBqJdndT/tZrM+0kLksRR9rP8Az0agDQe7O6k+1ms/7V/tmg3XHDmgC5Lcnsahe6PUVUkuSermonuT13mgC1JcnpVZ7o1A90xJG85qrJdEfxmgCzPcnbj8qpXN0etRzXJxgN3qtJOrck55oAdLcnaQG/GqVzdcHHpSzzAZAP4VRuZwM4OPegBLi6O3A6mqU85Y0XM4K8GqVxcEdGxxQAlxKQcGs67lO0gZqSe5YcFzVG4uCQfm70AV55SCazr2b0P5VYvLgBchuc1nXE4Pc+9AEM8hINZlxLhiKsXkxDEK2BWXcTtk4c0AR3spxyfzrMupSDj2qxezuRgvWbPMf7xPFAFW7ky2R+NZd3KdpAz1q5dysW61l3UuAcZ60AVZ5SCazr2XuP0qzPKcnJNZ15KRnBzQBUupcZrKu5M5zVy6kzn5qzLiXJPNAFO8kGKzppTg/Srt6wx6VmXMmOnpQBTu5DuNdX+yxJn9qP4bjP8AzP8Ao/8A6XQ1xd3Llyc12H7KrA/tR/Db/sf9G/8AS2GpqfA/Qqn8a9T+gv4c/wDHnH/u12i9B9K4v4c/8ecf+7XaL0H0r5k+iMTxd/x5v9DXxl/wUA4+Anjs/wDUo6n/AOkslfZvi7/jzf6GvjL/AIKAnHwC8eH/AKlHU/8A0lkq6fxr1Jn8DPw0+Mz/APFG2OT/AMzh4d/9PNlXoMEgb7uTjrXnHxmkz4Osv+xv8Pf+nizr0CylzkYr6NfG/RfqfPv4F6v9DasmO0VpWkuCBn0rIs5eBV+1lGRmqJNyCUAD5qv2Uo4zWPbyjGD+taFjMO9AG3azKMDditS0nXA5FYFvKcjFadrKCAKANyzkGQd1adrMgPzNWJZyZbOa0YZPegDes50wCJKvWtwoYfMPxrFs5SAKv2sw3YoA3opgygI24+gq/ZyuDzWNaTBSD7Vftrr5gaANu3nbAOelX7ec7gc1hwXRxn3q/bXQytAG/bz56Gr1vOScZ7ViW9zzzz6VetrrnmgDbtp8jgmr1vcgEfP9axLa6q1b3XzZzQBuw3S4+/Vu2ulyAG+tYkVzjpVq2uvmBoA3obpcD5+asR3K5ALVjQ3WRn0qxHdcgA0AbUd0mfv/AJVMt0uMbqyY7ruKlW644oA147peu+pUu1B5esmO5/Sn/azQBrNeoRgPSfah/f8A51lpdndT/tZoA0ftQ/v/AM6PtQ/v/wA6zvtZo+1mgC9LdJ3eoTdrjl6qSXWTzUTXWc0AWpLoZ+9VeSfrzVZ7vmo5Lk9vSgCWefAzu71Wluk6B6iubr93n3qnJc8mgCxNcDJIbtVC4nODzRLdZGapT3QIOT2oALifJ61RuJz60T3QwTmqNxdd80AJcznPWqVxOQCSaLi6+bAFUbm6OMDrQA26mO3A9azriVxzUtzdYH41RubnIx3oAZcycHJrMuZQpJLVNd3WM4NZl1cbjgDr1oAjvJ1IxuA+grOmnTnL1LezfLx+lZ00vXNAEN3cJuPzD61m3co2khqmu5RuxVC7lG00AVJ5gOS1Z17NH/FJVi6m96y72Un/AOvQBDdSgk4NZdzKFJO6rc8nyn5qy7uTrQBBeSgjgj61m3bEckVauJeOv0qhcyZHPpQBQunO7iux/ZUY/wDDU3w15/5qBo3/AKXQ1xN3MMmuy/ZRk3ftUfDQD/ooGjf+l0NTU+B+hVP416n9Cvw5/wCPOP8A3a7Reg+lcX8Of+POP/drtF6D6V8yfRGJ4u/483+hr4x/4KB8fADx5/2KGp/+kslfZ3i7/jzf6GvjD/goLx+z949P/Un6p/6SyVdP416kz+Bn4V/GQj/hDrL/ALG/w/8A+nizrv7IggivOvjG5/4RCy/7G7w//wCnizq5rF7rt148utLtE16W3g0i0lWPR7q2jVHeW5DFvOdSSRGnTP3fz+hbtN/L9TwUrwXz/Q9LtZyg/lWjbPjBH45rhviHqmo6Z4RNxpT3ImfUrCHFm6LKyyXcMbqpchQSrMMkjr1FVNE8b+LNN1z/AIRuPRLqaa4vIIra11+/ijkhRre5lMpkt1lVoybcoqnLhg5OF21Tkk7EqLaueqW0x4GBg1pWU2cV5vJ8UrnSrM69qXh5BpzPdxwyQXu+dmt45pG3RlFCgiCTHzkglcgZO1/iP4qa14T1PTrbxBpMds32xGmjsLzz0mieC5AVneOPysSRoSzYUKCxbCvgc4oOVnqkErEjFalpNkAfSuA07xxrI8Vp4c1rw9BZxuyol21zMyzSGESERN5AjbDblwXVzsJ2jgHtbaXC8H8KaaYmrG7ZTYfNaMU/HQVz9lcENWpaT7gdxxTEbVrcsvTGfStG1uSMHA9axIJBt6irkFwcjB6UAdBa3eSFOBmtC2mBxXP2dwd4+laNtcHpmgDetXBUHd3q/buBghu9YVnckKDnnPStC3uckYbqaAN22ny1XIbgjkEViW9yc5zzmrcdz6GgDdt7pvQewq7DMOMnr1rCguWIFXoLk4xkfnQBtx3BC8Y9qs2102R0rGFydgqxbXR29e9AG9BdNt4x/hU6XT5BGPpWPDcnYOR+dTx3Jx1H50AbEd6/oKmW+frgCseK4YnBqZLhsigDYjvXI6CpRdvj7w/KsdLhqsCcYGWPSgDSS7bdy36VJ9rHqayDcheQxo+2n+8fyoA1/tY9TSPeYUkHtWT9tP8AeP5UhvCQRu/SgDQN8x64ppumPJwfwrO+0Y6tSG5wOGH50AXJLpi3JFQSXzdOBVV7k9NwqtJckHqPzoAtXF4xTGB1qrLdHsBVee5+XBYdfWq0lyfX9aALct0wUkkfWqU90SajluPlILfWqc10d2c0ASXMwxiqU8vvTbm5OMbh9ao3Fyc5B7etADp5uetUJ5uOKSe5PrVGe4YZNAC3M2V61RuZiORj6UlzcHHNUbm4OaAEurhtxGazbiUZ3Z5FOvLltxqhcXBGRntQAt1OXGCB9RWdczbOAetOuLhgvXGaz724IxzQA25lDHmsu5umJIKj2qWe4OetZ9xL15oAgvJjszjv0rNupAepqzeSYXn1rOuZfQ/jQBBczYyMVl3UmWOe9XLqXGcmsu5l+Y80AQXcu0EDn2rOubhj2HSrN5KAOtZlzJzkelAEFxIGNdl+yfIT+1X8MwOn/CwdG/8AS6GuGuJOa7L9kx8/tXfDLH/RQtF/9Loamp8D9Cqfxr1P6I/hz/x5x/7tdovQfSuL+HP/AB5x/wC7XaL0H0r5k+iMXxaubRx7V8Zf8FCbdx+zx4/kA4Hg3VCT/wBuktfaviCAz27KB2r5t/a++D+t/FH4O+LvAXh9oI7/AFzw3fWFlLdMyxJNNbvGjOVDEKGYZIBOOgPSqg0ppkyTcWfz4+IrXw5qWmxW/ii4ijto7+2uY2luPLHnQTJPEc5HR4lOO4GDxVyw8S+EDqF3cLqtnHcxbYLuR5VRsKcqDnGVBlOO2XOOtfVdz/wRX/aX0qdJG13wOrRXBmBgv7sZkKspY/6IMkhj1rKg/wCCKPx/tLWO0svEPhaARXImV49WvNwbKHqbb/pmv5e9e68TQvdSR4qw9a1uVnzjfX/gC6086Fq1/YSQTzq5tmuFJZ2kEitjOc7yrA+pB9KNB1z4b6be2VjpGkRw3Ny6zwZgWORsrsWTLkFspKQOpwxGBgivpuy/4IyfHqxaE2+u+GEW2XbbxrrF4Vj+dHOAbfqWRSc+/qasWv8AwRr+OFpG0MWt+HFSUbZ1XWLwCVP7jD7P8w9zzyefmbK+s0b/ABIf1et/Kz5v8Pa98I77Wrq/sv7OS6WJnubia28vcjtsZ9zgAhm+UkfeIwc4rX0A/CaylEWiTaCjq5lCwTRZG1HXPBzhUMi+gUsOBmvdbX/gjN8dNJjaXSPEXh3zwgS2a41m+It49+8pHsgBTnvyfXIJB1tN/wCCQnxptdBXQr3VfDUyG3jjnddQuk3lWd8jEGV+aRzweM+woWJo/wAyG8PV7M8D0OL4UWuoQ6hpVrpKzQQNJHdwImyNYwsOQ4+UEKwjznIX5egxXS/8Jt4StU33PirTowuMl76MYypYZye6gn6AmvY7T/gkp8YrZ/POs6GZmdnec6tdFmYyCTd/qOodVYY4G0DGOKtN/wAEofi1Lzc6locu1JlQSatdkJ5qsspH7nq+4k+/NNYmivtIX1as/ss8t0XV7DUrdL7Tb2K4hcnZLDIHU4ODgjg4II/Cti2uRjivVtL/AOCbnxz0tXSHU/DwEkzyv/ptwcsxJJ/1Hqa0Yf8Agn18dI/val4f/C7n/wDjNP61h/5kT9Wr/wAp5PBcqAOa0ra4XAwe1enxfsDfHBBhtS0L8Luf/wCM1ch/YV+NUeM3+icel1N/8Zp/WsP/ADIPq1f+U8ztrhSRg/Wrtrcr0r0iL9iL4ypy17o3vi5m/wDjVWYv2LfjDGQTeaPx/wBPMv8A8ao+tYf+ZB9Wr/ynnttcrgc8d6vQXIBAz6c13kX7HPxcQYN1pP4XEv8A8aqzH+yL8V0xm40vj/p4l/8AjdH1rD/zIPq1f+U4i3uVyOavQXC9M12UX7KHxTTrPpv/AH/k/wDjdWIv2XPicnWXT/wmk/8AjdH1rD/zIPq1f+U5O2uQQCDVqG5APWurh/Zo+JMf3nsPwmk/+IqeP9nD4jIclrL/AL+v/wDEUfWsP/Mg+rV/5TmYrldgxU8VwB3rp0/Z7+ISjn7H7/vX/wDiKkT4AfEBev2T/v6//wATR9aw/wDMg+rV/wCU52G5yo+Y9asQ3IyOT26Vvx/Afx8oG4Wv4SP/APE1IvwN8dr/AA23/fxv/iaPrWH/AJkH1av/ACmPHeLntUyXqkdelay/BLx0D8yW3/fxv/iakHwY8cDjZb/9/G/+Jo+tYf8AmQfVq/8AKZUV6oxzUn2xf71ai/BzxsvWOD/vtv8A4mnf8Kf8bf8APKD/AL7b/wCJo+tYf+ZB9Wr/AMpk/bVH8VL9tH94flWr/wAKf8bf88oP++2/+Jo/4U/42/55Qf8Afbf/ABNH1rD/AMyD6tX/AJTK+2j+8PypPto/vCtb/hT/AI2/55Qf99t/8TR/wp/xt/zyg/77b/4mj61h/wCZB9Wr/wApiyXqEUwXi881tt8HPGzHPlwf99t/8TTV+DXjgZzHb/8Afbf/ABNH1rD/AMyD6tX/AJTCkvVz97tUElwDzmuhb4K+OCchLf8A7+N/8TQfgp44Ixst/wDv43/xNH1rD/zIPq1f+U5aW5BXAPNVpLlc11snwP8AHbj7lt/38b/4mon+A/j5ugtf+/j/APxNH1rD/wAyD6tX/lORluQAQTVOa4G48/jXbP8AALx+2cfZP+/r/wDxNV5P2d/iG3Q2X/f1/wD4ij61h/5kH1av/KcNcXIxgnmqVzcL69q9Al/Zv+I79Gsfxlf/AOIqtL+zH8S36Saf+M0n/wARR9aw/wDMg+rV/wCU85uLlRxmqs9wuDya9Hl/ZX+KEh4m078Z5P8A43UMv7J/xUcYWfTPxnk/+N0fWsP/ADIPq1f+U8wuLhSuc96o3NwOeeK9Um/ZD+K8gwtzpfXvcS//ABuq037HHxdk6XWkj/t4l/8AjVH1rD/zIPq1f+U8ju7ld5yaoXFwoJ5/GvYZv2KfjFI5YXuj8/8ATzN/8aqrL+w58Z5AQL7Rf/Amb/41R9aw/wDMg+rV/wCU8ZubldtULu4GevavbJf2EfjZJ01DQ/xupv8A4zVW4/YD+OMv3dS0Hp3u5/8A4zR9aw/8yD6tX/lPDJ7hcmqM9ypyM/lXu8v/AAT2+Oz9NU8P/jdz/wDxmq0v/BOr49uDjVvD3P8A0+XH/wAYo+tYf+ZB9Wr/AMp4BfT7kIHrWbcS+9fRMv8AwTe+P8gwNX8N9f8An8uP/jFVZf8Agmj+0G/3dZ8Nf+Btx/8AGKPrWH/mQfVq/wDKfN95L8x5NZ9zKBn6V9Kz/wDBML9omViya34Y/G+uf/keq0v/AAS1/aOfO3XfC3TvfXP/AMj0fWsP/Mg+rV/5T5ivJBjOaz5pRzX1FP8A8Ep/2k5emveFPxv7r/5Gqq//AASZ/aZbp4g8I/jqF1/8jUfWsP8AzIPq1f8AlPlqeT5sZrtP2SHDftZfDAZPPxF0X/0vhr22H/gkN+1DfTeVH4k8HAnnLajd/wDyLXon7Nf/AARr/ak8GfHfwV8TdZ8X+CH0/wAPeLtN1S/hg1K8M0kNvdRyuqA2gUuVQgAsBnGSOtTPE0HBrmKhh66mnyn69fDxClogPoK7McDFc34PsWtbdVK44rpK8A9siuoRKmMVz2t+F4r9SGjB/CumpCiN1UUAeZ33wutZ3JNsOfaqh+EVpn/j1H/fNeqmCI9YxR9mh/uCgDyr/hUVp/z7D/vmj/hUVp/z7D/vmvVfs0P9wUfZof7goA8q/wCFRWn/AD7D/vmj/hUVp/z7D/vmvVfs0P8AcFH2aH+4KAPKv+FRWn/PsP8Avmj/AIVFaf8APsP++a9V+zQ/3BR9mh/uCgDyr/hUVp/z7D/vmj/hUVp/z7D/AL5r1X7ND/cFH2aH+4KAPKv+FRWn/PsP++aP+FRWn/PsP++a9V+zQ/3BR9mh/uCgDyr/AIVFaf8APsP++aP+FRWn/PsP++a9V+zQ/wBwUfZof7goA8q/4VFaf8+w/wC+aP8AhUVp/wA+w/75r1X7ND/cFH2aH+4KAPKv+FRWn/PsP++aP+FRWn/PsP8AvmvVfs0P9wUfZof7goA8q/4VFaf8+w/75o/4VFaf8+w/75r1X7ND/cFH2aH+4KAPKv8AhUVp/wA+w/75o/4VFaf8+w/75r1X7ND/AHBR9mh/uCgDyr/hUVp/z7D/AL5o/wCFRWn/AD7D/vmvVfs0P9wUfZof7goA8q/4VFaf8+w/75o/4VFaf8+w/wC+a9V+zQ/3BR9mh/uCgDyr/hUVp/z7D/vmj/hUVp/z7D/vmvVfs0P9wUfZof7goA8q/wCFRWn/AD7D/vmj/hUVp/z7D/vmvVfs0P8AcFH2aH+4KAPKv+FRWn/PsP8Avmj/AIVFaf8APsP++a9V+zQ/3BR9mh/uCgDyr/hUVp/z7D/vmj/hUVp/z7D/AL5r1X7ND/cFH2aH+4KAPKv+FRWn/PsP++aP+FRWn/PsP++a9V+zQ/3BR9mh/uCgDyr/AIVFaf8APsP++aP+FRWn/PsP++a9V+zQ/wBwUfZof7goA8q/4VFaf8+w/wC+aP8AhUVp/wA+w/75r1X7ND/cFH2aH+4KAPKv+FRWn/PsP++aP+FRWn/PsP8AvmvVfs0P9wUfZof7goA8q/4VFaf8+w/75o/4VFaf8+w/75r1X7ND/cFH2aH+4KAPKv8AhUVp/wA+w/75o/4VFaf8+w/75r1X7ND/AHBR9mh/uCgDyr/hUVp/z7D/AL5o/wCFRWn/AD7D/vmvVfs0P9wUfZof7goA8q/4VFaf8+w/75o/4VFaf8+w/wC+a9V+zQ/3BR9mh/uCgDyr/hUVp/z7D/vmj/hUVp/z7D/vmvVfs0P9wUfZof7goA8q/wCFRWn/AD7D/vmj/hUVp/z7D/vmvVfs0P8AcFH2aH+4KAPKv+FRWn/PsP8Avmj/AIVFaf8APsP++a9V+zQ/3BR9mh/uCgDyr/hUVp/z7D/vmj/hUVp/z7D/AL5r1X7ND/cFH2aH+4KAPLrX4TWsUu8Ww6f3a6PRPBcNgQViAx7V1wt4R/AKURRjogoAr2FmsCgAdKtUUUAf/9k=", 0);
//		Message msg2 = new Message("msg2", "test 2", "David Krason", "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", 1);
//		Message msg3 = new Message("msg3", "test 3", "Lee Young Bin", "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", 2);
//		listMessage.addMessage(msg1);
//		listMessage.addMessage(msg2);
//		listMessage.addMessage(msg3);
//
//		lvMessage.setItems(test.getOList());
//		
//		//test
		lvMessage.setCellFactory(new Callback<ListView<Message>, ListCell<Message>>() {		
			@Override
			public ListCell<Message> call(ListView<Message> param) {
				return new MessageCell();
			}
		});
		
		
		//avatar
		if (!userInfo.getAvatarString().equals("null")){
			try {
				System.out.println("Avatar string: " + userInfo.getAvatarString());	
				cimgAvatar.setFill(new ImagePattern(ImageUtils.decodeBase64BinaryToImage(userInfo.getAvatarString())));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//update Username label when signing
		lblUsername.setText(userInfo.getFullName());
		
		//set canvas
		gc = canvas.getGraphicsContext2D();
		
		//update list online to lv
		ObservableList<String> olist = FXCollections.observableArrayList(listOnline.getListName());
		lvOnline.setItems(olist);
		
		//update list room to lv
		ObservableList<String> orlist = listRoom.getOListRoomName();
		System.out.println("list room name: " + listRoom.getListRoomName());
		lvRoom.setItems(orlist);
		//item change event
		lvRoom.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if (newValue.equals(oldValue))
					return;
				
				//get room by name
				currRoom = listRoom.getRoomFromName(newValue);
				System.out.println("Current room: " + currRoom.getName());
				//System.out.println("Room id: " + currRoom.getId());
				
				//emit to server to get message
				socket.emit("room_change", currRoom.getId());
				
				//update UI room
				lblRoomName.setText(currRoom.getName());
				
				//clear lvMessage
				lvMessage.getItems().clear();
				
				//clear listMessage
				listMessage.getList().clear();
			}

		});
		
		
		//auto complete for tfPerfomrer
		//TextFields.bindAutoCompletion(tfPerformers, currRoom.getMembers());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//update UI
		updateUI();	
		
		//Test lvPlan
		listJournal.addJournal(new Journal("id1", "Test workd 1", new ArrayList<String>() {} , false,  " ", null)); 
		listJournal.addJournal(new Journal("id2", "Test workd 2", new ArrayList<String>() {} , true, " ", null)); 
		listJournal.addJournal(new Journal("id3", "Test workd 3", new ArrayList<String>() {} , true, " ", null)); 
		
		ObservableList<Journal> testList = FXCollections.observableArrayList(listJournal.getOListJournal());
		
		lvPlan.setItems(testList);
		
		lvPlan.setCellFactory(new Callback<ListView<Journal>, ListCell<Journal>>() {
			
			@Override
			public ListCell<Journal> call(ListView<Journal> param) {
				return new PlanCell();
			}
		});
		
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
		if (list.size() == 0) return 0;
		int min = list.get(0).getIndex();
		for(int i=1; i<list.size(); i++){
			if (list.get(i).getIndex() < min)
				min = list.get(i).getIndex();
		}
		
		return min;
	}
	
	//--This method only use for SenderTyping list
	private void removeSenderFrom(ArrayList<SenderTyping> list, String sender, JFXListView<Message> lv){
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

	File imgPath;
	
	@FXML
	public void onBtnImageAction(){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String encodstring = ImageUtils.encodeFileToBase64Binary(imgPath);
		       
				//emit to server
		        socket.emit("new_image", encodstring);
			}
		});
		
	}
	
	@FXML
	public void onBtnChooseImgAction(){	
		FileChooser fileChooser = new FileChooser();
        
        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
          
        //Show open file dialog
        File file = fileChooser.showOpenDialog(null);
        imgPath = file;       
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imgview.setImage(image);
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
	}
	
	
	// ============== static sub class ========================
	public static class PlanCell extends ListCell<Journal>{	
		@Override
		public void updateItem(Journal journal, boolean empty){
			super.updateItem(journal, empty);
			setText(null);
			setGraphic(null);
			if (journal != null && !empty){
				PlanItem item = new PlanItem();
				//convert array list to just one string
				String performers = "";
				for(int i=0; i<journal.getListPerformer().size(); i++){
					performers += journal.getListPerformer().get(i).toString() + " ";
				}
				
				if (journal.getListPerformer().size() == 0)
					performers = "No one received this task";
				
				item.setInfo(journal.getWork(), performers, journal.IsDone(), journal.getStartDay(), journal.getEndDay());
				setGraphic(item.getVBox());
			}
		}
	}
	
	public static class MessageCell extends ListCell<Message>{
		@Override
		public void updateItem(Message message, boolean empty){
			super.updateItem(message, empty);
			setText(null);
			setGraphic(null);
			if (message!=null && !empty){
				MessageItem item = new MessageItem();
				try {
					item.setInfo(message.getImgString(), message.getMessage(), message.getTimestamp());
					setGraphic(item.getVBox());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
