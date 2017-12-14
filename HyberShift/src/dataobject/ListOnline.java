package dataobject;

import java.util.ArrayList;

public class ListOnline {
	private static ListOnline instance = null;
	private ArrayList<UserOnline> list;
	
	public ListOnline(){
		list = new ArrayList<>();
	}
	
	public static ListOnline getInstance(){
		if (instance == null)
			instance = new ListOnline();
		return instance;
	}
	
	public void setListOnline(ArrayList<UserOnline> listSource){
		this.list.clear();
		this.list = listSource;
	}
	
	public void addUserOnline(UserOnline newUser){
		//check if already in list
		for(int i=0; i<list.size(); i++)
			if (list.get(i).getEmail().equals(newUser.getEmail()))
				return;
		
		list.add(newUser);
	}
	
	public void removeUserOnline(String name){
		if (list.size() <= 0) return;
		for(int i=0; i<list.size(); i++){
			if (list.get(i).getName().equals(name)){
				list.remove(i);
			}
		}
	}
	
	public ArrayList<String> getListName(){
		ArrayList<String> listName = new ArrayList<>();
		for(int i=0; i<list.size(); i++)
			listName.add(list.get(i).getName());
		return listName;
	}
	
	public ArrayList<String> getListEmail(){
		ArrayList<String> listEmail = new ArrayList<>();
		for(int i=0; i<list.size(); i++)
			listEmail.add(list.get(i).getEmail());
		return listEmail;
	}
}
