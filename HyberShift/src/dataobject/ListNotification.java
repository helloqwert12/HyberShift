package dataobject;

import java.awt.List;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListNotification {
	ArrayList<Notification> list;
	
	public ListNotification(){
		list = new ArrayList<>();
	}
	
	public ArrayList<Notification> getNotificationList() { 
		return this.list; 
	}
	
	public void addNotification(Notification notification){
		//check
		for(int i=0; i<list.size(); i++){
			if (notification.equals(list.get(i)))
				return;
		}
		
		list.add(notification);
	}
	
	public ObservableList<Notification> getOList(){
		return FXCollections.observableArrayList(this.list);
	}
}
