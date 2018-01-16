package friend;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import Tools.ImageUtils;

import com.jfoenix.controls.JFXButton;

import dataobject.FriendInfo;

public class FriendInfoItem {

	  @FXML
	  private Circle cimgAvatar;
	  
	  @FXML
	  private Label lblName;

	  @FXML
	  private JFXButton btnAddFriend;
	  
	  @FXML
	  private Label lblEmail;
	  
	  @FXML
	  private VBox vbox;
	  
	  public FriendInfoItem(){
		  
	  }

	  @FXML
	  void onActionBtnAddFriend(ActionEvent event) {
		  System.out.println("BtnAddFriend clicked");
	  }
	    
	  public void setInfo(FriendInfo info) throws IOException{
		  lblName.setText(info.getName());
		  lblEmail.setText(info.getEmail());
		  
		  if (info.getImgstring() == null || info.getImgstring().equals("null"))
			  return;
		  
		  cimgAvatar.setFill(new ImagePattern(ImageUtils.decodeBase64BinaryToImage(info.getImgstring())));
	  }
	  
	  public VBox getVBox(){
		  return vbox;
	  }
}
