package application;
	
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import chatsocket.ChatSocket;

import com.github.nkzawa.socketio.client.Socket;
import com.jfoenix.controls.JFXButton;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.fxml.FXMLLoader;
import login.RegisterSceneController;


public class Main extends Application {
	//static Pane root;
	static Stage stg;
	
	private double xOffset = 0;
	private double yOffset = 0;
	

	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));	
			//Parent root = FXMLLoader.load(getClass().getResource("/chat/ChatScene.fxml"));	
			this.stg = primaryStage;
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			
			//Kéo thả, di chuyển frame
		    root.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>() {
		        @Override
		        public void handle(javafx.scene.input.MouseEvent event) {
		            xOffset = event.getSceneX();
		            yOffset = event.getSceneY();
		        }
		    });

		    root.setOnMouseDragged(new EventHandler<javafx.scene.input.MouseEvent>() {
		        @Override
		        public void handle(javafx.scene.input.MouseEvent event) {
		            primaryStage.setX(event.getScreenX() - xOffset);
		            primaryStage.setY(event.getScreenY() - yOffset);
		        }
		    });
			
			Scene scene  = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			primaryStage.setScene(scene);
			primaryStage.show();
			//Scene scene = new Scene(root,600,400);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			//primaryStage.setScene(scene);
			//primaryStage.show();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void showRegisterScene() throws IOException{
		try {
		    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/login/Register.fxml"));
		    Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));  
            stage.show();
            stg.close();
            stg = stage;
		            
		    } catch(Exception e) {
		       e.printStackTrace();
		      }
	}
	
	public static void showMainFromLoginScene(){
		try {
		    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/chat/ChatScene.fxml"));
		    Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));  
            stage.show();
            stg.close();
            stg = stage;
		            
		    } catch(Exception e) {
		       e.printStackTrace();
		    }
	}
	
	public static void showMainChatScene(){
		try {
		    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/chat/ChatScene.fxml"));
		    Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));  
            stage.show();
            stg.close();
            stg = stage;
		            
		    } catch(Exception e) {
		       e.printStackTrace();
		    }
	}
	
	public static void showCreateRoomScene(){
		try {
		    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/chat/CreateRoomScene.fxml"));
		    Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));  
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.show();	
            stg.close();
            stg = stage;
		            
		    } catch(Exception e) {
		       e.printStackTrace();
		    }
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}