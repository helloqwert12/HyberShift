package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;
import login.RegisterSceneController;


public class Main extends Application {
	//static Pane root;
	static Stage stg;
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("LoginScene.fxml"));
			this.stg = primaryStage;
			primaryStage.setTitle("Log in");
			primaryStage.setScene(new Scene(root, 900, 500));
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
		    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/login/RegisterScene.fxml"));
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
		    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/mainchat/ChatScene.fxml"));
			//FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("Register.fxml"));
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
	
	public static void showMainFromRegister(){
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
