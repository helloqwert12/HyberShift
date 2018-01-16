package friend;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import com.jfoenix.controls.JFXBadge;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;

import dataobject.FriendInfo;
import dataobject.ListFriendInfo;

public class AddFriendController implements Initializable {
	 @FXML
	 private JFXTextField tfFind;

	 @FXML
	 private JFXListView<FriendInfo> lvResult;
	 
	 @FXML
	 private JFXButton button;
	 
	 ListFriendInfo listFriendInfo = ListFriendInfo.getInstance();
	  
	 public AddFriendController(){
   
	 }
	
	  
	  @FXML
	  void onKeyTypedTfFind(KeyEvent event) {
		  System.out.println(tfFind.getText());
	  }



	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lvResult.setCellFactory(new Callback<ListView<FriendInfo>, ListCell<FriendInfo>>() {
			@Override
			public ListCell<FriendInfo> call(ListView<FriendInfo> param) {
				return new FriendInfoCell();
			}
		});
	}
	
	public static class FriendInfoCell extends ListCell<FriendInfo>{
		 @Override
		 public void updateItem(FriendInfo info, boolean empty){
			 super.updateItem(info, empty);
				setText(null);
				setGraphic(null);
				if (info!=null && !empty){
					FriendInfoItem item = new FriendInfoItem();
					try {
						item.setInfo(info);
						setGraphic(item.getVBox());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		 }
	 }
}
