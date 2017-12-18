package dataobject;

import java.util.ArrayList;

import javafx.collections.ObservableList;

import javafx.collections.FXCollections;

public class ListRoom {
	private static ListRoom instance;
	private ArrayList<Room> list;
	
	public ListRoom(){
		list = new ArrayList<>();
	}
	
	public static ListRoom getInstance(){
		if (instance == null)
			instance = new ListRoom();
		return instance;
	}
	
	public ArrayList<Room> getListRoom() { 
		return this.list; 
	}
	
	public ArrayList<String> getListRoomName(){
		ArrayList<String> lst = new ArrayList<>();
		for(int i=0; i<list.size(); i++){
			lst.add(list.get(i).getName());
		}
		
		return lst;
	}
	
	public ArrayList<String> getMembersFrom(String roomName){
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getName().equals(roomName)){
				return list.get(i).getMembers();
			}
		}
		
		return new ArrayList<>();
	}
	
	public ArrayList<String> getMembersFrom(int indexRoom){
		if (indexRoom >= list.size())
			return new ArrayList<>();
		
		return list.get(indexRoom).getMembers();
	}
	
	public void addRoom(Room room){
		//kiểm tra trùng trước khi add
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getName().equals(room.getName()))
				return;
		}
		list.add(room);
	}
	
	public ObservableList<String> getOListRoomName(){
		ObservableList<String> olist = FXCollections.observableArrayList(this.getListRoomName());
		return olist;
	}
	

}
