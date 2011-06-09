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

import java.io.File;
import java.util.*;
import com.volnatech.dolca.config.*;
import net.kano.joscar.DefensiveTools;
import net.kano.joustsim.Screenname;
import net.kano.joustsim.oscar.AimSession;
import net.kano.joustsim.oscar.AppSession;

public class DAppSession implements AppSession {

	private final File localPrefsDir;
	private final GlobalPrefs globalPrefs;
	private boolean loadedGlobalPrefs = false;

	private Map<Screenname, LocalPreferencesManager> prefs
	= new HashMap<Screenname, LocalPreferencesManager>();
	private Map<Screenname, List<AimSession>> sessions
	= new HashMap<Screenname, List<AimSession>>();

	private Thread shutdownHook = null;

	public DAppSession(File baseDir) throws IllegalArgumentException {
		DefensiveTools.checkNull(baseDir, "baseDir");

		if (baseDir.exists()) {
			if (!baseDir.isDirectory()) {
				throw new IllegalArgumentException(baseDir.getPath()
						+ " is not a directory");
			}
		} else {
			baseDir.mkdirs();
			if (!baseDir.isDirectory()) {
				throw new IllegalArgumentException(baseDir.getPath()
						+ " is not a directory and cannot be created");
			}
		}

		File configDir = PrefTools.getConfigDir(baseDir);
		this.localPrefsDir = PrefTools.getLocalConfigDir(configDir);
		PrefTools.getGlobalConfigDir(configDir);

		this.globalPrefs = new GlobalPrefs(configDir);
	}

	public synchronized void setSavePrefsOnExit(boolean save) {
		if (shutdownHook == null) {
			if (!save) return;
			shutdownHook = new Thread() {
				public void run() {
					saveAllPrefs();
				}
			};
		}

		Runtime runtime = Runtime.getRuntime();
		if (save) {
			runtime.addShutdownHook(shutdownHook);
		} else {
			runtime.removeShutdownHook(shutdownHook);
			shutdownHook = null;
		}
	}

	private synchronized void saveAllPrefs() {
		try {
			globalPrefs.savePrefs();
		} catch (Exception e) {
			//      logger.log(Level.WARNING, "Couldn't save global prefs", e);
			e.printStackTrace();
		}
		for (LocalPreferencesManager mgr : prefs.values()) {
			mgr.saveAllPrefs();
		}
	}


	public AimSession openAimSession(Screenname sn) {
		AimSession sess = new DAimAppSession(this, sn);

		synchronized (this) {
			List<AimSession> snsesses = sessions.get(sn);
			if (snsesses == null) {
				snsesses = new ArrayList<AimSession>();
				sessions.put(sn, snsesses);
			}

			snsesses.add(sess);
		}
		return sess;
	}

	public void closeAimSession(Screenname sn) {
		synchronized (this) {
			List<AimSession> snsesses = sessions.get(sn);
			if (snsesses!=null)
			{
				if (snsesses.size()>0)
				{
					AimSession sess = snsesses.get(0);
					sess.closeConnection(); 
				}
			}
		}
	}

	public synchronized GlobalPrefs getGlobalPrefs() {
		if (!loadedGlobalPrefs) {
			loadedGlobalPrefs = true;
			globalPrefs.loadPrefs();
		}
		return globalPrefs;
	}

	public synchronized LocalPreferencesManager getLocalPrefs(Screenname sn) {
		DefensiveTools.checkNull(sn, "sn");
		File prefsDir = getLocalPrefsDir(sn);
		if (prefsDir == null) return null;

		LocalPreferencesManager appPrefs = prefs.get(sn);
		if (appPrefs == null) {
			appPrefs = new LocalPreferencesManager(sn, prefsDir);
			prefs.put(sn, appPrefs);
		}
		return appPrefs;
	}

	public synchronized LocalPreferencesManager getLocalPrefsIfExist(
			Screenname sn) {
		File prefsDir = getLocalPrefsDir(sn);
		if (prefsDir == null) return null;
		if (prefsDir.isDirectory()) {
			return getLocalPrefs(sn);
		} else {
			return null;
		}
	}

	public synchronized boolean deleteLocalPrefs(Screenname sn) {
		File prefsDir = getLocalPrefsDir(sn);
		if (prefsDir == null) return false;

		boolean deleted = PrefTools.deleteDir(prefsDir);
		if (deleted) prefs.remove(sn);
		return deleted;
	}

	public synchronized Screenname[] getKnownScreennames() {
		globalPrefs.reloadIfNecessary();
		String[] possible = globalPrefs.getKnownScreennames();
		Collection<LocalPreferencesManager> loaded = prefs.values();
		Set<Screenname> known = new HashSet<Screenname>(
				possible.length + loaded.size());

		for (String sn : possible) {
			known.add(new Screenname(sn));
		}

		for (LocalPreferencesManager prefs : loaded) {
			String fmt = prefs.getGeneralPrefs().getScreennameFormat();
			Screenname sn;
			if (fmt == null) {
				sn = prefs.getScreenname();
			} else {
				sn = new Screenname(fmt);
			}
			known.add(sn);
		}
		return known.toArray(new Screenname[known.size()]);
	}

	private File getLocalPrefsDir(Screenname sn) {
		return PrefTools.getLocalPrefsDirForScreenname(this.localPrefsDir, sn);
	}

	public boolean hasLocalPrefs(Screenname sn) {
		return getLocalPrefsDir(sn).isDirectory();
	}
}
