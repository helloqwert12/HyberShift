//package application;
//
//import java.awt.event.ActionEvent;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.util.ResourceBundle;
//
//import javax.imageio.ImageIO;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import Tools.ImageUtils;
//
//import com.github.nkzawa.emitter.Emitter.Listener;
//import com.github.nkzawa.socketio.client.Socket;
//import com.jfoenix.controls.JFXButton;
//
//import chatsocket.ChatSocket;
//import dataobject.ListOnline;
//import dataobject.ListRoom;
//import dataobject.Room;
//import dataobject.UserInfo;
//import dataobject.UserOnline;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.embed.swing.SwingFXUtils;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.*;
//import javafx.scene.control.Alert.AlertType;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.KeyCode;
//import javafx.scene.input.KeyEvent;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.AnchorPane;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//
//public class RegisterController implements Initializable{
//	@FXML private JFXButton btnLogin, btnRegister, btnExit;	
//	@FXML private AnchorPane pn_Login, pn_Register;
//	
//	//Register
//	@FXML TextField tfEmail;
//	@FXML TextField tfName;
//	@FXML TextField tfPassword;
//	@FXML TextField tfConfirmPassword;
//	@FXML TextField tfPhoneNumber;
//	@FXML Button btnConfirm;
//	@FXML ImageView imgAvatar;
//	
//	File fileImg = null;
//	
//	Socket socket;
//	UserInfo userInfo = UserInfo.getInstance();
//	
//	//Login
//	Main main;
//	
//	@FXML private TextField tfEmailLogin;
//	@FXML private TextField tfPasswordLogin;
//	@FXML private Button btnSignin;
//
//	ChatSocket chatsocket;
//	Runnable socketRunnable;
//	
//	//List online
//	ListOnline listOnline = ListOnline.getInstance();
//	
//	//List room
//	ListRoom listRoom = ListRoom.getInstance();
//	
//	public RegisterController() {
//		initSocket();
//		Thread socketThread = new Thread(socketRunnable);
//		socketThread.start();
//		
//		socket.on("authentication_result", new Listener() {
//			
//			@Override
//			public void call(Object... args) {
//				Platform.runLater(new Runnable() {				
//					@Override
//					public void run() {
//						JSONObject data = (JSONObject) args[0];
//						System.out.println(data);
//		                String result;
//		                try {
//		                	if (data != null){
////		                    userInfo.setFullName(data.getJSONObject("userRecord").getString("displayName"));
////		                    userInfo.setPhone(data.getJSONObject("userRecord").getString("phoneNumber"));
////		                    userInfo.setEmail(data.getJSONObject("userRecord").getString("email"));
//		                    //userInfo.setAvatarString(data.getJSONObject("userRecord").getString("avatarstring"));
//		                    
//		                    userInfo.setFullName(data.getString("fullname"));
//		                    userInfo.setPhone(data.getString("phone"));
//		                    userInfo.setEmail(data.getString("email"));
//		                    userInfo.setAvatarString(data.getString("avatarstring"));
//		                    
//		                    System.out.println(userInfo.getFullName());
//		                    System.out.println(userInfo.getPhone());
//		                    System.out.println(userInfo.getEmail());
//		                    System.out.println(userInfo.getAvatarString());
//		                    
//		                    	Platform.runLater(new Runnable(){
//		    						@Override
//		    						public void run() {		
//		    							Main.showMainFromLoginScene();
//		    						}       	 
//		                        });   	
//		                	}
//		                	else
//		                		Platform.runLater(new Runnable(){
//		    						@Override
//		    						public void run() {		
//		    							new Alert(AlertType.ERROR, "Your email or password not valid. Please try again").show();
//		    						}       	 
//		                        });  
//		                } catch (JSONException e) {
//		                	e.printStackTrace();
//		                    return;
//		                }	
//						
//					}
//				});
//				
//			}
//		}).on("user_online", new Listener() {	
//			@Override
//			public void call(Object... args) {
//			Platform.runLater(new Runnable() {
//				@Override
//				public void run() {
//					JSONObject object = (JSONObject)args[0];
//					try {
//						String name = object.getString("fullname");
//						String email = object.getString("email");
//						listOnline.addUserOnline(new UserOnline(name, email));
//						System.out.println("Register form: " + listOnline.getListName());
//						
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}	
//				}
//			});	
//			}
//		}).on("room_created", new Listener() {	
//			@Override
//			public void call(Object... args) {
//				Platform.runLater(new Runnable() {
//					@Override
//					public void run() {
//						JSONObject object = (JSONObject)args[0];
//						try {
//							String roomId = object.getString("room_id");
//							String roomName = object.getString("room_name");
//							System.out.println("roomName: " + roomName);
//							listRoom.addRoom(new Room(roomId, roomName, null));
//							System.out.println("Register form: " + listRoom.getListRoomName());
//						} catch (JSONException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				});
//				
//			}
//		});
//	}
//	
//	@Override
//	public void initialize(URL location, ResourceBundle resources) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@FXML public void handleButtonAction(javafx.event.ActionEvent event) {
//		if (event.getSource() == btnLogin) {
//			pn_Login.toFront();
//			Login();
//		} 
//		else if (event.getSource() == btnRegister ) {
//				pn_Register.toFront();
//				Register();	
//		}
//		else if (event.getSource() == btnExit) {
//		    // get a handle to the stage
//		    Stage stage = (Stage) btnExit.getScene().getWindow();
//		    // do what you have to do
//		    stage.close();
//		}
//	}
//	
//
//	//////////////////////////////// Login Panel ////////////////////////////////
//	
//	private void Login() {
//		// TODO Auto-generated method stub
////		initSocket();
////		Thread socketThread = new Thread(socketRunnable);
////		socketThread.start();
//		
////		socket.on("authentication_result", new Listener() {
////			
////			@Override
////			public void call(Object... args) {
////				JSONObject data = (JSONObject) args[0];
////				System.out.println(data);
////                String result;
////                try {
////                	if (data != null){
////                    userInfo.setFullName(data.getString("fullname"));
////                                    
////                    	Platform.runLater(new Runnable(){
////    						@Override
////    						public void run() {		
////    							Main.showMainFromLoginScene();
////    						}       	 
////                        });   	
////                	}
////                	else
////                		Platform.runLater(new Runnable(){
////    						@Override
////    						public void run() {		
////    							new Alert(AlertType.ERROR, "Your email or password not valid. Please try again").show();
////    						}       	 
////                        });  
////                } catch (JSONException e) {
////                	e.printStackTrace();
////                    return;
////                }	
////				
////			}
////		});
//	}
//	@FXML
//	public void onKeyPressedBtnSigin(KeyEvent keyevent){
//		if (keyevent.getCode().equals(KeyCode.ENTER))
//        {			
//			System.out.println("BtnSigin clicked");
//			Authentication();
//        }
//	}
//	
//	@FXML
//	public void onActionBtnSigin(){
//		System.out.println("BtnSigin clicked");
//		if (!isValidLogin()){
//			new Alert(AlertType.WARNING, "Something went wrong with your information. Please check again").show();
//			return;
//		}
//		
//		Authentication();
//	}
//	
//	public void initSocket(){
//		chatsocket = ChatSocket.getInstance();
//		socket = ChatSocket.getSocket();
//		socketRunnable = new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				//set up event
//				socket.on(Socket.EVENT_CONNECT, new Listener() {
//					
//					@Override
//					public void call(Object... args) {
//						// TODO Auto-generated method stub
//						System.out.println("Client connected to server");
//					}
//				}).on(Socket.EVENT_DISCONNECT, new Listener() {
//					
//					@Override
//					public void call(Object... args) {
//						// TODO Auto-generated method stub
//						System.out.println("Client disconnected to server");
//					}
//				});
//				
//				socket.connect();
//			}
//		};
//	}
//	
//	private boolean isValidLogin(){
//		
//		if (tfEmailLogin.getText().trim().length() == 0)
//			return false;
//		
//		if (tfPasswordLogin.getText().trim().length() == 0)
//			return false;
//		
//		return true;
//	}
//	
//	
//	//////////////////////////////// Register Panel //////////////////////////////////
//	public void Register() { 
////		ChatSocket.getInstance();
////		socket = ChatSocket.getSocket();
////			
//		socket.on("register_result", new Listener() {
//			
//			@Override
//			public void call(Object... args) {
//				JSONObject data = (JSONObject) args[0];
//	            String message;
//	            try {
//	                message = data.getString("message");
//	                System.out.println(message);
//	                Platform.runLater(new Runnable(){
//						@Override
//						public void run() {		
//							new Alert(AlertType.INFORMATION, message).show();
//						}       	 
//	                });
//	               
//	            } catch (JSONException e) {
//	            	e.printStackTrace();
//	                return;
//	            }	
//			}
//		});
//	}
//	@FXML
//	public void onKeyPressedBtnConfirm(KeyEvent keyevent){
//		if (keyevent.getCode().equals(KeyCode.ENTER))
//        {	
//			System.out.println("BtnConfirm clicked");
//			pushData();	
//        }
//	}
//	
//	@FXML
//    void onImgAvatarMouseClicked(MouseEvent event) {
//		FileChooser fileChooser = new FileChooser();
//        
//        //Set extension filter
//        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
//        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
//        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
//          
//        //Show open file dialog
//        fileImg = fileChooser.showOpenDialog(null);
//        try {
//            BufferedImage bufferedImage = ImageIO.read(fileImg);
//            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
//            imgAvatar.setImage(image);
//            imgAvatar.setFitWidth(96);
//            imgAvatar.setFitWidth(96);
//            
//        } catch (IOException ex) {
//        	ex.printStackTrace();
//        }
//    }
//	
//	public void onActionBtnConfirm(KeyEvent keyevent){
//		System.out.println("BtnConfirm clicked");
//
//		if (!isValidRegister()){
//			new Alert(AlertType.WARNING, "Something went wrong with your information. Please check again").show();
//			return;
//		}
//		
//		//Push data
//		pushData();				
//	}
//	
//	private boolean isValidRegister(){
//		System.out.println(tfEmail.getText().trim().length());
//		if (tfEmail.getText().trim().length() == 0)
//			return false;
//		if (tfName.getText().trim().length() == 0)
//			return false;
//		if (tfPassword.getText().trim().length() == 0)
//			return false;
//		if (tfConfirmPassword.getText().trim().length() == 0)
//			return false;
//		if (tfPhoneNumber.getText().trim().length() == 0)
//			return false;
//		if (!tfPassword.getText().toString().equals(tfConfirmPassword.getText().toString()))
//			return false;
//		return true;
//	}
//	
//	public void pushData() {
//		//Push data
//		userInfo = UserInfo.getInstance();
//			
//		userInfo.setEmail(tfEmail.getText().toString());
//		userInfo.setPassword(tfPassword.getText().toString());
//		userInfo.setPhone(tfPhoneNumber.getText().toString());
//		userInfo.setFullName(tfName.getText().toString());
//		if (fileImg != null)
//			userInfo.setAvatarString(ImageUtils.encodeFileToBase64Binary(fileImg));
//		else
//			userInfo.setAvatarString("null");
//		
//		//Convert to JSONObject
//		JSONObject userjson = new JSONObject();
//		try {
//			//userjson.put("userid", userInfo.getUserid());
//			userjson.put("email", userInfo.getEmail());
//			userjson.put("fullname", userInfo.getFullName());
//			userjson.put("password", userInfo.getPassword());
//			userjson.put("phone", userInfo.getPhone());
//			userjson.put("avatarstring", userInfo.getAvatarString());
//			
//			socket.emit("register", userjson);
//			
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public void Authentication() {
//		//Convert to JSONObject
//		JSONObject userjson = new JSONObject();
//			userjson.put("email", tfEmailLogin.getText());
//			userjson.put("password", tfPasswordLogin.getText());
//			
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		socket.emit("authentication", userjson);
//	}
//}
