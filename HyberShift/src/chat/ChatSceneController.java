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
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import Tools.ImageUtils;
import Tools.SlideManager;
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
import dataobject.ListNotification;
import dataobject.ListOnline;
import dataobject.ListRoom;
import dataobject.Message;
import dataobject.Notification;
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
    
    //Show notification
    @FXML private JFXButton btnShowNotification;
    @FXML private AnchorPane pnlNotification;
    ListNotification listNotification = ListNotification.getInstance();
    
    //Slide presentation
    @FXML private ImageView imgSlide;
    @FXML private JFXButton btnOpenSlide;
    @FXML private JFXButton btnLeft;
    @FXML private JFXButton btnRight;
    @FXML private AnchorPane pnlSlide;
    @FXML private JFXButton btnShowSlide;
    ArrayList<Image> listSlide;
    private int currSlide;
    
    //Show room
    @FXML private JFXButton btnShowRoom;
    @FXML private AnchorPane pnlRoom;
    
    //Board drawing
    GraphicsContext gc;
    PenDrawing penDrawing;
    DrawState drawState;
    @FXML private JFXColorPicker colorPicker;
    @FXML private JFXSlider slider;
	
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
	
	//notification
	@FXML private JFXListView<Notification> lvNotification;
	
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
								//gc.setStroke((Color)object.get("color"));
								gc.setLineWidth(object.getDouble("width"));
								slider.setValue(object.getDouble("width"));
								System.out.println("width: " + object.getDouble("width"));
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
		}).on("notification", new Listener() {		
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						System.out.println("New notification!");
						JSONObject object = (JSONObject)args[0];
						Notification notification = new Notification();
						try {
							notification.setSender(object.getString("sender"));
							notification.setContent(object.getString("content"));
							notification.setTimestamp(object.getInt("timestamp"));
							notification.setType(object.getString("type"));
							notification.setImgString(object.getString("imgstring"));
							listNotification.addNotification(notification);
							lvNotification.setItems(listNotification.getOList());
						} catch (JSONException e) {
							// TODO Auto-generated catch block
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
							e.printStackTrace();
						}
						
					}
				});
			}
		}).on("task_change", new Listener() {
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
		}).on("new_slide", new Listener() {		
			@Override
			public void call(Object... args) {
				Platform.runLater(new Runnable() {	
					@Override
					public void run() {
						JSONObject object = (JSONObject)args[0];
						try {
							if (!currRoom.getId().equals(object.getString("room_id")))
								return;
							
							imgSlide.setImage(ImageUtils.decodeBase64BinaryToImage(object.getString("imgstring")));
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
    void onActionBtnShowNotification(ActionEvent event) {
		pnlNotification.toFront();
		pnlNotification.setVisible(true);
		pnlRoom.setVisible(false);
    }
	
	@FXML
    void onActionBtnShowRoom(ActionEvent event) {
		pnlRoom.toFront();
		pnlRoom.setVisible(true);
		pnlNotification.setVisible(false);
    }
	
	@FXML
	void onActionBtnShowSlide(ActionEvent event) {
		if (pnlSlide.isVisible()){
			pnlSlide.setVisible(false);
		}
		else{
			pnlSlide.toFront();
			pnlSlide.setVisible(true);
			pnlPlan.setVisible(false);
			drawer.setVisible(false);
		}
		
	}
	
	
	@FXML
    void onActionBtnOpenSlide(ActionEvent event) {
		listSlide.clear();
		imgSlide.setImage(null);
		currSlide = 0;
		
		FileChooser fileChooser = new FileChooser();
        
        //Show open file dialog
        File pptPath = fileChooser.showOpenDialog(null);
    	Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					listSlide = SlideManager.convertSlideToImage(pptPath.getPath());
					imgSlide.setImage(listSlide.get(currSlide));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
    }
	
	 @FXML
	 void onActionBtnLeft(ActionEvent event) {
		 if (listSlide.size() == 0) 
			 return;
		 if (currSlide == 0)
			 return;
		 currSlide--;
		 //imgSlide.setImage(listSlide.get(currSlide));
		 JSONObject object = new JSONObject();
		 try {
			object.put("room_id", currRoom.getId());
			object.put("imgstring", ImageUtils.imgToBase64String(SwingFXUtils.fromFXImage(listSlide.get(currSlide), null)));
			
			//emit
			socket.emit("new_slide", object);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
	  
	 @FXML
	 void onActionBtnRight(ActionEvent event) {
		 if (listSlide.size() == 0) 
			 return;
		 if (currSlide >= listSlide.size())
			 return;
		 currSlide++;
		 //imgSlide.setImage(listSlide.get(currSlide));
		 JSONObject object = new JSONObject();
		 try {
			object.put("room_id", currRoom.getId());
			object.put("imgstring", ImageUtils.imgToBase64String(SwingFXUtils.fromFXImage(listSlide.get(currSlide), null)));
			
			//emit
			socket.emit("new_slide", object);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
					drawer.setVisible(true);
					pnlPlan.setVisible(false);
					pnlSlide.setVisible(false);
					drawer.open();
				}
			});
    	}
    }
    
    @FXML
    void onMouseDraggedCanvas(MouseEvent event) {
    	if (drawer.isHidden()) return;
    	
    	penDrawing.addPoint(new Point((int)event.getX(), (int)event.getY()));
    	gc.setStroke(colorPicker.getValue());
    	gc.setLineWidth(slider.getValue());
    	penDrawing.draw(gc);
    	drawState = DrawState.ON_DRAW;
    	JSONObject object = new JSONObject();
    	try {
			object.put("room_id", currRoom.getId());
			object.put("color", (Color)colorPicker.getValue());
			object.put("width", slider.getValue());
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
    void onActionColorPicker(ActionEvent event) {
    	gc.setStroke(colorPicker.getValue());
    	System.out.println(colorPicker.getValue());
    }
    
    @FXML
    void onDragSlider(MouseEvent event) {
    	gc.setLineWidth(slider.getValue());
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
    	if (pnlPlan.isVisible()){
    		pnlPlan.setVisible(false);
    	}
    	else{
    		pnlPlan.setVisible(true);
    		pnlPlan.toFront();
    		pnlSlide.setVisible(false);
    		drawer.setVisible(false);
    	}
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
		//pen drawing
		//gc.setLineWidth(slider.getValue());
		//gc.setStroke(colorPicker.getValue());
		
		pnlNotification.setVisible(true);
		pnlRoom.setVisible(false);
		
		lblRoomName.setText("Hybershift public chat");
		
		listSlide = new ArrayList<>();
		
		//lvMessage
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
				
				//clear lvPlan
				lvPlan.getItems().clear();
				
				//clear list journal
				listJournal.getList().clear();
				
				//auto complete for tfPerfomrer
				//TextFields.bindAutoCompletion(tfPerformers, currRoom.getMembers());
			}

		});
		
		
	
		
		
		//lvNotification
		lvNotification.setCellFactory(new Callback<ListView<Notification>, ListCell<Notification>>() {
			@Override
			public ListCell<Notification> call(ListView<Notification> param) {
				return new NotificationCell();
			}
		});
		
		lvNotification.setItems(listNotification.getOList());
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//update UI
		updateUI();	
		
		//Test lvPlan
//		listJournal.addJournal(new Journal("id1", "Test workd 1", new ArrayList<String>() {} , false,  " ", null)); 
//		listJournal.addJournal(new Journal("id2", "Test workd 2", new ArrayList<String>() {} , true, " ", null)); 
//		listJournal.addJournal(new Journal("id3", "Test workd 3", new ArrayList<String>() {} , true, " ", null)); 
//		
//		ObservableList<Journal> testList = FXCollections.observableArrayList(listJournal.getOListJournal());
//		
//		lvPlan.setItems(testList);
		
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
	
	public static class NotificationCell extends ListCell<Notification>{
		@Override
		public void updateItem(Notification notification, boolean empty){
			super.updateItem(notification, empty);
			setText(null);
			setGraphic(null);
			if (notification != null && !empty){
				NotificationItem item = new NotificationItem();
				try {
					item.setInfo(notification.getSender(), notification.getContent(), notification.getType(), notification.getImgString(), notification.getTimestamp());
					setGraphic(item.getVBox());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
