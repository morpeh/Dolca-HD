package com.volnatech.dolca;

import java.util.*;

import android.app.*;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class AccountListActivity extends Activity {
	private AccountsAdapter adapter = null; 
	private ProgressDialog connectAccount = null;

	/** Called when the activity is first created. */

	protected void reloadAccountList(){
		ListView listView = (ListView) this.findViewById(R.id.account_list);

		HashMap<String, Account> accounts = Main.getMessengerService().getAccounts();
		ArrayList<Account> accountList = new ArrayList<Account>();
		if (accounts!= null && accounts.values().size()>0)
			accountList.addAll(accounts.values());
		adapter = new AccountsAdapter(this, R.layout.account_row, accountList);
		listView.setAdapter(adapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accounts);
		ActionBar actionBar = this.getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		reloadAccountList();
	}
	
	@Override 
	protected void onDestroy(){
		super.onDestroy();
		AccountManager.getInstance().saveAccounts(this);
		HistoryManager.getInstance().saveHistory(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		//        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
	}

	private class AccountsAdapter extends ArrayAdapter<Account> {

		private ArrayList<Account> items = null;

		public AccountsAdapter(Context context, int textViewResourceId, ArrayList<Account> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.account_row, null);
			}

			final Account account = items.get(position);

			ImageView badge = (ImageView)v.findViewById(R.id.contact_badge);

			int imageRes = 0;
			String className = account.getClass().getCanonicalName();
			if (className == GtalkAccount.class.getCanonicalName())
				imageRes = R.drawable.gtalk;
			if (className == FacebookAccount.class.getCanonicalName())
				imageRes = R.drawable.facebook;
			if (className == MSNAccount.class.getCanonicalName())
				imageRes = R.drawable.msn;
			if (className == AIMAccount.class.getCanonicalName())
				imageRes = R.drawable.aim;
			if (className == ICQAccount.class.getCanonicalName())
				imageRes = R.drawable.icq;

			final CheckBox auto_connect = (CheckBox)v.findViewById(R.id.auto_connect);
			auto_connect.setChecked(account.isAutoConnect());
			auto_connect.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					final boolean isChecked = auto_connect.isChecked();
					Thread thread = new Thread()
					{
						@Override
						public void run() {

							AccountListActivity.this.runOnUiThread(new Runnable() {
								public void run() {
									String message = isChecked?"Connecting, please wait...":"Disconnecting, please wait...";
									connectAccount = ProgressDialog.show(AccountListActivity.this, "", 
											message, true);
								}
							});

							account.setAutoConnect(isChecked, new Account.AccountLoginListener() {

								@Override
								public void loginDidSucceeded() {
									while (connectAccount==null)
									{
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									
									runOnUiThread(new Runnable() {
										public void run() {
											connectAccount.dismiss();
//											auto_connect.setChecked(true);
										}
									});

									
								}

								@Override
								public void loginDidFailedWithError(final String error) {
									while (connectAccount==null)
									{
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									
									runOnUiThread(new Runnable() {
										public void run() {
											connectAccount.dismiss();
											auto_connect.setChecked(false);
											ErrorReporter.getInstance().reportError(error, AccountListActivity.this);					
										}
									});	
								}
							});  
						}
					};
					thread.start();
				}

			});
//				public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {

			Button remove_button = (Button)v.findViewById(R.id.remove_button);
			remove_button.setOnClickListener(new View.OnClickListener(){

				public void onClick(View v) {
					AccountManager.getInstance().removeAccount(account);
					AccountManager.getInstance().saveAccounts(AccountListActivity.this);
					reloadAccountList();
				}

			});

			badge.setImageResource(imageRes);

			if (account != null) {
				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				//				badge.setMode(ContactsContract.QuickContact.MODE_SMALL);
				//				badge.setScaleType(ImageView.ScaleType.CENTER_CROP);
				String email = account.getUserName();
				String name = account.getName();
				if (name == null)
					name = account.getUserName();
				if (tt != null) {
					tt.setText((name!=null)?name:account.getUserName());                           
				}
				if(bt != null){
					bt.setText(email);
				}
			}
			return v;
		}
	}



}
