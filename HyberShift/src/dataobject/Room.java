package dataobject;

import java.util.ArrayList;

public class Room {
	private String id;
	private String name;
	private ArrayList<String> members;	//list of name of users in room
	
	public Room(){
		members = new ArrayList<>();
	}
	
	public Room(String id, String name, ArrayList<String> members){
		this.id = id;
		this.name = name;
		this.members = members;
	}
	
	public String getId() { return id; }
	public String getName() { return name; }
	public ArrayList<String> getMembers() { return members; }
	
	public void setId(String id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setMemebers(ArrayList<String> members) { this.members = members; }
	
	public void addMemebers(String member) { members.add(member); } 
	public int getMemebersCount() { return members.size(); }
}
