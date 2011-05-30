package com.volnatech.dolca;

import java.io.IOException;

public class GtalkAccount extends XMPPAccount {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GtalkAccount(){
		this.setServerAddress("talk.google.com");
		this.setServerPort(5222);
		this.setService("gmail.com");
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.setServerAddress("talk.google.com");
		this.setServerPort(5222);
		this.setService("gmail.com");
	}
	
	@Override
	public void setUserName(String userName) {
		if (userName.contains("@"))
			userName = userName.split("@")[0];
		super.setUserName(userName);
	}
}
