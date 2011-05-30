package com.volnatech.dolca;

import java.util.*;

import android.content.Context;
import android.content.SharedPreferences;
public class HistoryManager {
	private HashMap<String, ArrayList<FriendMessage>> history = null;
	private HashMap<String, MessageArriveListener> detailsListeners = null;
	private static volatile HistoryManager instance = null;
	
	public static abstract class MessageArriveListener {
		public abstract void didReceiveMessage(FriendMessage message, Friend friend);
	}
	
	public static HistoryManager getInstance() {
		if (instance == null)
			synchronized (HistoryManager.class) {
				if (instance == null)
					instance = new HistoryManager();
			}
		return instance;
	}
	
	HistoryManager(){
		history = new HashMap<String, ArrayList<FriendMessage>>();
		detailsListeners = new HashMap<String, MessageArriveListener>();
	}
	
	@SuppressWarnings("unchecked")
	public void loadHistory(Context context) {
		SharedPreferences settings = context.getSharedPreferences("HistoryPreferences", Context.MODE_PRIVATE);
		String historyString = settings.getString("history", "");
		Object object = Util.stringToObject(historyString);
		history = (HashMap<String, ArrayList<FriendMessage>>)object;
		if (history==null) {
			history = new HashMap<String, ArrayList<FriendMessage>>();
		}
	}
	
	public void saveHistory(Context context) {
		String historyString = Util.objectToString(history);

		SharedPreferences settings = context.getSharedPreferences("HistoryPreferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("history", historyString);
		editor.commit();
	}
	
	public void registerListenerForUID(String UID, MessageArriveListener listener){
		MessageArriveListener savedListener = detailsListeners.get(UID);
		if (savedListener!=null) {
			detailsListeners.remove(UID);	
		}
		detailsListeners.put(UID, listener);
	}
	
	public void unregisterListenerForUID(String UID){
		detailsListeners.remove(UID);
	}
	
	public void addMessageToFriendsHistory(FriendMessage message, Friend friend){
			ArrayList<FriendMessage> messageList = getHistoryForFriend(friend);
			messageList.add(message);
			MessageArriveListener listener = detailsListeners.get(friend.getUniqueDescriptior());
			if (listener!=null)
				listener.didReceiveMessage(message, friend);
	}
	
	public ArrayList<FriendMessage> getHistoryForFriend(Friend friend){
		ArrayList<FriendMessage> messageList = history.get(friend.getUniqueDescriptior());
		if (messageList == null){
			messageList = new ArrayList<FriendMessage>();
			history.put(friend.getUniqueDescriptior(), messageList); 
		}
		return messageList;
	}	
}
