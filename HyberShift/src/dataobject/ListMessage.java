package dataobject;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListMessage {
	private static ListMessage instance;
	private ArrayList<Message> list;
	
	public ListMessage(){
		list = new ArrayList<>();
	}
	
	static public ListMessage getInstance(){
		if (instance == null)
			instance = new ListMessage();
		return instance;
	}
	
	public void addMessage(Message msg){
		//check exist
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getTimestamp() == msg.getTimestamp())
				return;
		}
		list.add(msg);
	}
	
	public void setListMessage(ArrayList<Message> list) { this.list = list; }
	
	public ArrayList<Message> getList() { return list; }
	
	public ArrayList<String> getListMessage(){
		ArrayList<String> result = new ArrayList<>();
		for(int i=0; i<list.size(); i++){
			result.add(list.get(i).getMessage());
		}
		
		return result;
	}
	
	public ArrayList<String> getListSender(){
		ArrayList<String> result = new ArrayList<>();
		for(int i=0; i<list.size(); i++){
			result.add(list.get(i).getSender());
		}
		
		return result;
	}
	
	
	public Message getMessageFromId(String id){
		for(int i=0; i<list.size(); i++){
			Message temp = list.get(i);
			if (temp.getId().equals(id))
				return temp;
		}
		
		return null;
	}
	
	public Message getMessageFromSender(String sender){
		for(int i=0; i<list.size(); i++){
			Message temp = list.get(i);
			if (temp.getSender().equals(sender))
				return temp;
		}
		
		return null;
	}
	
	public ObservableList<String> getOListSender(){
		ObservableList<String> olist = FXCollections.observableArrayList(this.getListSender());
		return olist;
	}
	
	public ObservableList<String> getOListMessage(){
		ArrayList<String> msgList = new ArrayList<>();
		for(int i=0; i<list.size(); i++){
			String msg = list.get(i).getSender() + ": " + list.get(i).getMessage();
			msgList.add(msg);
		}
		ObservableList<String> olist = FXCollections.observableArrayList(msgList);
		return olist;
	}
	
}
