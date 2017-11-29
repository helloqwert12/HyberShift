package dataobject;

public class UserInfo {
	private String userid;
	private String email;
	private String password;
	private String phone;
	private String fullName;
	private String linkAvatar;
	
	public UserInfo(){
		
	}
	
	public UserInfo(String email, String pass, String phone, String fullName){
		//this.userid = userid;
		this.email = email;
		this.password = pass;
		this.phone = phone;
		this.fullName = fullName;
		//this.linkAvatar = linkAvatar;
	}
	
	public String getUserid() { return userid; }
	public String getEmail() { return email; }
	public String getPassword() { return password; }
	public String getPhone() { return phone; }
	public String getFullName() { return fullName; }
	public String getLinkAvatar() { return linkAvatar;}
	
	public void setUserid(String userid) { this.userid = userid; }
	public void setEmail(String email) { this.email = email; }
	public void setPhone(String phone) { this.phone = phone; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public void setLinkAvatar(String linkAvatar) { this.linkAvatar = linkAvatar; }
}
