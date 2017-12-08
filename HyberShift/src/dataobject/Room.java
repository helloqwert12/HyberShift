package dataobject;

import java.util.ArrayList;

public class Room {
	private String name;
	private String creator;	//uid of creator user
	private ArrayList<String> members;	//list of uid of users in room
	
	public Room(){
		
	}
	
	public Room(String name, String creator, ArrayList<String> members){
		this.name = name;
		this.creator = creator;
		this.members = members;
	}
	
	public String getName() { return name; }
	public String getCreator() { return creator; }
	public ArrayList<String> getMembers() { return members; }
	
	public void setName(String name) { this.name = name; }
	public void setCreator(String creator) { this.creator = creator; }
	public void setMemebers(ArrayList<String> members) { this.members = members; }
	
	public void addMemebers(String member) { members.add(member); } 
	public int getMemebersCount() { return members.size(); }
}
