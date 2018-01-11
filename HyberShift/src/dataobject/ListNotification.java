package dataobject;

import java.awt.List;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListNotification {
	private static ListNotification instance = null;
	private ArrayList<Notification> list;
	
	public ListNotification(){
		list = new ArrayList<>();
	}
	
	public static ListNotification getInstance(){
		if (instance == null)
			instance = new ListNotification();
		return instance;
	}
	
	public ArrayList<Notification> getNotificationList() { 
		return this.list; 
	}
	
	public void clear(){
		list.clear();
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
