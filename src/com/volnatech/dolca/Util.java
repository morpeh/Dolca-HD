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

import java.io.*;

import android.app.Activity;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

public class Util {
	public static final String VERSION = "1.0";
	public Activity activity = null;
	private static volatile Util instance = null;
	private Util(){

	}
	public static Util getInstance() {
		if (instance == null)
			synchronized (Util.class) {
				if (instance == null)
					instance = new Util();
			}
		return instance;
	}
	
//	private static Bitmap loadContactPhoto(ContentResolver cr, long  id) {
//	    Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
//	    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
//	    if (input == null) {
//	        return null;
//	    }
//	    return BitmapFactory.decodeStream(input);
//	}
	
//	public String getNameByEmail(String email){
//		String name = null;
//		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(email));
//		String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME };
//		Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
//		if (cursor != null && cursor.moveToFirst()) {		    	
//			int column = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
//			if (column>=0)
//				name = cursor.getString(column);
//		}
//		return name;
//	}
	
//	public void fillFriendFieldsFromScreenName(DFriend friend, String screenName){
//		friend.setEmail(screenName);
//		friend.setName(screenName);
//		
//		String name = null;
//		long id = -1;
//		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(screenName));
//		String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID };
//		Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
//		if (cursor != null && cursor.moveToFirst()) {		    	
//			int column = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
//			if (column>=0)
//				name = cursor.getString(column);
//			column = cursor.getColumnIndex(Contacts._ID);
//			if (column>=0)
//				id = cursor.getLong(column);
//		}
//		friend.setName(name);
//		if (id>=0)
//			friend.setBitmap(loadContactPhoto(activity.getContentResolver(), id));
//	}

//	public void fillFriendFieldsFromEmail(DFriend friend, String email){
//		String name = null;
//		long id = -1;
//		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(email));
//		String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID };
//		Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
//		if (cursor != null && cursor.moveToFirst()) {		    	
//			int column = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
//			if (column>=0)
//				name = cursor.getString(column);
//			column = cursor.getColumnIndex(Contacts._ID);
//			if (column>=0)
//				id = cursor.getLong(column);
//		}
//		friend.setName(name);
//		if (id>=0)
//			friend.setBitmap(loadContactPhoto(activity.getContentResolver(), id));
//	}
	
	public static String objectToString(Serializable object) {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try {
	        new ObjectOutputStream(out).writeObject(object);
	        byte[] data = out.toByteArray();
	        out.close();

	        out = new ByteArrayOutputStream();
	        Base64OutputStream b64 = new Base64OutputStream(out, Base64.DEFAULT);
	        b64.write(data);
	        b64.close();
	        out.close();
	        return new String(out.toByteArray());
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public static Object stringToObject(String encodedObject) {
	    try {
	        return new ObjectInputStream(new Base64InputStream(
	                new ByteArrayInputStream(encodedObject.getBytes()), Base64.DEFAULT)).readObject();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	public void destory(){
		instance = null;
	}

}
