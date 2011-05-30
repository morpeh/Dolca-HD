package com.volnatech.dolca;

import android.app.*;
import android.content.*;

public class ErrorReporter {
	
	private static volatile ErrorReporter instance = null;
	
	public static ErrorReporter getInstance() {
		if (instance == null)
			synchronized (ErrorReporter.class) {
				if (instance == null)
					instance = new ErrorReporter();
			}
		return instance;
	}
	
	private void reportError(final String title, final String message, final int code, Activity context){
		   new AlertDialog.Builder(context)
		      .setMessage(message)
		      .setTitle(title)
		      .setCancelable(true)
		      .setNeutralButton(android.R.string.ok,
		         new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton){}
		         })
		      .show();
	}

	public void reportError(final String title, final String message, final Activity context){
		final ErrorReporter self = this;
		context.runOnUiThread(new Runnable() {
			public void run() {
				self.reportError(title, message, -1, context);
		}});
	}
	
	public void reportError(final String message, Activity context){
		this.reportError("Error", message, context);
	}
	public void reportException(Exception e, Activity context){
		String message = e.getLocalizedMessage();
		this.reportError(message!=null?message:e.toString(), context);
	}
}
