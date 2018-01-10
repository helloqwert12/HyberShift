package dataobject;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListJournal {
	private static ListJournal instance = null;
	ArrayList<Journal> list;
	
	public static ListJournal getInstance(){
		if (instance == null)
			instance = new ListJournal();
		
		return instance;
	}
	
	public ListJournal(){
		list = new ArrayList<>();
	}
	
	public ArrayList<Journal> getList(){
		return list;
	}
	
	public ArrayList<String> getListWork(){
		ArrayList<String> result = new ArrayList<>();
		for(int i=0; i<list.size(); i++){
			result.add(list.get(i).getWork());
		}
		
		return result;
	}
	
	public void addJournal(Journal journal){
		//check
		for(int i=0; i<list.size(); i++){
			if (list.get(i).equals(journal))
				return;
		}
		
		list.add(journal);
	}
	
	public ObservableList<Journal> getOListJournal(){
		ObservableList<Journal> olist = FXCollections.observableArrayList(this.list);
		return olist;
	}
	
	public ObservableList<String> getOListWork(){
		ObservableList<String> olist = FXCollections.observableArrayList(this.getListWork());
		return olist;
	}
}
