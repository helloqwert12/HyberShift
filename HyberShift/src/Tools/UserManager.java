package Tools;

import java.util.ArrayList;

public class UserManager {
	
	private java.util.List<User> UserList;
	
	UserManager()
	{
		UserList = new ArrayList<>();
	}
	
	private void addUser(User user)
	{
		UserList.add(user);
	}
	private void removeUser(User user)
	{
		UserList.remove(user);
	}
	
	private User getUser(String name)
	{
		for ( int  i =0 ; i < UserList.size() ; i++)
		{
			if ( UserList.get(i).getName() == name)
			{
				return UserList.get(i);
			}
		}
		return null;
	}
}
