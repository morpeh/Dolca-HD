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

import java.io.IOException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sasl.SASLDigestMD5Mechanism;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FacebookAccount extends XMPPAccount {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	FacebookAccount(){
		this.setServerAddress("chat.facebook.com");
		this.setServerPort(5222);
		this.setService("chat.facebook.com");
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.setServerAddress("chat.facebook.com");
		this.setServerPort(5222);
		this.setService("chat.facebook.com");
	}
	
	@Override
	public void setUserName(String userName) {
		if (userName.contains("@"))
			userName = userName.split("@")[0];
		userName = userName + "@chat.facebook.com"; 
		super.setUserName(userName);
	}
	
	@Override public boolean connect(AccountLoginListener loginListener){
		final XMPPAccount self = this;
		this.loginListener = loginListener;
		ConnectionConfiguration config = new ConnectionConfiguration(self.getServerAddress(), self.getServerPort(), self.getService());
		config.setSecurityMode(SecurityMode.disabled);
		SASLAuthentication.registerSASLMechanism("DIGEST-MD5", SASLDigestMD5Mechanism.class);
		SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 0);
		
		config.setSASLAuthenticationEnabled(true);
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
							FacebookAccount.this.getSelfAsFriend().setAvatar(bitmap);
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
						FacebookAccount.this.setName(name);
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
