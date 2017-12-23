package dataobject;

public class Message {
	private String id;
	private String message;
	private String sender;	// name of sender
	private int timestamp;
	
	public Message(){
		
	}
	
	public Message(String id, String message, String sender, int timestamp){
		this.id = id;
		this.message = message;
		this.sender = sender;
		this.timestamp = timestamp;
	}
	
	public String getId() { return id; }
	public String getMessage() { return message; }
	public String getSender() { return sender; }
	public int getTimestamp() { return timestamp; }
	
	public void setId(String id) { this.id = id; }
	public void setMessage(String message) { this.message = message; }
	public void setSender(String sender) { this.sender = sender; }
	public void setTimestamp(int timestamp) { this.timestamp = timestamp; }
}
