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

import com.volnatech.dolca.config.*;

import net.kano.joustsim.Screenname;
import net.kano.joustsim.oscar.AbstractAimSession;
import net.kano.joustsim.oscar.AimConnection;
import net.kano.joustsim.oscar.AimConnectionProperties;
import net.kano.joustsim.oscar.AppSession;
import net.kano.joustsim.trust.TrustPreferences;

public class DAimAppSession extends AbstractAimSession {

	private AimConnection connection = null;
	private DAppSession appSession = null;
	private Screenname screenName = null;
	
	private final LocalPreferencesManager localPrefs;

	public DAimAppSession(DAppSession appSession, Screenname screenName){
		this.appSession = appSession;
		this.screenName = screenName;
		
	      localPrefs = appSession.getLocalPrefs(screenName);
	        Thread keysLoaderThread = new Thread(new Runnable() {
	            public void run() {
	                localPrefs.loadEverything();
	            }
	        }, "Prefs loader: " + screenName);
	        keysLoaderThread.start();
	}

	public void closeConnection() {
		AimConnection conn = getConnection();
		if (conn != null) conn.disconnect();
	}

	public AppSession getAppSession() {
		return this.appSession;
	}

	public AimConnection getConnection() {
		return this.connection;
	}

	public Screenname getScreenname() {
		return this.screenName;
	}

	public TrustPreferences getTrustPreferences() {
		return getLocalPrefs();
	}

	public LocalPreferencesManager getLocalPrefs() {
		return localPrefs;
	}

	public AimConnection openConnection(AimConnectionProperties props) {
		closeConnection();
		AimConnection conn = new AimConnection(this,
				getTrustPreferences(), props);
		synchronized(this) {
			this.connection = conn;
		}
		return conn;
	}

}
