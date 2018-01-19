package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Tools.ImageUtils;
import Tools.SlideManager;
import chatsocket.ChatSocket;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import dataobject.ListNotification;
import dataobject.ListOnline;
import dataobject.ListRoom;
import dataobject.Notification;
import dataobject.Room;
import dataobject.UserInfo;
import dataobject.UserOnline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RegisterSceneController{
	    @FXML
	    private JFXButton btnLogin;

	    @FXML
	    private JFXButton btnRegister;

	    @FXML
	    private JFXButton btnExit;

	    @FXML
	    private AnchorPane pn_Register;

	    @FXML
	    private ImageView imgAvatar;

	    @FXML
	    private JFXTextField tfEmail;

	    @FXML
	    private JFXTextField tfName;

	    @FXML
	    private JFXTextField tfPhoneNumber;

	    @FXML
	    private JFXPasswordField tfPassword;

	    @FXML
	    private JFXPasswordField tfConfirmPassword;

	    @FXML
	    private JFXButton btnConfirm;

	    @FXML
	    private AnchorPane pn_Login;

	    @FXML
	    private JFXTextField tfEmailLogin;

	    @FXML
	    private JFXPasswordField tfPasswordLogin;

	    @FXML
	    private JFXButton btnSignin;
	    
	    File fileImg = null;
		Socket socket;
		UserInfo userInfo = UserInfo.getInstance();
		ChatSocket chatsocket;
		
		//List online
		ListOnline listOnline = ListOnline.getInstance();
		
		//List room
		ListRoom listRoom = ListRoom.getInstance();
		
		//Login
		Main main;
		
		//Notificaton
		ListNotification listNotification = ListNotification.getInstance();
	    
	    public RegisterSceneController() {  	    	
	    	socket = ChatSocket.getInstance().getSocket();
	    	socket.on(Socket.EVENT_CONNECT, new Listener() {	
				@Override
				public void call(Object... args) {
					System.out.println("Client connected to server");
				}
			}).on(Socket.EVENT_DISCONNECT, new Listener() {	
				@Override
				public void call(Object... args) {
					System.out.println("Client disconnected to server");
				}
			});
	    	
	    	socket.connect();
			
			socket.on("authentication_result", new Listener() {
				
				@Override
				public void call(Object... args) {
					Platform.runLater(new Runnable() {				
						@Override
						public void run() {
							JSONObject data = (JSONObject) args[0];
							System.out.println(data);
			                String result;
			                try {
			                	if (data != null){
			                		
			                    userInfo.setFullName(data.getString("fullname"));
			                    userInfo.setPhone(data.getString("phone"));
			                    userInfo.setEmail(data.getString("email"));
			                    userInfo.setAvatarString(data.getString("avatarstring"));
			                    
			                    System.out.println(userInfo.getFullName());
			                    System.out.println(userInfo.getPhone());
			                    System.out.println(userInfo.getEmail());
			                    System.out.println(userInfo.getAvatarString());
			                    
			                    	Platform.runLater(new Runnable(){
			    						@Override
			    						public void run() {		
			    							Main.showMainChatScene();
			    						}       	 
			                        });   	
			                	}
			                	else
			                		Platform.runLater(new Runnable(){
			    						@Override
			    						public void run() {		
			    							new Alert(AlertType.ERROR, "Your email or password not valid. Please try again").show();
			    						}       	 
			                        });  
			                } catch (JSONException e) {
			                	e.printStackTrace();
			                    return;
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
							System.out.println("Register form: " + listOnline.getListName());
							
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
								JSONArray listjson = object.getJSONArray("members");
								ArrayList<String> members = new ArrayList<>();
								for(int i=0; i<listjson.length(); i++){
									members.add(listjson.getString(i));
								}		
								listRoom.addRoom(new Room(roomId, roomName, members));
								System.out.println("Register form: " + listRoom.getListRoomName());
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					
				}
			}).on("register_result", new Listener() {
				
				@Override
				public void call(Object... args) {
					String data = (String)args[0];
					System.out.println(data);
	                Platform.runLater(new Runnable(){
						@Override
						public void run() {		
							if (data.equals("null")){
								new Alert(AlertType.INFORMATION, "Ops! Something goes wrong. Please try again").show();
							}
							else{
								userInfo.setUserid(data);
								Main.showMainChatScene();
							}
						}       	 
	                });
				}
			});
		}

	    @FXML
	    void handleButtonAction(ActionEvent event) {
	    	if (event.getSource() == btnLogin) {
				pn_Login.toFront();
			} 
			else if (event.getSource() == btnRegister ) {
					pn_Register.toFront();
			}
			else if (event.getSource() == btnExit) {
			    // get a handle to the stage
			    Stage stage = (Stage) btnExit.getScene().getWindow();
			    // do what you have to do
			    socket.close();
			    stage.close();
			}
	    }

	    @FXML
	    void onActionBtnConfirm(ActionEvent event) {
	    	System.out.println("BtnConfirm clicked");

			if (!isValidRegister()){
				new Alert(AlertType.WARNING, "Something went wrong with your information. Please check again").show();
				return;
			}
			
			//Push data
			userInfo = UserInfo.getInstance();
				
			userInfo.setEmail(tfEmail.getText().toString());
			userInfo.setPassword(tfPassword.getText().toString());
			userInfo.setPhone(tfPhoneNumber.getText().toString());
			userInfo.setFullName(tfName.getText().toString());
			if (fileImg != null)
				userInfo.setAvatarString(ImageUtils.encodeFileToBase64Binary(fileImg));
			else
				userInfo.setAvatarString("null");
			
			//Convert to JSONObject
			JSONObject userjson = new JSONObject();
			try {
				//userjson.put("userid", userInfo.getUserid());
				userjson.put("email", userInfo.getEmail());
				userjson.put("fullname", userInfo.getFullName());
				userjson.put("password", userInfo.getPassword());
				userjson.put("phone", userInfo.getPhone());
				userjson.put("avatarstring", userInfo.getAvatarString());
				
				socket.emit("register", userjson);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	    @FXML
	    void onActionBtnSigin(ActionEvent event) {
	    	System.out.println("BtnSigin clicked");
			if (!isValidLogin()){
				new Alert(AlertType.WARNING, "Something went wrong with your information. Please check again").show();
				return;
			}
			
			authentication();
	    }

	    @FXML
	    void onImgAvatarMouseClicked(MouseEvent event) {
	    	FileChooser fileChooser = new FileChooser();
	        
	        //Set extension filter
	        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
	        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
	        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
	          
	        //Show open file dialog
	        fileImg = fileChooser.showOpenDialog(null);
	        try {
	            BufferedImage bufferedImage = ImageIO.read(fileImg);
	            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
	            imgAvatar.setImage(image);
	            imgAvatar.setFitWidth(96);
	            imgAvatar.setFitWidth(96);
	            
	        } catch (IOException ex) {
	        	ex.printStackTrace();
	        }
	    }

	    @FXML
	    void onKeyPressedBtnConfirm(KeyEvent event) {

	    }

	    @FXML
	    void onKeyPressedBtnSigin(KeyEvent event) {

	    }	

	    private boolean isValidLogin(){
			
			if (tfEmailLogin.getText().trim().length() == 0)
				return false;
			
			if (tfPasswordLogin.getText().trim().length() == 0)
				return false;
			
			return true;
		}
	    
	    public void authentication() {
			//Convert to JSONObject
			JSONObject userjson = new JSONObject();
			try {
				userjson.put("email", tfEmailLogin.getText());
				userjson.put("password", tfPasswordLogin.getText());
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			socket.emit("authentication", userjson);
		}

	    private boolean isValidRegister(){
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
