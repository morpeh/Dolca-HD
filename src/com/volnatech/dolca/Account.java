/*Copyright (C) 2011 by Alexander Voloshyn

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.volnatech.dolca;

import java.io.*;

public class Account implements Serializable {

	private static final long serialVersionUID = 1L;
	private String serverAddress = null;
	private int serverPort = 0;
	private String userName = null;
	private String password = null;
	private String service = null;
	private boolean autoConnect = true;
	protected boolean isOnline = false;
	protected Friend selfAsFriend = null;
	private String name = null;

	public interface AccountListener {
		public void accountDidLogin(Account account);
		public void friendStatusDidChange(Friend friend);
		public void friendAvatarDidChange(Friend friend);
		public void didAddFriend(Friend friend);
		public void didReceiveMessageForFriend(FriendMessage message, Friend friend);
		public void accountDidDisconnect(Account account);
	}

	protected AccountLoginListener loginListener = null;
	protected AccountListener accountListener = null; 

	Account(){
	}
	
	public Friend getSelfAsFriend(){
		if (selfAsFriend == null){
			selfAsFriend = new Friend(this.getUserName());
			selfAsFriend.setUserName(this.getUserName());
			selfAsFriend.setAccount(this);
		}
		return selfAsFriend;
	}
	
	public void sendMessage(FriendMessage message){
		
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(userName);
		out.writeObject(password);
		out.writeObject(service);
		out.writeBoolean(autoConnect);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		userName = (String) in.readObject();
		password = (String) in.readObject();
		service = (String) in.readObject();
		autoConnect = in.readBoolean();
	}

	public boolean connect(AccountLoginListener loginListener){
		loginListener.loginDidFailedWithError("Account->connect: Method is not implemented");
		return false;
	}

	public void disconnect(){

	}

	public String getService(){
		return service;
	}

	public void setService(String service){
		this.service = service;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setAutoConnect(boolean autoConnect, AccountLoginListener loginListener) {
		if (this.autoConnect != autoConnect)
		{
			this.autoConnect = autoConnect;
			if (this.autoConnect == false)
			{
				this.disconnect();
				loginListener.loginDidSucceeded();
			}
			else this.connect(loginListener);
		}
		else
		{
			loginListener.loginDidSucceeded();
		}
	}

	public boolean isAutoConnect() {
		return autoConnect;
	}
	
	public void setAccountListener(AccountListener accountListener) {
		this.accountListener = accountListener;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static abstract class AccountLoginListener {
		public abstract void loginDidSucceeded();
		public abstract void loginDidFailedWithError(String error);
	}
}
