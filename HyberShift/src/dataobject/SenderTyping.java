package dataobject;

public class SenderTyping {
	private String senderName; 	//name of sender
	private int index;			//index of "typing" message in listview
	
	public SenderTyping(){
		
	}
	
	public SenderTyping(String senderName, int index){
		this.senderName = senderName;
		this.index = index;
	}
	
	public String getSenderName() { return this.senderName; }
	public int getIndex() { return this.index; }
	
	public void setSenderName(String senderName) { this.senderName = senderName; }
	public void setIndex(int index) { this.index = index; }
}
