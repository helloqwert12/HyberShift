package dataobject;

public class UserInfo {
	private String userid;
	private String email;
	private String password;
	private String phone;
	private String fullName;
	private String avatarString;
	private static UserInfo instance = null;
	
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
	
	public static UserInfo getInstance(){
		if (instance == null)
			instance = new UserInfo();
		
		return instance;
	}
	
	public String getUserid() { return userid; }
	public String getEmail() { return email; }
	public String getPassword() { return password; }
	public String getPhone() { return phone; }
	public String getFullName() { return fullName; }
	public String getAvatarString() { return avatarString;}
	
	public void setUserid(String userid) { this.userid = userid; }
	public void setEmail(String email) { this.email = email; }
	public void setPassword(String password) { this.password = password; }
	public void setPhone(String phone) { this.phone = phone; }
	public void setFullName(String fullName) { this.fullName = fullName; }
	public void setAvatarString(String avatarString) { this.avatarString = avatarString; }
}
