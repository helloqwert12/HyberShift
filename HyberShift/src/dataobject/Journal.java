package dataobject;

import java.util.ArrayList;

// class chứa thông tin journal của 1 project

public class Journal {
	private String id;
	private String work;
	private ArrayList<String> listPerformer;	// những người thực hiện (name)
	private boolean isDone;								// dùng cho checkbox
	private String startDay;
	private String endDay;
	
	public Journal(){
		listPerformer = new ArrayList<>();
	}
	
	public Journal(String id, String work, ArrayList<String> listPerformer, boolean isDone, String startDay, String endDay){
		listPerformer = new ArrayList<>();
		this.id = id;
		this.work = work;
		this.listPerformer = listPerformer;
		this.isDone = isDone;
		this.startDay = startDay;
		this.endDay = endDay;
	}
	
	public String getId() { return id; }
	public String getWork() { return work; }
	public ArrayList<String> getListPerformer() { return listPerformer; }
	public boolean IsDone() { return isDone; }
	public String getStartDay() { return startDay; }
	public String getEndDay() { return endDay; }
	
	public void setId(String id) { this.id = id; }
	public void setWork(String work) { this.work = work; }
	public void setListPerformer(ArrayList<String> listPerformer) { this.listPerformer = listPerformer; }
	public void setDone(boolean value) { isDone = value; }
	public void setStartDay(String startDay) { this.startDay = startDay; }
	public void setEndDay(String endDay) { this.endDay = endDay; }
	
	public void addPerformer(String name){
		
		//check
		for(int i=0; i<listPerformer.size(); i++){
			if (listPerformer.get(i).equals(name))
				return;
		}
		
		listPerformer.add(name);
	}
}
