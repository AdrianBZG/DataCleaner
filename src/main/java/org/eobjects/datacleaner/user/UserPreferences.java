/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPreferences implements Serializable {

	private static final long serialVersionUID = 5L;

	private static final File userPreferencesFile = new File(DataCleanerHome.get(), "userpreferences.dat");
	private static final Logger logger = LoggerFactory.getLogger(UserPreferences.class);

	private static UserPreferences instance;

	private List<UserDatabaseDriver> databaseDrivers = new ArrayList<UserDatabaseDriver>();
	private List<Datastore> userDatastores = new ArrayList<Datastore>();
	private List<Dictionary> userDictionaries = new ArrayList<Dictionary>();
	private List<StringPattern> userStringPatterns = new ArrayList<StringPattern>();
	private List<SynonymCatalog> userSynonymCatalogs = new ArrayList<SynonymCatalog>();

	private String username;

	private boolean proxyEnabled = false;
	private boolean proxyAuthenticationEnabled = false;
	private String proxyHostname;
	private int proxyPort = 8080;
	private String proxyUsername;
	private String proxyPassword;

	private boolean welcomeDialogShownOnStartup = true;
	private boolean displayDatastoresTaskPane = true;
	private boolean displayJobsTaskPane = true;
	private boolean displayDictionariesTaskPane = false;
	private boolean displaySynonymCatalogsTaskPane = false;
	private boolean displayStringPatternsTaskPane = false;

	private List<File> recentJobFiles = new ArrayList<File>();
	private File openDatastoreDirectory = DataCleanerHome.get();
	private File configuredFileDirectory = DataCleanerHome.get();
	private File analysisJobDirectory = DataCleanerHome.get();
	private File saveDatastoreDirectory;

	public static UserPreferences getInstance() {
		if (instance == null) {
			synchronized (UserPreferences.class) {
				if (instance == null) {
					if (userPreferencesFile.exists()) {
						ObjectInputStream inputStream = null;
						try {
							inputStream = new ObjectInputStream(new FileInputStream(userPreferencesFile));
							instance = (UserPreferences) inputStream.readObject();

							List<UserDatabaseDriver> installedDatabaseDrivers = instance.getDatabaseDrivers();
							for (UserDatabaseDriver userDatabaseDriver : installedDatabaseDrivers) {
								try {
									userDatabaseDriver.loadDriver();
								} catch (IllegalStateException e) {
									logger.error("Could not load database driver", e);
								}
							}

						} catch (InvalidClassException e) {
							logger.warn("User preferences file version does not match application version: {}",
									e.getMessage());
							instance = new UserPreferences();
						} catch (Exception e) {
							logger.warn("Could not read user preferences file", e);
							instance = new UserPreferences();
						} finally {
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (Exception e) {
									throw new IllegalStateException(e);
								}
							}
						}
					} else {
						instance = new UserPreferences();
					}
				}
			}
		}
		return instance;
	}

	private UserPreferences() {
		// prevent instantiation
	}

	public void save() {
		logger.info("Saving user preferences to {}", userPreferencesFile.getAbsolutePath());
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(userPreferencesFile));
			outputStream.writeObject(this);
			outputStream.flush();
		} catch (Exception e) {
			logger.warn("Unexpected error while saving user preferences", e);
			throw new IllegalStateException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		}
	}

	public File getOpenDatastoreDirectory() {
		return openDatastoreDirectory;
	}

	public void setOpenDatastoreDirectory(File openFileDir) {
		this.openDatastoreDirectory = openFileDir;
	}

	public File getConfiguredFileDirectory() {
		return configuredFileDirectory;
	}

	public void setConfiguredFileDirectory(File openPropertyFileDirectory) {
		this.configuredFileDirectory = openPropertyFileDirectory;
	}

	public File getAnalysisJobDirectory() {
		return analysisJobDirectory;
	}

	public void setAnalysisJobDirectory(File saveFileDirectory) {
		this.analysisJobDirectory = saveFileDirectory;
	}

	public File getSaveDatastoreDirectory() {
		if (saveDatastoreDirectory == null) {
			saveDatastoreDirectory = new File(DataCleanerHome.get(), "datastores");
		}
		return saveDatastoreDirectory;
	}

	public void setSaveDatastoreDirectory(File saveDatastoreDirectory) {
		this.saveDatastoreDirectory = saveDatastoreDirectory;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void addRecentJobFile(File file) {
		if (recentJobFiles.contains(file)) {
			recentJobFiles.remove(file);
		}
		recentJobFiles.add(0, file);
	}

	public List<File> getRecentJobFiles() {
		return recentJobFiles;
	}

	public boolean isWelcomeDialogShownOnStartup() {
		return welcomeDialogShownOnStartup;
	}

	public void setWelcomeDialogShownOnStartup(boolean welcomeDialogShownOnStartup) {
		this.welcomeDialogShownOnStartup = welcomeDialogShownOnStartup;
	}

	public List<Datastore> getUserDatastores() {
		if (userDatastores == null) {
			userDatastores = new ArrayList<Datastore>();
		}
		return userDatastores;
	}

	public List<Dictionary> getUserDictionaries() {
		if (userDictionaries == null) {
			userDictionaries = new ArrayList<Dictionary>();
		}
		return userDictionaries;
	}

	public List<SynonymCatalog> getUserSynonymCatalogs() {
		if (userSynonymCatalogs == null) {
			userSynonymCatalogs = new ArrayList<SynonymCatalog>();
		}
		return userSynonymCatalogs;
	}

	public List<UserDatabaseDriver> getDatabaseDrivers() {
		if (databaseDrivers == null) {
			databaseDrivers = new ArrayList<UserDatabaseDriver>();
		}
		return databaseDrivers;
	}

	public List<StringPattern> getUserStringPatterns() {
		if (userStringPatterns == null) {
			userStringPatterns = new ArrayList<StringPattern>();
		}
		return userStringPatterns;
	}

	public boolean isDisplayDatastoresTaskPane() {
		return displayDatastoresTaskPane;
	}

	public void setDisplayDatastoresTaskPane(boolean displayDatastoresTaskPane) {
		this.displayDatastoresTaskPane = displayDatastoresTaskPane;
	}

	public boolean isDisplayJobsTaskPane() {
		return displayJobsTaskPane;
	}

	public void setDisplayJobsTaskPane(boolean displayJobsTaskPane) {
		this.displayJobsTaskPane = displayJobsTaskPane;
	}

	public boolean isDisplayDictionariesTaskPane() {
		return displayDictionariesTaskPane;
	}

	public void setDisplayDictionariesTaskPane(boolean displayDictionariesTaskPane) {
		this.displayDictionariesTaskPane = displayDictionariesTaskPane;
	}

	public boolean isDisplaySynonymCatalogsTaskPane() {
		return displaySynonymCatalogsTaskPane;
	}

	public void setDisplaySynonymCatalogsTaskPane(boolean displaySynonymCatalogsTaskPane) {
		this.displaySynonymCatalogsTaskPane = displaySynonymCatalogsTaskPane;
	}

	public boolean isDisplayStringPatternsTaskPane() {
		return displayStringPatternsTaskPane;
	}

	public void setDisplayStringPatternsTaskPane(boolean displayStringPatternsTaskPane) {
		this.displayStringPatternsTaskPane = displayStringPatternsTaskPane;
	}

	public boolean isProxyEnabled() {
		return proxyEnabled;
	}

	public void setProxyEnabled(boolean proxyEnabled) {
		this.proxyEnabled = proxyEnabled;
	}

	public String getProxyHostname() {
		return proxyHostname;
	}

	public void setProxyHostname(String proxyHostname) {
		this.proxyHostname = proxyHostname;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public boolean isProxyAuthenticationEnabled() {
		return proxyAuthenticationEnabled;
	}

	public void setProxyAuthenticationEnabled(boolean proxyAuthenticationEnabled) {
		this.proxyAuthenticationEnabled = proxyAuthenticationEnabled;
	}
}
