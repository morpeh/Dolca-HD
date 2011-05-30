package com.volnatech.dolca;

import java.io.IOException;

public class FacebookAccount extends XMPPAccount {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	FacebookAccount(){
		this.setServerAddress("chat.facebook.com");
		this.setServerPort(5222);
		this.setService(null);
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.setServerAddress("chat.facebook.com");
		this.setServerPort(5222);
		this.setService(null);
	}
	
	@Override
	public void setUserName(String userName) {
		if (userName.contains("@"))
			userName = userName.split("@")[0];
		userName = userName + "@chat.facebook.com"; 
		super.setUserName(userName);
	}
}
