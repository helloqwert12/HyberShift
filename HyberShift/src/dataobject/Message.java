package dataobject;

public class Message {
	private String id;
	private String message;
	private String sender;	// name of sender
	private String imgstring;	//image string of sender (base64)
	private int timestamp;
	
	public Message(){
		
	}
	
	public Message(String id, String message, String sender, String imgstring, int timestamp){
		this.id = id;
		this.message = message;
		this.sender = sender;
		this.timestamp = timestamp;
		this.imgstring = imgstring;
	}
	
	public String getId() { return id; }
	public String getMessage() { return message; }
	public String getSender() { return sender; }
	public String getImgString() { return imgstring; }
	public int getTimestamp() { return timestamp; }
	
	public void setId(String id) { this.id = id; }
	public void setMessage(String message) { this.message = message; }
	public void setSender(String sender) { this.sender = sender; }
	public void setImgString(String imgstring) { this.imgstring = imgstring; }
	public void setTimestamp(int timestamp) { this.timestamp = timestamp; }
}
