package com.volnatech.dolca;

import java.beans.PropertyChangeEvent;
import java.io.IOException;

import java.io.*;
import java.util.*;
import android.content.*;
import android.graphics.*;
import android.text.Html;
import android.text.Spanned;
import net.kano.joscar.*;
import net.kano.joscar.flapcmd.SnacCommand;
import net.kano.joscar.snac.SnacRequestAdapter;
import net.kano.joscar.snac.SnacResponseEvent;
import net.kano.joscar.snaccmd.CertificateInfo;
import net.kano.joscar.snaccmd.DirInfo;
import net.kano.joscar.snaccmd.FullUserInfo;
import net.kano.joscar.snaccmd.icq.MetaShortInfoCmd;
import net.kano.joscar.snaccmd.icq.MetaShortInfoRequest;
import net.kano.joustsim.*;
import net.kano.joustsim.oscar.*;
import net.kano.joustsim.oscar.oscar.service.buddy.*;
import net.kano.joustsim.oscar.oscar.service.chatrooms.*;
import net.kano.joustsim.oscar.oscar.service.icbm.*;
import net.kano.joustsim.oscar.oscar.service.info.InfoService;
import net.kano.joustsim.oscar.oscar.service.info.InfoServiceListener;
import net.kano.joustsim.trust.BuddyCertificateInfo;

public class AIMAccount extends Account {
	
	protected DAimAppSession aimSession = null;
	protected AimConnection connection = null;
	protected DAppSession appSession = null;
	private ConnStateListener connStateListener = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	AIMAccount(){
		this.setServerAddress("login.oscar.aol.com");
		this.setServerPort(5190);
		this.setService("oscar");
		aimSession = null;
		connection = null;
		appSession = null;
		connStateListener = new ConnStateListener();
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.setServerAddress("login.oscar.aol.com");
		this.setServerPort(5190);
		this.setService("oscar");
		aimSession = null;
		connection = null;
		appSession = null;
		connStateListener = new ConnStateListener();
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	}
	
	@Override public boolean connect(AccountLoginListener loginListener){
		Screenname name = new Screenname(this.getUserName());
		AimConnectionProperties props = new AimConnectionProperties(name, this.getPassword());
		try {
			File dir = Util.getInstance().activity.getDir("aimconfig", Context.MODE_PRIVATE);
			DAppSession sess = new DAppSession(new File(dir.getAbsolutePath(), ".dolca"));
			sess.setSavePrefsOnExit(true);
			aimSession = (DAimAppSession) sess.openAimSession(name);
			connection = aimSession.openConnection(props);

			connection.addStateListener(connStateListener);
//			connection.addOpenedServiceListener(new OpenedServiceListener() {
//				
//				public void openedServices(AimConnection conn,
//						Collection<? extends Service> services) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//				public void closedServices(AimConnection conn,
//						Collection<? extends Service> services) {
//					// TODO Auto-generated method stub
//					
//				}
//			})
			
			connection.connect();
			this.loginListener = loginListener;
			
		} catch (Exception e) {
			String errorMessage = e.getLocalizedMessage();
			loginListener.loginDidFailedWithError(errorMessage!=null?errorMessage:e.toString());
			return false;
		}
		return true;
	}
	
	public Friend getFriendFromBuddy(Screenname buddy, boolean needFullInfo) {
		Friend friend = new Friend(buddy.getFormatted());
		friend.setAccount(AIMAccount.this);
//		friend.setName(buddy.getFormatted());
//		if (needFullInfo)
//			friendApplyNewStatusFromContact(friend, contact);
		return friend;
	}
	
	@Override
	public void sendMessage(final FriendMessage message){
		
		HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
		Friend friend = friends.get(message.getTo());
		Screenname buddy = new Screenname(friend.getUserName());		
		Conversation c = (Conversation)friend.getUserInfo();
		if (c == null) {
			c = connection.getIcbmService().getImConversation(buddy);
			friend.setUserInfo(c);
		}
		Message oscarMessage = new Message() {
			
			public boolean isAutoResponse() {
				return false;
			}
			
			public String getMessageBody() {
				return message.getMessage();
			}
		};
		c.sendMessage(oscarMessage);
		Friend recipient = friends.get(message.getTo());
		accountListener.didReceiveMessageForFriend(message, recipient);
	}
	
	@Override public void disconnect(){
		if (appSession != null) {
			appSession.closeAimSession(aimSession.getScreenname());
			accountListener.accountDidDisconnect(this);
		}
	}

	private class ConnStateListener implements StateListener {


		
		public void applyInfoToFriend(final Friend friend, FullUserInfo info){

			friend.setIsAvailable(true);
			long status = info.getIcqStatus();
			if ((status & FullUserInfo.ICQSTATUS_AWAY)==1){
				friend.setStatus(Friend.Status.AWAY);
			} else if (((status & FullUserInfo.ICQSTATUS_DND)==1)){
				friend.setStatus(Friend.Status.DND);
			} else if ((status & FullUserInfo.ICQSTATUS_OCCUPIED)==1){
				friend.setStatus(Friend.Status.DND);
			} else if ((status & FullUserInfo.ICQSTATUS_NA)==1){
				friend.setStatus(Friend.Status.NA);
			} else if ((status & FullUserInfo.ICQSTATUS_DEFAULT)==1){
				friend.setStatus(Friend.Status.AVAILABLE);
			} else if ((status & FullUserInfo.ICQSTATUS_FFC) == 1) {
				friend.setStatus(Friend.Status.AVAILABLE);
			}
			Screenname buddy = new Screenname(friend.getUserName());
			BuddyInfo buddyInfo = connection.getBuddyInfoManager().getBuddyInfo(buddy);
			if (buddyInfo!=null) {
				friend.setStatusMessage(buddyInfo.getStatusMessage());
			}
			//connection.getInfoService().requestDirectoryInfo(buddy);
//			connection.getInfoService().requestUserProfile(buddy);
			
//			 int requestId = nextSeqId();
//	         int buddyUIN = Integer.parseInt(friend.getUserName());
//	         int ownerUIN = Integer.parseInt(getUserName());
//	         MetaShortInfoRequest req = new MetaShortInfoRequest(ownerUIN,requestId, buddyUIN);
//
//	         ShortInfoResponseRetriever responseRetriever = new ShortInfoResponseRetriever();
//	         connection.getInfoService().getOscarConnection().sendSnacRequest(req, responseRetriever);
//	         
//	         
//	         synchronized(responseRetriever)
//	         {
//	             try{
//	                 responseRetriever.wait(30000);
//	             }
//	             catch (InterruptedException ex)
//	             {
//	             }
//	         }
//	         friend.setName(responseRetriever.nickname);

//			int requestId = nextSeqId();
	         long buddyUIN = Long.parseLong(friend.getUserName());
	         long ownerUIN = Long.parseLong(getUserName());
	         MetaShortInfoRequest req = new MetaShortInfoRequest(ownerUIN,2, buddyUIN);

	         connection.getInfoService().getOscarConnection().sendSnacRequest(req, new SnacRequestAdapter() {
	        	 public void handleResponse(SnacResponseEvent e) {
	                 SnacCommand snac = e.getSnacCommand();
	                 if (snac instanceof MetaShortInfoCmd)
	                 {
	                     MetaShortInfoCmd infoSnac = (MetaShortInfoCmd)snac;
	                     String nick = infoSnac.getNickname();
	                     String firstName = infoSnac.getFirstName();
	                     String lastName = infoSnac.getLastName();
	                     String fullname = firstName + " " + lastName;
	                     if (fullname.length()<2)
	                    	 fullname = nick;
	                     friend.setName(fullname);
	                 }
	             }
			});

			
		}
		
		
		public void applyInfoToFriendFromBuddyInfo(final Friend friend, BuddyInfo info) {
			friend.setIsAvailable(true);
			long status = info.getIcqStatus();
			if ((status & FullUserInfo.ICQSTATUS_AWAY)==1){
				friend.setStatus(Friend.Status.AWAY);
			} else if (((status & FullUserInfo.ICQSTATUS_DND)==1)){
				friend.setStatus(Friend.Status.DND);
			} else if ((status & FullUserInfo.ICQSTATUS_OCCUPIED)==1){
				friend.setStatus(Friend.Status.DND);
			} else if ((status & FullUserInfo.ICQSTATUS_NA)==1){
				friend.setStatus(Friend.Status.NA);
			} else if ((status & FullUserInfo.ICQSTATUS_DEFAULT)==1){
				friend.setStatus(Friend.Status.AVAILABLE);
			} else if ((status & FullUserInfo.ICQSTATUS_FFC) == 1) {
				friend.setStatus(Friend.Status.AVAILABLE);
			}
			
			friend.setStatusMessage(info.getStatusMessage());
//			Screenname buddy = new Screenname(friend.getUserName());
//			connection.getInfoService().requestDirectoryInfo(buddy);
//			connection.getInfoService().requestUserProfile(buddy);
			
			
//			 int requestId = nextSeqId();
	         long buddyUIN = Long.parseLong(friend.getUserName());
	         long ownerUIN = Long.parseLong(getUserName());
	         MetaShortInfoRequest req = new MetaShortInfoRequest(ownerUIN,2, buddyUIN);

//	         ShortInfoResponseRetriever responseRetriever = new ShortInfoResponseRetriever();
	         connection.getInfoService().getOscarConnection().sendSnacRequest(req, new SnacRequestAdapter() {
	        	 public void handleResponse(SnacResponseEvent e) {
	                 SnacCommand snac = e.getSnacCommand();
	                 if (snac instanceof MetaShortInfoCmd)
	                 {
	                     MetaShortInfoCmd infoSnac = (MetaShortInfoCmd)snac;
	                     String nick = infoSnac.getNickname();
	                     String firstName = infoSnac.getFirstName();
	                     String lastName = infoSnac.getLastName();
	                     String fullname = firstName + " " + lastName;
	                     if (fullname.length()<2)
	                    	 fullname = nick;
	                     friend.setName(fullname);
	                 }
	             }
			});
	         
	         
//	         synchronized(responseRetriever)
//	         {
//	             try{
//	                 responseRetriever.wait(30000);
//	             }
//	             catch (InterruptedException ex)
//	             {
//	             }
//	         }
	         
		}
		
		public void handleStateChange(StateEvent event) {
			AimConnection conn = event.getAimConnection();
			if (aimSession == null || conn != aimSession.getConnection()) {
				return;
			}	
			
			State state = event.getNewState();
			
			if (state == State.FAILED) {
				if (AIMAccount.this.loginListener!=null)
				{
					AIMAccount.this.loginListener.loginDidFailedWithError("Failed to connect to '"+AIMAccount.this.getUserName()+"' account");
					AIMAccount.this.loginListener = null;
				}
			}
			
			if (state == State.ONLINE) {
				if (AIMAccount.this.loginListener!=null)
				{
					AIMAccount.this.loginListener.loginDidSucceeded();
					AIMAccount.this.loginListener = null;
				}

				
				isOnline = true;
				IcbmService icbmservice = conn.getIcbmService();
				icbmservice.addIcbmListener(new IcbmListener() {

					public void buddyInfoUpdated(IcbmService service,
							Screenname buddy, IcbmBuddyInfo info) {						
					}

					public void newConversation(IcbmService service,
							Conversation conv) {
						conv.addConversationListener(new ConversationListener() {
							
							public void sentOtherEvent(Conversation conversation,
									ConversationEventInfo event) {
								// TODO Auto-generated method stub
								
							}
							
							public void sentMessage(Conversation c, MessageInfo minfo) {
								// TODO Auto-generated method stub
								
							}
							
							public void gotOtherEvent(Conversation conversation,
									ConversationEventInfo event) {
								// TODO Auto-generated method stub
								
							}
							
							public void gotMessage(Conversation c, MessageInfo minfo) {
								Friend friend = getFriendFromBuddy(minfo.getFrom(), false);
								HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
								Friend existingFriend = friends.get(friend.getUniqueDescriptior());
								existingFriend.setUserInfo(c);
								Spanned messageText = Html.fromHtml(minfo.getMessage().getMessageBody());
				    			FriendMessage friendMessage = new FriendMessage(existingFriend.getUniqueDescriptior(), AIMAccount.this.getUserName(), messageText.toString(), false);
				    			accountListener.didReceiveMessageForFriend(friendMessage, existingFriend);
							}
							
							public void conversationOpened(Conversation c) {
								// TODO Auto-generated method stub
								
							}
							
							public void conversationClosed(Conversation c) {
								Friend friend = getFriendFromBuddy(c.getBuddy(), false);
								HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
								Friend existingFriend = friends.get(friend.getUniqueDescriptior());
								existingFriend.setUserInfo(null);
							}
							
							public void canSendMessageChanged(Conversation c, boolean canSend) {
								// TODO Auto-generated method stub
								
							}
						});
					}

					public void sendAutomaticallyFailed(IcbmService service,
							Message message, Set<Conversation> triedConversations) {
						// TODO Auto-generated method stub
						
					}
				});
				
				
				connection.getBuddyInfoManager().addGlobalBuddyInfoListener(new GlobalBuddyInfoListener() {
					
					@Override
					public void receivedStatusUpdate(BuddyInfoManager manager,
							Screenname buddy, BuddyInfo info) {
					}
					
					@Override
					public void newBuddyInfo(BuddyInfoManager manager, Screenname buddy, BuddyInfo info) {
						Friend friend = getFriendFromBuddy(buddy, false);
						HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
						Friend existingFriend = friends.get(friend.getUniqueDescriptior());
						if (existingFriend!=null) {
							ByteBlock iconData = info.getIconData();
							if (iconData!=null) {
								Bitmap bitmap = BitmapFactory.decodeByteArray(iconData.toByteArray(), 0, iconData.getLength());
								if (bitmap!=null) {
									existingFriend.setAvatar(bitmap);
								}
							}
							applyInfoToFriendFromBuddyInfo(existingFriend, info);
							accountListener.friendStatusDidChange(existingFriend);
						}
					}
					
					@Override
					public void buddyInfoChanged(BuddyInfoManager manager, Screenname buddy,
							BuddyInfo info, PropertyChangeEvent event) {
						Friend friend = getFriendFromBuddy(buddy, false);
						HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
						Friend existingFriend = friends.get(friend.getUniqueDescriptior());
						
						if (existingFriend!=null) {
							ByteBlock iconData = info.getIconData();
							if (iconData!=null) {
								Bitmap bitmap = BitmapFactory.decodeByteArray(iconData.toByteArray(), 0, iconData.getLength());
								if (bitmap!=null) {
									existingFriend.setAvatar(bitmap);
								}
							}
							applyInfoToFriendFromBuddyInfo(existingFriend, info);
							accountListener.friendStatusDidChange(existingFriend);
						}
					}
				});
				
				connection.getInfoService().addInfoListener(new InfoServiceListener() {
					
					@Override
					public void handleUserProfile(InfoService service, Screenname buddy,
							String userInfo) {
						Friend friend = getFriendFromBuddy(buddy, false);
						HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
						Friend existingFriend = friends.get(friend.getUniqueDescriptior());
						existingFriend.setName(userInfo);
					}
					
					@Override
					public void handleInvalidCertificates(InfoService service,
							Screenname buddy, CertificateInfo origCertInfo, Throwable ex) {
					}
					
					@Override
					public void handleDirectoryInfo(InfoService service, Screenname buddy,
							DirInfo dirInfo) {
						Friend friend = getFriendFromBuddy(buddy, false);
						HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
						Friend existingFriend = friends.get(friend.getUniqueDescriptior());
						
						if (dirInfo!=null) {
							String firstName = dirInfo.getFirstname();
							String nick = dirInfo.getNickname();
							String lastName = dirInfo.getLastname();
							String fullName = firstName!=null?firstName:""+lastName!=null?lastName:"";
							if (fullName!=null && fullName.length()>0) {
								existingFriend.setName(fullName);
							} else if (nick!=null && nick.length()>0) {
								existingFriend.setName(nick);
							}
						}

					}
					
					@Override
					public void handleCertificateInfo(InfoService service, Screenname buddy,
							BuddyCertificateInfo certInfo) {
					}
					
					@Override
					public void handleAwayMessage(InfoService service, Screenname buddy,
							String awayMessage) {
					}
				});

				conn.getChatRoomManager().addListener(new ChatRoomManagerListener() {

					public void handleInvitation(ChatRoomManager chatRoomManager,
							ChatInvitation ourInvitation) {
					}
				});


				BuddyService buddyService = conn.getBuddyService();
				buddyService.addBuddyListener(new BuddyServiceListener(){

					public void buddyOffline(BuddyService service, Screenname buddy) {
						synchronized (AIMAccount.this) {
							Friend friend = getFriendFromBuddy(buddy, false);
							friend.setIsAvailable(false);
							HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
							Friend existingFriend = friends.get(friend.getUniqueDescriptior());
							if (existingFriend == null) {
								existingFriend = friend;
								accountListener.didAddFriend(existingFriend);
							} else {
								accountListener.friendStatusDidChange(existingFriend);
							}
						}
					}

					public void gotBuddyStatus(BuddyService service, Screenname buddy, FullUserInfo info) {
						synchronized (AIMAccount.this) {
							Friend friend = getFriendFromBuddy(buddy, false);
							HashMap<String, Friend> friends = AccountManager.getInstance().getFriends();
							Friend existingFriend = friends.get(friend.getUniqueDescriptior());
							if (existingFriend == null) {
								existingFriend = friend;
								accountListener.didAddFriend(existingFriend);
							} 
							applyInfoToFriend(existingFriend, info);
							accountListener.friendStatusDidChange(existingFriend);
						}
					}
				});
			} else {
				disconnect();
			}
		}

	}
}
