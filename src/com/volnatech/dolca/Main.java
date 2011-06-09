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

import com.volnatech.dolca.MessengerService.LocalBinder;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;
import android.view.View.OnKeyListener;
import android.widget.*;

public class Main extends Activity {
	
	private static MessengerService messengerService = null;
	public static Friend selectedFriend = null;
	public static boolean mDualPane;
	boolean mIsBound;
	private static TitlesFragment.TitlesAdapter titlesAdapter = null;
	public final static String KEY_ASK_FOR_EXIT = "KEY_ASK_FOR_EXIT";
//	private AdView adView = null;
	public static Activity self = null;
//	public static final boolean IS_HONEYCOMB = true;
	
	final static int DIALOG_QUIT_ID = 1;
	
	public static MessengerService getMessengerService() {
//		if (messengerService == null) {
//			Intent intent = new Intent(self, MessengerService.class);
//			self.doBindService(intent);
//		}
			
		return messengerService;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.getInstance().activity = this;
		setContentView(R.layout.main);
	    selectedFriend = null;
		HistoryManager.getInstance().loadHistory(this);
		Intent intent = new Intent(Main.this, MessengerService.class);
		doBindService(intent);
		
//		adView = (AdView)findViewById(R.id.adView);

//		LinearLayout layout = (LinearLayout)findViewById(R.id.main_layout_for_ad);
//	    adView = new AdView(this, AdSize.BANNER, "a14da73942e8db8");
//	    // Lookup your LinearLayout assuming it’s been given
//	    // the attribute android:id="@+id/mainLayout"
//	    // Add the adView to it
//	    layout.addView(adView);
	    
//	    AdRequest request = new AdRequest();
//	    request.setTesting(true);
//	    request.setTesting(testing)

	    // Initiate a generic request to load it with an ad
///	    adView.loadAd(request);
	}

	@Override protected void onStart() {
		super.onStart();
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (messengerService!=null)
			messengerService.setAplicationOnScreen(true);

//		AdRequest request = new AdRequest();
//	    request.setTesting(false);
//	    adView.loadAd(request);
	}
	
//	@Override protected void on {
////	    String action = intent.getAction();
////	    if (action.equals(Intent.ACTION_MAIN)) {
//	    	Bundle bundle = intent.getExtras(); 
//	        String guid = bundle.getString("show_friend");
//	        if (guid!=null){
//	        	
//	        }
////	    }
//	}

	@Override protected void onResume(){
		super.onResume();

//		Bundle bundle = this.getIntent().getExtras();
//		if (bundle!=null)
//		{
//			String guid = bundle.getString("show_friend");
//			if (guid!=null && messengerService!=null){
//				bundle.remove("show_friend");
//				Friend friend = messengerService.getFriends().get(guid);
//				if (friend != null)
//					showDetails(this, friend);
//			}
//		}
	}

	@Override protected void onPause(){
		super.onPause();
	}

	@Override protected void onStop(){
		if (messengerService!=null)
			messengerService.setAplicationOnScreen(false);
//		adView.stopLoading();
		super.onStop();
	}
	
	@Override protected void onDestroy(){
		super.onDestroy();
		selectedFriend = null;
		AccountManager.getInstance().saveAccounts(this);
		HistoryManager.getInstance().saveHistory(this);
		doUnbindService();
	}
	
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_QUIT_ID:
	    	AlertDialog.Builder builder;
	    	LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
	    	View layout = inflater.inflate(R.layout.quit_dialog,
	    			(ViewGroup) findViewById(R.id.layout_root));
	    	TextView text = (TextView) layout.findViewById(R.id.text);
	    	text.setText(getText(R.string.quit_app_hint));
	    	ImageView image = (ImageView) layout.findViewById(R.id.image);
	    	image.setImageResource(R.drawable.icon);
	    	
	    	CheckBox remember_choice_box = (CheckBox)layout.findViewById(R.id.remember_choice_box);
	    	remember_choice_box.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					SharedPreferences settings = Main.this.getSharedPreferences("HintsPreferences", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean(KEY_ASK_FOR_EXIT, isChecked);
					editor.commit();
				}
			});

	    	builder = new AlertDialog.Builder(this);
	    	builder.setView(layout);
	    	builder.setTitle(getText(R.string.quit_app_hint_title));
	    	builder.setCancelable(false);
	    	builder.setPositiveButton(getText(R.string.yes), new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {
	    			Main.this.finish();
	    		}
	    	});
	    	builder.setNegativeButton(getText(R.string.no), new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {
	    			dialog.cancel();
	    		}
	    	});
	    	dialog = builder.create();

	    	
	        break;
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
	@Override public void onBackPressed (){		
		SharedPreferences settings = getSharedPreferences("HintsPreferences", Context.MODE_PRIVATE);
		boolean ignoreHint = settings.getBoolean(KEY_ASK_FOR_EXIT, false);
		if (ignoreHint == false)
			showDialog(DIALOG_QUIT_ID);
		else {
			Main.this.finish();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, AccountListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		//        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
	}
	
	public class FriendComparator implements Comparator<Friend>{
			public int compare(Friend object1, Friend object2) {
				int ret = 0;
				if (object1.getIsAvailable() && !object2.getIsAvailable()) return -1;
				if (!object1.getIsAvailable() && object2.getIsAvailable()) return 1;
				if (object1.getIsAvailable() == object2.getIsAvailable()){
					ret = object1.getStatus().compareTo(object2.getStatus());
					if (ret == 0)
					{
						String name1 = object1.getName();
						String name2 = object2.getName();
						if (name1 == null) name1 = object1.getUserName();
						if (name2 == null) name2 = object2.getUserName();
						return name1.compareToIgnoreCase(name2);		
					}
					return ret;
				}
				return ret;
			}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem actionItem = menu.add("Add account");
		actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		actionItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(Main.this, AddAccountActivity.class);
				startActivity(intent);
				return true;
			}

		});
		//actionItem.setIcon(android.R.drawable.ic_menu_share);
		return true;
	}


	private ServiceConnection mConnection = new ServiceConnection() {
		
		public void onServiceConnected(ComponentName className, IBinder service) {
//			mService = new Messenger(service);
			LocalBinder binder = (MessengerService.LocalBinder)service;
			messengerService = (MessengerService)binder.getService();
			messengerService.setMessageServiceListener(new MessengerService.MessengerServiceListener() {
				
				public void friendStatusDidChange(Friend friend) {
					Main.this.runOnUiThread(new Runnable() {
						
						public void run() {
							titlesAdapter.sort(new FriendComparator());
							titlesAdapter.notifyDataSetChanged();
						}
					});

//					int position = titlesAdapter.getPosition(friend);
//					ArrayList<Friend> items = titlesAdapter.getItems();
//					Collection.sort(titlesAdapter);
				}
				
				public void accountDidLogin(final Account account) {
					Main.this.runOnUiThread(new Runnable() {
						
						public void run() {
							Toast.makeText(Main.this, account.getUserName()+" logged in",
									Toast.LENGTH_SHORT).show();
						}
					});
				}

				public void messengerServiceDidInitialized() {
					HashMap<String, Account> accounts = messengerService.getAccounts();
					boolean isAnyAccountConnected = false;
					for (Account account : accounts.values()){
						if (account.isAutoConnect()) {
							isAnyAccountConnected = true;
							break;
						}
					}
					
					if (accounts.size()==0) {
						Intent intent = new Intent(Main.this, AddAccountActivity.class);
						Main.this.startActivity(intent);
					} else if (!isAnyAccountConnected) {
						Intent intent = new Intent(Main.this, AccountListActivity.class);
						Main.this.startActivity(intent);
					}
				}

				public void didAddFriend(final Friend friend) {
					Main.this.runOnUiThread(new Runnable() {
						public void run() {
							titlesAdapter.add(friend);
							titlesAdapter.sort(new FriendComparator());
							titlesAdapter.notifyDataSetChanged();
						}
					});
				}

				public void friendAvatarDidChange(Friend friend) {
					Main.this.runOnUiThread(new Runnable() {
						public void run() {
							titlesAdapter.notifyDataSetChanged();
						}
					});
				}

				public void didReceiveMessageForFriend(final FriendMessage message, final Friend friend) {
					if (selectedFriend == null || selectedFriend != friend){
						Main.this.runOnUiThread(new Runnable() {
							
							public void run() {
								friend.setUnreadMessagesCount(friend.getUnreadMessagesCount()+1);
								titlesAdapter.notifyDataSetChanged();
								String name = friend.getName();
								if (name == null) {
									name = friend.getUserName();
								}
								Toast.makeText(Main.this,  name+": "+message.getMessage(),
										Toast.LENGTH_SHORT).show();
							}
						});
					}
					HistoryManager.getInstance().addMessageToFriendsHistory(message, friend);					
				}

				public void accountDidDisconnect(Account account) {
					Main.this.runOnUiThread(new Runnable() {
						
						public void run() {
							titlesAdapter.setNotifyOnChange(false);
							titlesAdapter.clear();
							titlesAdapter.addAll(getMessengerService().getFriends().values());
							titlesAdapter.sort(new FriendComparator());
							titlesAdapter.notifyDataSetChanged();
							titlesAdapter.setNotifyOnChange(true);
						}
					});
				}

				public void reportErrorString(final String error) {
					Main.this.runOnUiThread(new Runnable() {
						
						public void run() {
							Toast.makeText(Main.this, error, Toast.LENGTH_SHORT).show();
						}
					});
				}
			});
			
			Toast.makeText(Main.this, R.string.remote_service_connected,
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			messengerService = null;
			Toast.makeText(Main.this, R.string.remote_service_disconnected,
					Toast.LENGTH_SHORT).show();
			
			
//			NotificationManager mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//			Notification notification = new Notification(R.drawable.icon, "onServiceDisconnected", System.currentTimeMillis());
//			Intent notificationIntent = new Intent();
//			notificationIntent.setAction(Intent.ACTION_MAIN);
//			notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//			PendingIntent contentIntent = PendingIntent.getActivity(Main.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//			notification.setLatestEventInfo(Main.this, "onServiceDisconnected", "onServiceDisconnected", contentIntent);
//			notification.flags = Notification.FLAG_AUTO_CANCEL|Notification.FLAG_SHOW_LIGHTS;
//			mNM.notify("onServiceDisconnected".hashCode(), notification);
//			
			
			
		}
	};

	void doBindService(Intent intent) {
		if (mIsBound == false)
		{
//			Intent intent = new Intent(Main.this, MessengerService.class);
			mIsBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}
	
	public static void showDetails(Activity activity, Friend friend){
		if (mDualPane) {
//			getListView().setItemChecked(mCurCheckPosition, true);
			DetailsFragment df = DetailsFragment.newInstance(friend); 
			FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
			ft.replace(R.id.details, df);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		}
		else
		{
			Intent intent = new Intent();
			intent.setClass(activity, DetailsActivity.class);
			intent.putExtra("FriendUID", friend.getUniqueDescriptior());
			activity.startActivity(intent);
		}
	}
	
	public static class TitlesFragment extends ListFragment {
		int mCurCheckPosition = -1;
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			ArrayList<Friend> namesList = new ArrayList<Friend>(); 
			titlesAdapter = new TitlesAdapter(getActivity(), R.layout.friend_list_row, namesList);
			setListAdapter(titlesAdapter);

			View detailsFrame = getActivity().findViewById(R.id.details);
			mDualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

			if (savedInstanceState != null) {
				mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
			}
			if (mDualPane) {
				getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				if (mCurCheckPosition >= 0 )
					showDetails(getActivity(), (Friend)getListAdapter().getItem(mCurCheckPosition));
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("curChoice", mCurCheckPosition);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			NotificationManager mNM = (NotificationManager)getActivity().getSystemService(NOTIFICATION_SERVICE);
			Friend friend = (Friend)getListAdapter().getItem(position);
			String name = friend.getName();
			if (name == null)
				name = friend.getUserName();
			mNM.cancel(name.hashCode());
			friend.setUnreadMessagesCount(0);
			selectedFriend = friend;
			messengerService.setSelectedFriend(selectedFriend);
			titlesAdapter.notifyDataSetChanged();
			showDetails(getActivity(), friend);
		}
		
		public class TitlesAdapter extends ArrayAdapter<Friend> {

			public TitlesAdapter(Context context, int textViewResourceId, ArrayList<Friend> items) {
				super(context, textViewResourceId, items);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					v = vi.inflate(R.layout.friend_list_row, null);
				}

				Friend friend = titlesAdapter.getItem(position);

				if (friend != null) {
					TextView tt = (TextView) v.findViewById(R.id.toptext);
					TextView bt = (TextView) v.findViewById(R.id.bottomtext);
					ImageView badge = (ImageView)v.findViewById(R.id.contact_badge);
					ImageView statusImage = (ImageView)v.findViewById(R.id.contact_presence);
					TextView unread_message_count = (TextView) v.findViewById(R.id.unread_message_count);
					int count = friend.getUnreadMessagesCount();
					if (count>0)
						unread_message_count.setText("("+ count +")");
					else
						unread_message_count.setText("");
					Bitmap contact_picute = friend.getAvatar();
					if (contact_picute!=null)
						badge.setImageBitmap(contact_picute);
					else
						badge.setImageResource(R.drawable.buddy_offline);

					Boolean isOnline = friend.getIsAvailable();
					int imageID = isOnline?R.drawable.online:R.drawable.offline;
					Friend.Status status = friend.getStatus();
					if (isOnline)
					{
						switch (status) {
						case NA: imageID = R.drawable.online; break;
						case AVAILABLE: imageID = R.drawable.online; break;
						case AWAY : imageID = R.drawable.away; break;
						case CHAT : imageID = R.drawable.online; break;
						case DND : imageID = R.drawable.dnd; break;
						case XA : imageID = R.drawable.dnd; break;
						}
					}
					
					statusImage.setImageResource(imageID);
					badge.setScaleType(ImageView.ScaleType.CENTER_CROP);
					if (tt != null) {
						String name = friend.getName();
						if (name == null){
							name = friend.getUserName();
							if (name.contains("@")){
								name = name.split("@")[0];
							}
						}
						tt.setText(name);                           
					}
					if(bt != null){
						String statusMessage = friend.getStatusMessage();
						if (null == statusMessage || statusMessage.length()==0)
							statusMessage = friend.getUserName();
						bt.setText(statusMessage);
					}
				}
				return v;
			}
		}
		
	}

	public static class DetailsFragment extends Fragment {
		/**
		 * Create a new instance of DetailsFragment, initialized to
		 * show the text at 'index'.
		 */
		private View chatView = null;
		private String uid = null;
		public static DetailsFragment newInstance(Friend friend) {
			DetailsFragment f = new DetailsFragment();
	        Bundle args = new Bundle();
	        args.putString("FriendUID", friend.getUniqueDescriptior());
	        f.setArguments(args);

			return f;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if (container == null) {
				return null;
			}
			if (chatView==null)
			{
				uid = getArguments().getString("FriendUID");
				if (uid!=null){
					LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					chatView = vi.inflate(R.layout.chat_view, null);
//					chatView.setBackgroundResource(R.drawable.grey_gradient);
//					chatView.setBackgroundColor(0xFF888888);
					final ListView lv = (ListView)chatView.findViewById(R.id.chat_list_view);
					final EditText message_text = (EditText)chatView.findViewById(R.id.message_edit_view);
					lv.setDividerHeight(0);
					HistoryManager historyManager = HistoryManager.getInstance();
					ArrayList<FriendMessage> items = null;
					final HashMap<String, Friend> friendList = getMessengerService().getFriends();
					items = historyManager.getHistoryForFriend(friendList.get(uid));
					final MessagesAdapter adapter = new MessagesAdapter(getActivity(), R.id.chat_list_view, items);
					lv.setAdapter(adapter);
					if (adapter.getCount()>0) {
						lv.setSelection(adapter.getCount()-1);
					}
					historyManager.registerListenerForUID(uid, new HistoryManager.MessageArriveListener() {
						
						@Override
						public void didReceiveMessage(final FriendMessage message, final Friend friend) {
							getActivity().runOnUiThread(new Runnable() {
								public void run() {
									adapter.notifyDataSetChanged();
									lv.setSelection(adapter.getCount()-1);
								}
							});
						}
					});
					message_text.setOnKeyListener(new OnKeyListener() {
						
						public boolean onKey(View v, int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
								return true;
							}
							if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
									final HashMap<String, Friend> friendList = getMessengerService().getFriends();
									String message_body = message_text.getText().toString();
									Friend friend = friendList.get(uid);
									final Account account = friend.getAccount();
									final FriendMessage message = new FriendMessage(account.getUserName(), friend.getUniqueDescriptior(), message_body, true);
									new Thread(new Runnable() {
										@Override
										public void run() {
											account.sendMessage(message);
										}
									}).start();
									
									message_text.setText("");

								return true;
							}
							return false;
						}
					});
				}
			}
			return chatView;
		}

		@Override
		public void onDestroy() {
			if (uid!=null)
				HistoryManager.getInstance().unregisterListenerForUID(uid);
			super.onDestroy();
		}
		
		public void didReceiveMessage(FriendMessage message, Friend friend){
			
		}

		public class MessagesAdapter extends ArrayAdapter<FriendMessage> {

			public MessagesAdapter(Context context, int textViewResourceId, ArrayList<FriendMessage> items) {
				super(context, textViewResourceId, items);
			}

			@Override
			public int getItemViewType (int position){
				FriendMessage message = getItem(position);
				return message.isOutgoing()?1:0;
			}

			@Override
			public int getViewTypeCount (){
				return 1;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				FriendMessage message = getItem(position);
				if (v == null) {
					int rID = R.layout.message_row;
					LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//					if (message.isOutgoing()) 
//					{
//						rID = R.layout.message_right_row;
//					}
					v = vi.inflate(rID, null);
				}
				ImageView badge = (ImageView)v.findViewById(R.id.contact_badge);
				TextView text = (TextView)v.findViewById(R.id.message_text);
				TextView user_name = (TextView)v.findViewById(R.id.user_name);
				text.setText(message.getMessage());
				Friend friend = null;
				if (message.isOutgoing()) {
					friend = getMessengerService().getFriends().get(message.getTo()).getAccount().getSelfAsFriend();
				} else {
					friend = getMessengerService().getFriends().get(message.getFrom());
				}
				String name = friend.getName(); 
				if (name == null)
					name = friend.getUserName();
				user_name.setText(name);
				int resID = message.isOutgoing()?R.drawable.bckg1:R.drawable.green_gradient;
				user_name.setBackgroundResource(resID);
				Bitmap bitmap = null;
				if (friend!=null)
					bitmap = friend.getAvatar();
				if (bitmap!=null)
					badge.setImageBitmap(bitmap);
				else badge.setImageResource(R.drawable.buddy_offline);
				return v;
			}
		}
	}

	public static class DetailsActivity extends Activity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (getResources().getConfiguration().orientation
					== Configuration.ORIENTATION_LANDSCAPE) {
				// If the screen is now in landscape mode, we can show the
				// dialog in-line with the list so we don't need this activity.
				finish();
				return;
			}

			if (savedInstanceState == null) {
				// During initial setup, plug in the details fragment.
				DetailsFragment details = new DetailsFragment();
				details.setArguments(getIntent().getExtras());
				getFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
			}
		}
	}
}