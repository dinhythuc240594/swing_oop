package com.project01;

public class SessionManager {
	
	private static SessionManager instance;
	private int userId;
	private String username;
	private String userRole;
	private boolean isLoggedIn;
	
	private SessionManager() {
		setLoggedIn(false);
	}
	
	public static SessionManager getInstance() {
		if(instance == null) {
			instance = new SessionManager();
		}
		return instance;
	}
	
	public void login(int userId, String username, String userRole) {
		this.setUserId(userId);
		this.setUsername(username);
		this.setUserRole(userRole);
		this.setLoggedIn(true);
	}
	
	public void logout() {
		this.setUserId(-1);
		this.setUsername(null);
		this.setUserRole(null);
		this.setLoggedIn(false);
	}
	
	public boolean isAdmin() {
		return isLoggedIn && "administrator".equals(userRole);
	}

	public boolean isManager() {
		return isLoggedIn && "manager".equals(userRole);
	}

	public boolean isEmployee() {
		return isLoggedIn && "employee".equals(userRole);
	}
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}
	
	
}
