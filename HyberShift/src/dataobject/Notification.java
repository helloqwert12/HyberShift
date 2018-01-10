package dataobject;

public class Notification {
	private String sender;	//name of sender
	private String imgstring;
	private String content;	
	private String type;	// type of notification
	private int timestamp;
	
	public Notification(){
		
	}
	
	public String getSender() { return sender; }
	public String getImgString() { return imgstring; }
	public String getContent() { return content; }
	public String getType() { return type; }
	public int getTimestamp() { return timestamp; }
	
	public void setSender(String sender) { this.sender = sender; }
	public void setImgString(String imgstring) { this.imgstring = imgstring; }
	public void setContent(String content) { this.content = content; }
	public void setType(String type) { this.type = type; }
	public void setTimestamp(int timestamp) { this.timestamp = timestamp; }
}
