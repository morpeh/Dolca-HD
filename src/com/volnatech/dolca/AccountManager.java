package com.volnatech.dolca;

import java.util.*;

import android.content.*;

public class AccountManager {
	private static volatile AccountManager instance = null;
	private HashMap<String, Account> accounts = null;
	private HashMap<String, Friend> friends = null;

	private AccountManagerListener accountManagerListener = null;

	public interface AccountManagerListener extends Account.AccountListener {
		public abstract void reportErrorString(String error);
	}

	private AccountManager(){
		accounts = new HashMap<String, Account>();
		friends = new HashMap<String, Friend>();
	}

	public HashMap<String, Friend> getFriends(){
		return friends;
	}

	public static AccountManager getInstance() {
		if (instance == null)
			synchronized (AccountManager.class) {
				if (instance == null)
					instance = new AccountManager();
			}
		return instance;
	}

	public void removeAccount(Account account){
		account.disconnect();
		accounts.remove(account.getUserName());
	}

	public void addAccount(Account account){
		accounts.put(account.getUserName(), account);
		account.setAccountListener(new Account.AccountListener() {

			public void accountDidLogin(Account account) {
				accountManagerListener.accountDidLogin(account);
			}

			public void friendStatusDidChange(Friend friend) {
				accountManagerListener.friendStatusDidChange(friend);
			}

			public void didAddFriend(Friend friend) {
				friends.put(friend.getUniqueDescriptior(), friend);
				accountManagerListener.didAddFriend(friend);
			}

			public void friendAvatarDidChange(Friend friend) {
				accountManagerListener.friendAvatarDidChange(friend);
			}

			public void didReceiveMessageForFriend(FriendMessage message,
					Friend friend) {
				accountManagerListener.didReceiveMessageForFriend(message, friend);
			}

			public void accountDidDisconnect(Account account) {
				ArrayList<Friend> toDelete = new ArrayList<Friend>();
				for (Friend friend : friends.values()){
					if (friend.getAccount() == account) {
						toDelete.add(friend);
					}
				}
				for (Friend friend : toDelete){
					friends.remove(friend.getUniqueDescriptior());
				}
				toDelete.clear();
				accountManagerListener.accountDidDisconnect(account);
			}
		});
	}

	public void setAccountManagerListener(AccountManagerListener listener){
		this.accountManagerListener = listener;
	}

	public HashMap<String, Account> getAccounts(){
		return accounts;
	}

	public void saveAccounts(Context context){
		String accountsString = "";
		for (Account account : accounts.values()){
			if (accountsString.length() > 0) {
				accountsString += "__account_divider__";
			}
			accountsString += Util.objectToString(account);
		}

		SharedPreferences settings = context.getSharedPreferences("AccountPreferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("accounts", accountsString);
		editor.commit();
	}

	public void loadAccounts(final Context context){
		SharedPreferences settings = context.getSharedPreferences("AccountPreferences", Context.MODE_PRIVATE);
		String accountsString = settings.getString("accounts", "");
		if (accountsString.length()>0 && accountsString!="null")
		{
			String[] accountStringArray = accountsString.split("__account_divider__");
			for (int i=0; i<accountStringArray.length; i++){
				final Account account = (Account)Util.stringToObject(accountStringArray[i]);
				if (account !=null)
				{
					this.addAccount(account);

					new Thread(new Runnable() {

						public void run() {
							if (account.isAutoConnect()) {
								account.connect(new Account.AccountLoginListener() {

									@Override
									public void loginDidSucceeded() {
									}

									@Override
									public void loginDidFailedWithError(final String error) {
										accountManagerListener.reportErrorString(error);
									}
								});
							}	
						}
					}).start();
				}
			}
		}
	}

	public void destroy(){
		for (Account account : accounts.values()){
			account.disconnect();
		}
		accounts.clear();
		accounts = null;
		instance = null;
	}
}
