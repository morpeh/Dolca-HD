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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.*;

public class MessengerService extends Service {

	@SuppressWarnings("rawtypes")
	private static final Class[] mStartForegroundSignature = new Class[] {
		int.class, Notification.class};
	@SuppressWarnings("rawtypes")
	private static final Class[] mStopForegroundSignature = new Class[] {
		boolean.class};

	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];

	NotificationManager mNM;
	private boolean serviceInitialized = false;
	private IBinder mBinder = new LocalBinder();
	private boolean isAplicationOnScreen = true;
	private Friend selectedFriend = null;
	private final static int serviceID = 0xCAFEBABE;

	private MessengerServiceListener messengerServiceListener = null;

	public interface MessengerServiceListener extends AccountManager.AccountManagerListener {
		public void messengerServiceDidInitialized();
	}

	void startForegroundCompat(int id, Notification notification) {
		// If we have the new startForeground API, then use it.
		if (mStartForeground != null) {
			mStartForegroundArgs[0] = Integer.valueOf(id);
			mStartForegroundArgs[1] = notification;
			try {
				mStartForeground.invoke(this, mStartForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w("MyApp", "Unable to invoke startForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w("MyApp", "Unable to invoke startForeground", e);
			}
			return;
		}

		// Fall back on the old API.
		//	        setForeground(true);
		//	        mNM.notify(id, notification);
	}


	void stopForegroundCompat(int id) {
		// If we have the new stopForeground API, then use it.
		if (mStopForeground != null) {
			mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				mStopForeground.invoke(this, mStopForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w("MyApp", "Unable to invoke stopForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w("MyApp", "Unable to invoke stopForeground", e);
			}
			return;
		}

		// Fall back on the old API.  Note to cancel BEFORE changing the
		// foreground state, since we could be killed at that point.
		//	        mNM.cancel(id);
		//	        setForeground(false);
	}


	@Override
	public void onCreate() {
		//		super.onCreate();

		try {
			mStartForeground = getClass().getMethod("startForeground",
					mStartForegroundSignature);
			mStopForeground = getClass().getMethod("stopForeground",
					mStopForegroundSignature);
		} catch (NoSuchMethodException e) {
			// Running on an older platform.
			mStartForeground = mStopForeground = null;
		}

		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE); //ACCOUNT_SERVICE, try later TODO

		AccountManager.getInstance().setAccountManagerListener(new AccountManager.AccountManagerListener() {

			public void accountDidLogin(Account account) {
				messengerServiceListener.accountDidLogin(account);
			}

			public void friendStatusDidChange(Friend friend) {
				messengerServiceListener.friendStatusDidChange(friend);
			}

			public void didAddFriend(Friend friend) {
				messengerServiceListener.didAddFriend(friend);
			}

			public void friendAvatarDidChange(Friend friend) {
				messengerServiceListener.friendAvatarDidChange(friend);
			}

			public void didReceiveMessageForFriend(FriendMessage message, Friend friend) {
				messengerServiceListener.didReceiveMessageForFriend(message, friend);
				if (MessengerService.this.getSelectedFriend() != friend ||  MessengerService.this.isAplicationOnScreen==false) {					
					String name = friend.getName();
					if (name == null)
						name = friend.getUserName();
					showNotification(name, message.getMessage(), "show_friend", friend.getUniqueDescriptior());
				}
			}

			public void accountDidDisconnect(Account account) {
				messengerServiceListener.accountDidDisconnect(account);
			}

			public void reportErrorString(String error) {
				messengerServiceListener.reportErrorString(error);
			}

		});
		serviceInitialized = true;

		Notification notification = new Notification(R.drawable.icon, getText(R.string.app_name),
				System.currentTimeMillis());

		Intent notificationIntent = new Intent(this, Main.class);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);


		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);



		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name),
				"Available", contentIntent);


//		startForeground(serviceID, notification);
		startForegroundCompat(serviceID, notification);
		

	}

	//	@Override
	//	public int onStartCommand(Intent intent, int flags, int startId) {
	//		// We want this service to continue running until it is explicitly
	//		// stopped, so return sticky.
	//		return START_STICKY;
	//	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		//		mNM.cancel(R.string.remote_service_started);
		AccountManager.getInstance().destroy();
		// Tell the user we stopped.
		Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		MessengerService getService() {
			return MessengerService.this;
		}
	}

	public HashMap<String, Friend>getFriends(){
		return AccountManager.getInstance().getFriends();
	}

	public void setMessageServiceListener(MessengerServiceListener listener){
		this.messengerServiceListener = listener;
		AccountManager.getInstance().loadAccounts(this);
		if (serviceInitialized) messengerServiceListener.messengerServiceDidInitialized();
	}

	public HashMap<String, Account> getAccounts(){
		return AccountManager.getInstance().getAccounts();
	}

	public void saveAccounts(Context context){
		AccountManager.getInstance().saveAccounts(context);
	}

	public void addAccount(final String userName, final String password, final int accountType, final Account.AccountLoginListener listener){

		new Thread(new Runnable() {
			public void run() {
				Account account = null;
				switch (accountType) {
				case 0: account = new GtalkAccount(); break;
				case 1: account = new MSNAccount(); break;
				case 2: account = new AIMAccount(); break;
				case 3: account = new ICQAccount(); break;
				case 4: account = new FacebookAccount(); break;
				}
				account.setUserName(userName);
				account.setPassword(password);
				final Account faccount = account;	
				account.connect(new Account.AccountLoginListener() {
					@Override
					public void loginDidSucceeded() {
						AccountManager.getInstance().addAccount(faccount);
						listener.loginDidSucceeded();
					}

					@Override
					public void loginDidFailedWithError(String error) {
						listener.loginDidFailedWithError(error);
					}
				});
			}
		}).start();
	}

	private void showNotification(String title, String messageText, String extraName, String extraValue) {
		Notification notification = new Notification(R.drawable.icon, title, System.currentTimeMillis());
		if (this.isAplicationOnScreen() == false) {
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		notification.ledARGB = 0xff00ff00;
		notification.ledOnMS = 500;
		notification.ledOffMS = 1000;
		Intent notificationIntent = new Intent(this, Main.class);
		notificationIntent.putExtra(extraName, extraValue);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, title, messageText, contentIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL|Notification.FLAG_SHOW_LIGHTS;
		mNM.notify(title.hashCode(), notification);
	}

	public void setAplicationOnScreen(boolean isAplicationOnScreen) {
		this.isAplicationOnScreen = isAplicationOnScreen;
	}

	public boolean isAplicationOnScreen() {
		return isAplicationOnScreen;
	}

	public void setSelectedFriend(Friend selectedFriend) {
		this.selectedFriend = selectedFriend;
	}

	public Friend getSelectedFriend() {
		return selectedFriend;
	}

}
