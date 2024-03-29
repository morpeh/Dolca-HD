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

import com.volnatech.dolca.MessengerService.LocalBinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.*;

public class AddAccountActivity extends Activity {

	private boolean mIsBound = false;
	private ProgressDialog addAccountDialog = null;
	private MessengerService messengerService = null;

	void doBindService() {
		if (mIsBound == false) {
			Intent intent = new Intent(this, MessengerService.class);
			mIsBound = bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE);
		}
	}

	void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// mService = new Messenger(service);
			LocalBinder binder = (MessengerService.LocalBinder) service;
			messengerService = (MessengerService) binder.getService();
			// Toast.makeText(this, R.string.remote_service_connected,
			// Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			messengerService = null;
			// Toast.makeText(Main.this, R.string.remote_service_disconnected,
			// Toast.LENGTH_SHORT).show();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_account);
		doBindService();
		final Spinner spinner = (Spinner) findViewById(R.id.service_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.service_array,
				android.R.layout.simple_spinner_item);
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		final EditText login_text_edit = (EditText) findViewById(R.id.login_text_edit);
		final EditText pass_text_edit = (EditText) findViewById(R.id.pass_text_edit);

		Button button = (Button) findViewById(R.id.add_account_button);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String login = (String) login_text_edit.getText().toString();
				final String pass = (String) pass_text_edit.getText().toString();
				final int pos = spinner.getSelectedItemPosition();

				if (messengerService != null) {
					addAccountDialog = ProgressDialog.show(
							AddAccountActivity.this, "",
							"Checking account details, please wait...", true);
					messengerService.addAccount(login, pass, pos,
							new Account.AccountLoginListener() {

								@Override
								public void loginDidSucceeded() {
									addAccountDialog.dismiss();
									messengerService.saveAccounts(AddAccountActivity.this);
									AddAccountActivity.this.finish();
								}

								@Override
								public void loginDidFailedWithError(String error) {
									addAccountDialog.dismiss();
									ErrorReporter.getInstance().reportError(error, AddAccountActivity.this);
								}
							});
				}
			}
		});
	}
}
