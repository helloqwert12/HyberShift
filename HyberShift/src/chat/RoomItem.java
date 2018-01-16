package chat;

import java.io.IOException;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import dataobject.Room;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;

public class RoomItem {

	 @FXML 
	 private Label lblRoomName;
	 
	 @FXML
	 private VBox vbox;
	 
	 @FXML
	 private Label lblMembers;
	 
	 @FXML
	 private MaterialDesignIconView iconNewMessage;
	 
	 private String members = "";

	 
	 public RoomItem(){
		 FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/chat/RoomItem.fxml"));
	        fxmlLoader.setController(this);
	        try
	        {
	            fxmlLoader.load();
	        }
	        catch (IOException e)
	        {
	            throw new RuntimeException(e);
	        }
	        
	        iconNewMessage.setVisible(false);
	 }

	 @FXML
	 void onMouseEnteredRoomItem(MouseEvent event) {
		 lblMembers.setTooltip(new Tooltip(members));
	 }

	 public void setInfo(Room room){
		 lblRoomName.setText(room.getName());
		 members += room.getMembers().get(0);
		 for(int i=1; i<room.getMembers().size(); i++){
			 members += ", " + room.getMembers().get(i);
		 }
		 
		 lblMembers.setText(members);
		 
		 // check new message alert
		 if (room.hasNewMessage())
			 iconNewMessage.setVisible(true);
		 else
			 iconNewMessage.setVisible(false);
	 }
	
	 public VBox getVBox(){
		 return vbox;
	 }
	 
	 public void showNotification(){
		 iconNewMessage.setVisible(true);
	 }
	 
	 public void hideNotification(){
		 iconNewMessage.setVisible(false);
	 }
}
