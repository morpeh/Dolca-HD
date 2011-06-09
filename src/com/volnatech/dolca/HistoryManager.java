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
