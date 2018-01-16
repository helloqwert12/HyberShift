package dataobject;

public class FriendInfo {
	private String id;
	private String name;
	private String email;
	private String imgstring;

	public FriendInfo(){
		
	}
	
	public String getId() { return id; }
	public String getName() { return name; }
	public String getEmail() { return email; }
	public String getImgstring() { return imgstring; }
	
	public void setId(String id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setEmail(String email) { this.email = email; }
	public void setImgstring(String imgstring) { this.imgstring = imgstring; }
	
}
