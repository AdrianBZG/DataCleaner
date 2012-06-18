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
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.LoginChangeListener;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main implementation of {@link UserPreferences}.
 */
public class UserPreferencesImpl implements UserPreferences, Serializable {

    private static final long serialVersionUID = 6L;

    private static final Logger logger = LoggerFactory.getLogger(UserPreferencesImpl.class);

    private transient File _userPreferencesFile;
    private transient List<LoginChangeListener> loginChangeListeners;

    private List<UserDatabaseDriver> databaseDrivers = new ArrayList<UserDatabaseDriver>();
    private List<ExtensionPackage> extensionPackages = new ArrayList<ExtensionPackage>();
    private List<Datastore> userDatastores = new ArrayList<Datastore>();
    private List<Dictionary> userDictionaries = new ArrayList<Dictionary>();
    private List<StringPattern> userStringPatterns = new ArrayList<StringPattern>();
    private List<SynonymCatalog> userSynonymCatalogs = new ArrayList<SynonymCatalog>();
    private Map<String, String> additionalProperties = new HashMap<String, String>();

    private String username;

    private boolean proxyEnabled = false;
    private boolean proxyAuthenticationEnabled = false;
    private String proxyHostname;
    private int proxyPort = 8080;
    private String proxyUsername;
    private String proxyPassword;

    private List<File> recentJobFiles = new ArrayList<File>();
    private File openDatastoreDirectory;
    private File configuredFileDirectory;
    private File analysisJobDirectory;
    private File saveDatastoreDirectory;

    private MonitorConnection monitorConnection;

    private QuickAnalysisStrategy quickAnalysisStrategy = new QuickAnalysisStrategy();

    public UserPreferencesImpl(File userPreferencesFile) {
        _userPreferencesFile = userPreferencesFile;
    }

    public static UserPreferences load(final File userPreferencesFile, final boolean loadDatabaseDrivers) {
        if (userPreferencesFile == null || !userPreferencesFile.exists()) {
            logger.info("User preferences file does not exist");
            return new UserPreferencesImpl(userPreferencesFile);
        }

        ChangeAwareObjectInputStream inputStream = null;
        try {
            inputStream = new ChangeAwareObjectInputStream(new FileInputStream(userPreferencesFile));
            inputStream.addRenamedClass("org.eobjects.datacleaner.user.UserPreferences", UserPreferencesImpl.class);
            UserPreferencesImpl result = (UserPreferencesImpl) inputStream.readObject();

            if (loadDatabaseDrivers) {
                List<UserDatabaseDriver> installedDatabaseDrivers = result.getDatabaseDrivers();
                for (UserDatabaseDriver userDatabaseDriver : installedDatabaseDrivers) {
                    try {
                        userDatabaseDriver.loadDriver();
                    } catch (IllegalStateException e) {
                        logger.error("Could not load database driver", e);
                    }
                }
            }

            result._userPreferencesFile = userPreferencesFile;

            return result;
        } catch (InvalidClassException e) {
            logger.warn("User preferences file version does not match application version: {}", e.getMessage());
            return new UserPreferencesImpl(userPreferencesFile);
        } catch (Exception e) {
            logger.warn("Could not read user preferences file", e);
            return new UserPreferencesImpl(userPreferencesFile);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    @Override
    public void save() {
        logger.info("Saving user preferences to {}", _userPreferencesFile.getAbsolutePath());
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(_userPreferencesFile));
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

    private List<LoginChangeListener> getLoginChangeListeners() {
        if (loginChangeListeners == null) {
            loginChangeListeners = new ArrayList<LoginChangeListener>();
        }
        return loginChangeListeners;
    }

    @Override
    public void addLoginChangeListener(LoginChangeListener listener) {
        getLoginChangeListeners().add(listener);
    }

    @Override
    public void removeLoginChangeListener(LoginChangeListener listener) {
        getLoginChangeListeners().add(listener);
    }

    @Override
    public File getOpenDatastoreDirectory() {
        if (openDatastoreDirectory == null) {
            openDatastoreDirectory = DataCleanerHome.get();
        }
        return openDatastoreDirectory;
    }

    @Override
    public void setOpenDatastoreDirectory(File openFileDir) {
        this.openDatastoreDirectory = openFileDir;
    }

    @Override
    public File getConfiguredFileDirectory() {
        if (configuredFileDirectory == null) {
            configuredFileDirectory = DataCleanerHome.get();
        }
        return configuredFileDirectory;
    }

    @Override
    public void setConfiguredFileDirectory(File openPropertyFileDirectory) {
        this.configuredFileDirectory = openPropertyFileDirectory;
    }

    @Override
    public File getAnalysisJobDirectory() {
        if (analysisJobDirectory == null) {
            analysisJobDirectory = DataCleanerHome.get();
        }
        return analysisJobDirectory;
    }

    @Override
    public void setAnalysisJobDirectory(File saveFileDirectory) {
        this.analysisJobDirectory = saveFileDirectory;
    }

    @Override
    public File getSaveDatastoreDirectory() {
        if (saveDatastoreDirectory == null) {
            saveDatastoreDirectory = new File(DataCleanerHome.get(), "datastores");
        }
        return saveDatastoreDirectory;
    }

    @Override
    public void setSaveDatastoreDirectory(File saveDatastoreDirectory) {
        this.saveDatastoreDirectory = saveDatastoreDirectory;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;

        List<LoginChangeListener> listeners = getLoginChangeListeners();
        for (LoginChangeListener listener : listeners) {
            listener.onLoginStateChanged(isLoggedIn(), username);
        }
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isLoggedIn() {
        return !StringUtils.isNullOrEmpty(getUsername());
    }

    @Override
    public void addRecentJobFile(File file) {
        if (recentJobFiles.contains(file)) {
            recentJobFiles.remove(file);
        }
        recentJobFiles.add(0, file);
    }

    @Override
    public List<File> getRecentJobFiles() {
        return recentJobFiles;
    }

    @Override
    public List<Datastore> getUserDatastores() {
        if (userDatastores == null) {
            userDatastores = new ArrayList<Datastore>();
        }
        return userDatastores;
    }

    @Override
    public List<Dictionary> getUserDictionaries() {
        if (userDictionaries == null) {
            userDictionaries = new ArrayList<Dictionary>();
        }
        return userDictionaries;
    }

    @Override
    public List<SynonymCatalog> getUserSynonymCatalogs() {
        if (userSynonymCatalogs == null) {
            userSynonymCatalogs = new ArrayList<SynonymCatalog>();
        }
        return userSynonymCatalogs;
    }

    @Override
    public List<UserDatabaseDriver> getDatabaseDrivers() {
        if (databaseDrivers == null) {
            databaseDrivers = new ArrayList<UserDatabaseDriver>();
        }
        return databaseDrivers;
    }

    @Override
    public List<StringPattern> getUserStringPatterns() {
        if (userStringPatterns == null) {
            userStringPatterns = new ArrayList<StringPattern>();
        }
        return userStringPatterns;
    }

    @Override
    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    @Override
    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    @Override
    public String getProxyHostname() {
        return proxyHostname;
    }

    @Override
    public void setProxyHostname(String proxyHostname) {
        this.proxyHostname = proxyHostname;
    }

    @Override
    public int getProxyPort() {
        return proxyPort;
    }

    @Override
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    @Override
    public String getProxyUsername() {
        return proxyUsername;
    }

    @Override
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    @Override
    public String getProxyPassword() {
        return proxyPassword;
    }

    @Override
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public boolean isProxyAuthenticationEnabled() {
        return proxyAuthenticationEnabled;
    }

    @Override
    public void setProxyAuthenticationEnabled(boolean proxyAuthenticationEnabled) {
        this.proxyAuthenticationEnabled = proxyAuthenticationEnabled;
    }

    @Override
    public QuickAnalysisStrategy getQuickAnalysisStrategy() {
        return quickAnalysisStrategy;
    }

    @Override
    public void setQuickAnalysisStrategy(QuickAnalysisStrategy quickAnalysisStrategy) {
        this.quickAnalysisStrategy = quickAnalysisStrategy;
    }

    @Override
    public List<ExtensionPackage> getExtensionPackages() {
        if (extensionPackages == null) {
            extensionPackages = new ArrayList<ExtensionPackage>();
        }
        return extensionPackages;
    }

    @Override
    public void setExtensionPackages(List<ExtensionPackage> extensionPackages) {
        this.extensionPackages = extensionPackages;
    }

    @Override
    public Map<String, String> getAdditionalProperties() {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<String, String>();
        }
        return additionalProperties;
    }

    @Override
    public void setMonitorConnection(MonitorConnection connection) {
        this.monitorConnection = connection;
    }

    @Override
    public MonitorConnection getMonitorConnection() {
        return monitorConnection;
    }
}
