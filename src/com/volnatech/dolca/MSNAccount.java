package com.volnatech.dolca;

import java.io.IOException;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import net.sf.jml.*;
import net.sf.jml.event.*;
import net.sf.jml.impl.*;
import net.sf.jml.message.*;
import net.sf.jml.message.p2p.*;

public class MSNAccount extends Account {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public MsnMessenger messenger = null;
	
	MSNAccount(){
		this.setService("msn_service");
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.setService("msn_service");
	}
	
	public Friend getFriendFromContact(MsnContact contact, boolean needFullInfo){
		Friend friend = new Friend(contact.getEmail().getEmailAddress());
		friend.setAccount(this);
		friend.setName(contact.getDisplayName());
		if (needFullInfo)
			friendApplyNewStatusFromContact(friend, contact);
		return friend;
	}
	
	public void friendApplyNewStatusFromContact(Friend friend, MsnContact contact){
		MsnUserStatus status = contact.getStatus();
		friend.setIsAvailable((contact.getStatus() != MsnUserStatus.OFFLINE));
		friend.setStatusMessage(contact.getPersonalMessage());

		if (status == MsnUserStatus.ONLINE){
			friend.setStatus(Friend.Status.AVAILABLE);
		} else if (status == MsnUserStatus.IDLE){
			friend.setStatus(Friend.Status.AWAY);
		} else if (status == MsnUserStatus.BUSY){
			friend.setStatus(Friend.Status.DND);
		} else if (status == MsnUserStatus.AWAY){
			friend.setStatus(Friend.Status.AWAY);
		} else if (status == MsnUserStatus.BE_RIGHT_BACK){
			friend.setStatus(Friend.Status.AWAY);
		} else if (status == MsnUserStatus.ON_THE_PHONE){
			friend.setStatus(Friend.Status.NA);
		}

	}
	
	public void tryToLoadAvatar(final Friend friend, MsnContact contact){
		final MsnObject avatar = contact.getAvatar();
		if (avatar != null) {

			new Thread(new Runnable() {						
				public void run() {
					messenger.retrieveDisplayPicture(avatar, new DisplayPictureListener() {
						public void notifyMsnObjectRetrieval(
								MsnMessenger messenger,
								DisplayPictureRetrieveWorker worker,
								MsnObject msnObject, ResultStatus result,
								byte[] resultBytes, Object context) {
							if (result == ResultStatus.GOOD) {
								if (resultBytes!=null)
								{
									Bitmap bitmap = BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.length);
									friend.setAvatar(bitmap);
									accountListener.friendAvatarDidChange(friend);
								}
							}
						}
					});
				}
			}).start();
		}
	}

	protected void initMessenger(MsnMessenger messenger) {
		messenger.getOwner().setInitStatus(MsnUserStatus.ONLINE);
		messenger.addContactListListener(new MsnContactListAdapter() {
			
			public void contactListInitCompleted(MsnMessenger messenger) {
				MsnContact[] contacts = messenger.getContactList().getContactsInList(MsnList.AL);
				for (MsnContact contact : contacts){
					Friend friend = getFriendFromContact(contact, false);
					HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
					Friend existingFriend = friends.get(friend.getUniqueDescriptior());
					if (existingFriend!=null)
						tryToLoadAvatar(existingFriend, contact);
				}
			}
			public void contactPersonalMessageChanged(MsnMessenger messenger, MsnContact contact) {
				HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
				Friend friend = getFriendFromContact(contact, false);
				friend = friends.get(friend.getUniqueDescriptior());
				friendApplyNewStatusFromContact(friend, contact);
				accountListener.friendStatusDidChange(friend);
			}

			public void contactStatusChanged(MsnMessenger messenger, MsnContact contact) {
				HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
				Friend friend = getFriendFromContact(contact, false);
				friend = friends.get(friend.getUniqueDescriptior());
				friendApplyNewStatusFromContact(friend, contact);
				accountListener.friendStatusDidChange(friend);
			}

			public void contactListSyncCompleted(MsnMessenger messenger){
				MsnContact[] contacts = messenger.getContactList().getContactsInList(MsnList.AL);
				for (MsnContact contact : contacts){
					Friend friend = getFriendFromContact(contact, false);
					HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
					Friend existingFriend = friends.get(friend.getUniqueDescriptior());
					if (existingFriend == null){
						friendApplyNewStatusFromContact(friend, contact);
						accountListener.didAddFriend(friend);
						existingFriend = friend;
					} else {
						friendApplyNewStatusFromContact(existingFriend, contact);
						accountListener.friendStatusDidChange(existingFriend);	
					}
				}
				messenger.getOwner().setStatus(MsnUserStatus.ONLINE);
			}
		});

		messenger.addMessengerListener(new MsnMessengerListener(){

			public void exceptionCaught(MsnMessenger arg0, Throwable arg1) {
				if (isOnline == false){
					String error = arg1.getLocalizedMessage();
					MSNAccount.this.loginListener.loginDidFailedWithError(error!=null?error:arg1.toString());
				} else {
					
				}
			}

			public void loginCompleted(MsnMessenger arg0) {
				isOnline = true;
				MSNAccount.this.loginListener.loginDidSucceeded();
				accountListener.accountDidLogin(MSNAccount.this);
				String displayName = MSNAccount.this.messenger.getOwner().getDisplayName();
				if (displayName!=null && displayName.length()>0)
					MSNAccount.this.setName(displayName);
			}

			public void logout(MsnMessenger arg0) {
				accountListener.accountDidDisconnect(MSNAccount.this);
				isOnline = false;
			}

		});
		
		messenger.addSwitchboardListener(new MsnSwitchboardListener() {
			
			public void switchboardStarted(MsnSwitchboard arg0) {
			}
			
			public void switchboardClosed(MsnSwitchboard switchboard) {
				HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
				for (Friend friend : friends.values()) {
					if (friend.getUserInfo() == switchboard) {
						if (friend.getUserInfo()!=null) {
							friend.setUserInfo(null);
							FriendMessage message = new FriendMessage(friend.getUniqueDescriptior(), MSNAccount.this.getUserName(), "<-- (Conversation closed) -->", false);
							accountListener.didReceiveMessageForFriend(message, friend);
						}
					}
				}
			}
			
			public void contactLeaveSwitchboard(MsnSwitchboard switchboard, MsnContact contact) {
				Friend friend = getFriendFromContact(contact, false);
				HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
				friend = friends.get(friend.getUniqueDescriptior());
				if (friend!=null)
					friend.setUserInfo(null);
			}
			
			public void contactJoinSwitchboard(MsnSwitchboard arg0, MsnContact arg1) {
			}
		});
		
		messenger.addMessageListener(new MsnMessageAdapter() {

            public void instantMessageReceived(MsnSwitchboard switchboard,
                    MsnInstantMessage message, MsnContact contact) {
                
            	Friend friend = getFriendFromContact(contact, false);
				HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
				friend = friends.get(friend.getUniqueDescriptior());
				friend.setUserInfo(switchboard);
    			FriendMessage friendMessage = new FriendMessage(friend.getUniqueDescriptior(), MSNAccount.this.getUserName(), message.getContent(), false);
    			accountListener.didReceiveMessageForFriend(friendMessage, friend);
            	
//                switchboard.sendMessage(message);
            }

            public void controlMessageReceived(MsnSwitchboard switchboard,
                    MsnControlMessage message, MsnContact contact) {
                //such as typing message and recording message
//                switchboard.sendMessage(message);
            }

            public void datacastMessageReceived(MsnSwitchboard switchboard,
                    MsnDatacastMessage message, MsnContact contact) {
                //such as Nudge
//                switchboard.sendMessage(message);
            }

        });
		
	}
	
	@Override
	public void sendMessage(final FriendMessage message){
		HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
		String to = message.getTo();
		final Friend recipient = friends.get(to);
		final MsnInstantMessage reply = new MsnInstantMessage();
        reply.setContent(message.getMessage());		

		MsnSwitchboard switchboard = (MsnSwitchboard)recipient.getUserInfo();
		if (switchboard == null){
			final Object id = new Object();
			messenger.addSwitchboardListener(new MsnSwitchboardAdapter() {
				public void switchboardStarted(MsnSwitchboard switchboard) {
                    if (id != switchboard.getAttachment())
                        return;
                    switchboard.inviteContact(Email.parseStr(recipient.getUserName()));
                }
				
				  public void contactJoinSwitchboard(MsnSwitchboard switchboard, MsnContact contact) {
                      if (id != switchboard.getAttachment())
                          return;
                      switchboard.sendMessage(reply);
                      recipient.setUserInfo(switchboard);
                      accountListener.didReceiveMessageForFriend(message, recipient);
				  }
			});
			messenger.newSwitchboard(id);
		} else {
			switchboard.sendMessage(reply);
			accountListener.didReceiveMessageForFriend(message, recipient);
		}
		
//		Object userInfo = recipient.getUserInfo();
//		if (userInfo==null){
//			Chat chat = connection.getChatManager().createChat(recipient.getUserName(), null);
//			userInfo = chat;
//			recipient.setUserInfo(userInfo);
//		}
//		try {
//			((Chat)userInfo).sendMessage(message.getMessage());
//			accountListener.didReceiveMessageForFriend(message, recipient);
//		} catch (XMPPException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	@Override public boolean connect(Account.AccountLoginListener loginListener){
		this.loginListener = loginListener;
		try {
			messenger = MsnMessengerFactory.createMsnMessenger(this.getUserName(), this.getPassword());
			messenger.setLogIncoming(true);
			messenger.setLogOutgoing(true);
			initMessenger(messenger);
			messenger.login();

		} catch (Exception e) {
			loginListener.loginDidFailedWithError(e.getLocalizedMessage()!=null?e.getLocalizedMessage():e.toString());
			return false; 
		}
		return true;
	}
	
	@Override public void disconnect(){
		if (messenger!=null)
			messenger.logout();
	}

}
