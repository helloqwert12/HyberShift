package dataobject;

import java.util.ArrayList;

public class Room {
	private static Room instance = null;
	private String name;
	private ArrayList<String> members;	//list of name of users in room
	
	public Room(){
		
	}
	
	public Room(String name, ArrayList<String> members){
		this.name = name;
		this.members = members;
	}
	
	public static Room getInstance(){
		if (instance == null)
			instance = new Room();
		return instance;
	}
	
	public String getName() { return name; }
	public ArrayList<String> getMembers() { return members; }
	
	public void setName(String name) { this.name = name; }
	public void setMemebers(ArrayList<String> members) { this.members = members; }
	
	public void addMemebers(String member) { members.add(member); } 
	public int getMemebersCount() { return members.size(); }
}
