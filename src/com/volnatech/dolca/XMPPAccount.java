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
import java.util.*;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.*;
import org.jivesoftware.smackx.provider.VCardProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class XMPPAccount extends Account {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected XMPPConnection connection = null;
	protected XMPPRosterListener rosterListener = null;
	protected XMPPChatManagerListener chatListener = null;

	XMPPAccount() {
		rosterListener = new XMPPRosterListener();
		chatListener = new XMPPChatManagerListener();
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		rosterListener = new XMPPRosterListener();
		chatListener = new XMPPChatManagerListener();
	}

	//	@Override public DChat startChatWithPerson(DFriend recipient){
	//		DXMPPChat xmppchat = new DXMPPChat(recipient, this);
	//		Chat chat = connection.getChatManager().createChat(recipient.getUserName(), xmppchat);
	//		xmppchat.userInfo = chat;
	//		return xmppchat;
	//	}
	//
	public void getFriendList(){
		if (connection!=null)
		{
			Roster roster = connection.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for(RosterEntry entry : entries) {
				Friend friend = getFriendWithName(entry.getUser());
				Friend existingFriend = AccountManager.getInstance().getFriends().get(friend.getUniqueDescriptior());
				if (existingFriend == null)
					accountListener.didAddFriend(friend);
			}
		}
	}

	@Override public void disconnect(){
		if (connection!=null)
		{
			Roster roster = connection.getRoster();
			if (roster!=null) {
				roster.removeRosterListener(rosterListener);
				connection.getChatManager().removeChatListener(chatListener);
				connection.disconnect();
				connection = null;
				accountListener.accountDidDisconnect(this);
			}
		}
		isOnline = false;
	}

	private void applyPresenceToFriend(Presence presence, Friend friend){
		friend.setStatusMessage(presence.getStatus());
		friend.setIsAvailable(presence.isAvailable());

		Friend.Status status = Friend.Status.NA;
		Presence.Mode mode = presence.getMode();
		if (mode!=null){
			switch (mode){
			case available: status = Friend.Status.AVAILABLE; break;
			case away: status = Friend.Status.AWAY; break;
			case chat: status = Friend.Status.CHAT; break;
			case dnd: status = Friend.Status.DND; break;
			case xa: status = Friend.Status.XA; break;	
			}
		}
		friend.setStatus(status);
	}

	private Friend getFriendWithName(String userName){
		final Friend friend = new Friend(userName);
		friend.setAccount(this);
		Roster roster = connection.getRoster();
		RosterEntry entry = roster.getEntry(userName);
		friend.setName(entry.getName());
		Presence bestPresence = roster.getPresence(userName);
		applyPresenceToFriend(bestPresence, friend);
		new Thread(new Runnable() {

			public void run() {
				try {
					VCard vcard = new VCard();
					vcard.load(connection, friend.getUserName());
					byte[] avatarBytes = vcard.getAvatar();
					if (avatarBytes!=null)
					{
						Bitmap bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
						friend.setAvatar(bitmap);
						accountListener.friendAvatarDidChange(friend);
					}
				} catch (XMPPException e) {
					// just ignore
				}	    
			}
		}).start();
		return friend;
	}

	private class XMPPRosterListener implements RosterListener {

		public void entriesAdded(Collection<String> nameList) {
			//			for (String userName : nameList){
			//				if (userName.contains("/")){
			//					userName = userName.split("/")[0];
			//				}
			//				Presence presence = connection.getRoster().getPresence(userName);
			//				Friend friend = getFriendWithName(userName);
			//				applyPresenceToFriend(presence, friend);
			//				accountListener.didAddFriend(friend);
			//			}
		}

		public void entriesDeleted(Collection<String> arg0) {
		}

		public void entriesUpdated(Collection<String> nameList) {
			for (String userName : nameList){
				HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
				if (userName.contains("/")){
					userName = userName.split("/")[0];
				}
				Presence presence = connection.getRoster().getPresence(userName);
				Friend friend = getFriendWithName(userName);
				friend = friends.get(friend.getUniqueDescriptior());
				applyPresenceToFriend(presence, friend);
				accountListener.friendStatusDidChange(friend);
			}
		}

		public void presenceChanged(Presence presence) {
			HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
			String from = presence.getFrom();
			if (from.contains("/")){
				from = from.split("/")[0];
			}
			Friend friend = getFriendWithName(from);
			friend = friends.get(friend.getUniqueDescriptior());
			applyPresenceToFriend(presence, friend);
			accountListener.friendStatusDidChange(friend);
		}
	}

	@Override public void setUserName(String userName) {
		if (userName.contains("@"))
			userName = userName.split("@")[0];
		super.setUserName(userName);
	}

	protected void processChatMessage(Chat chat, Message message){
		String body = message.getBody();
		if (body != null && body.length()>0)
		{
			String userName = chat.getParticipant().split("/")[0];
			Friend friend = getFriendWithName(userName);
			HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
			friend = friends.get(friend.getUniqueDescriptior());
			FriendMessage friendMessage = new FriendMessage(friend.getUniqueDescriptior(), XMPPAccount.this.getUserName(), body, false);
			accountListener.didReceiveMessageForFriend(friendMessage, friend);
		}
	}

	private class XMPPChatManagerListener implements ChatManagerListener {

		public void chatCreated(Chat chat, boolean arg1) {

			chat.addMessageListener(new MessageListener() {
				public void processMessage(Chat chat, Message message) {
					processChatMessage(chat, message);
				}
			});
		}
	}

	@Override
	public void sendMessage(FriendMessage message){
		HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
		String to = message.getTo();
		Friend recipient = friends.get(to);
		Object userInfo = recipient.getUserInfo();
		if (userInfo==null){
			Chat chat = connection.getChatManager().createChat(recipient.getUserName(), null);
			userInfo = chat;
			recipient.setUserInfo(userInfo);
		}
		try {
			((Chat)userInfo).sendMessage(message.getMessage());
			accountListener.didReceiveMessageForFriend(message, recipient);
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override public boolean connect(AccountLoginListener loginListener){
		final XMPPAccount self = this;
		this.loginListener = loginListener;
		ConnectionConfiguration config = new ConnectionConfiguration(self.getServerAddress(), self.getServerPort(), self.getService());
//		SASLAuthentication.registerSASLMechanism("DIGEST-MD5", SASLDigestMD5Mechanism.class);		
		config.setSASLAuthenticationEnabled(false);
		try {

			ProviderManager pm = ProviderManager.getInstance();
			pm.addIQProvider("vCard","vcard-temp", new VCardProvider());

			connection = new XMPPConnection(config);
			connection.connect();
			connection.login(self.getUserName(), self.getPassword());
			isOnline = true;
			this.loginListener.loginDidSucceeded();
			Roster roster = connection.getRoster();
			accountListener.accountDidLogin(this);
			connection.getChatManager().addChatListener(chatListener);

			new Thread(new Runnable() {

				public void run() {
					try {
						VCard vcard = new VCard();
						vcard.load(connection);
						byte[] avatarBytes = vcard.getAvatar();
						if (avatarBytes!=null)
						{
							Bitmap bitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
							XMPPAccount.this.getSelfAsFriend().setAvatar(bitmap);
						}
						String firstName = vcard.getFirstName();
						String lastName = vcard.getLastName();
						String name = null;
						if (firstName!=null)
							name = firstName;
						if (lastName!=null)
							if (name == null)
								name = lastName;
							else
								name = name + " " + lastName;
						if (name == null)
							name = vcard.getNickName();
						XMPPAccount.this.setName(name);
					} catch (Exception e) {
					}	    
				}
			}).start();



			new Thread(new Runnable() {
				public void run() {
					getFriendList();
				}
			}).start();
			roster.addRosterListener(rosterListener);
			
			connection.addConnectionListener(new ConnectionListener() {
				
				public void reconnectionSuccessful() {
					// TODO Auto-generated method stub
					
				}
				
				public void reconnectionFailed(Exception e) {
					// TODO Auto-generated method stub
					
				}
				
				public void reconnectingIn(int seconds) {
					// TODO Auto-generated method stub
					
				}
				
				public void connectionClosedOnError(Exception e) {
					// TODO Auto-generated method stub
					
				}
				
				public void connectionClosed() {
					// TODO Auto-generated method stub
					
				}
			});
			
		} catch (XMPPException e) {
			this.loginListener.loginDidFailedWithError(e.getLocalizedMessage()!=null?e.getLocalizedMessage():e.toString());
			return false;
		} catch (Exception e){
			this.loginListener.loginDidFailedWithError(e.getLocalizedMessage()!=null?e.getLocalizedMessage():e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
