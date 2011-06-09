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

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Friend implements Comparable<Friend> {

	public static final String FriendLock = "FriendLock";
    
	public enum Status {
	    NA, AVAILABLE, CHAT, AWAY, DND, XA 
	}
	private String name = null;
	private String email = null;
	private String statusMessage = null;
	private boolean isAvailable = false;
	private Status status = Status.NA;
	private Account account = null;
	private String userName = null;
	private Bitmap avatar = null;
	private ArrayList<FriendMessage> messages = null;
	private Object userInfo = null;
	private int unreadMessagesCount = 0;
	
	Friend(String userName){
		setUserName(userName);
		messages = new ArrayList<FriendMessage>();
	}
	
	public String getUniqueDescriptior(){
		return getUserName() + "(at)" + account.getService();
	}

	public void setName(String name) {
		this.name = name;
	}
    
	public String getName() {
		return name;
	}
	
	public String getEmail(){
		return email;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setIsAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	public Boolean getIsAvailable() {
		return isAvailable;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}
	public int compareTo(Friend another) {
		int ret = 0;
		if (this.getIsAvailable() && !another.getIsAvailable()) return -1;
		if (!this.getIsAvailable() && another.getIsAvailable()) return 1;
		if (this.getIsAvailable() == another.getIsAvailable()){
			ret = this.getStatus().compareTo(another.getStatus());
			if (ret == 0)
			{
				String name1 = this.getName();
				String name2 = another.getName();
				if (name1 == null) name1 = this.getUserName();
				if (name2 == null) name1 = another.getUserName();
				return name1.compareTo(name2);	
			}
			return ret;
		}
		return ret;
	}
    
	public void setAccount(Account account) {
		this.account = account;
	}
    
	public Account getAccount() {
		return account;
	}
    
	public void setUserName(String userName) {
		this.userName = userName;
	}
    
	public String getUserName() {
		return userName;
	}

	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}

	public Bitmap getAvatar() {
		return avatar;
	}

	public void setMessages(ArrayList<FriendMessage> messages) {
		this.messages = messages;
	}

	public ArrayList<FriendMessage> getMessages() {
		return messages;
	}
	
	public void addMessage(FriendMessage message){
		messages.add(message);
	}

	public void setUserInfo(Object userInfo) {
		this.userInfo = userInfo;
	}

	public Object getUserInfo() {
		return userInfo;
	}

	public void setUnreadMessagesCount(int unreadMessagesCount) {
		this.unreadMessagesCount = unreadMessagesCount;
	}

	public int getUnreadMessagesCount() {
		return unreadMessagesCount;
	}
}
