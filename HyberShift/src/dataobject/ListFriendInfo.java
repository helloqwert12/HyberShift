package dataobject;

import java.awt.List;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListFriendInfo {
	
	private static ListFriendInfo instance = null;
	private ArrayList<FriendInfo> list;
	
	public static ListFriendInfo getInstance(){
		if (instance == null)
			instance = new ListFriendInfo();
		return instance;
	}
	
	public ListFriendInfo(){
		list = new ArrayList<>();
	}
	
	public void addFriendInfo(FriendInfo info){
		for(int i=0; i<list.size(); i++){
			if (list.get(i).equals(info))
				return;
		}
		
		list.add(info);
	}
	
	public ObservableList<FriendInfo> getOList(){
		return FXCollections.observableArrayList(this.list);
	}
	
}
