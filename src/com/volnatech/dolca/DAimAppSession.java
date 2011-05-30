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
