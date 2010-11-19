package org.eobjects.datacleaner.user;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.database.DriverWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserDatabaseDriver implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(UserDatabaseDriver.class);

	private transient Driver _driverInstance;
	private transient Driver _registeredDriver;
	private transient boolean _loaded = false;
	private final File[] _files;
	private final String _driverClassName;

	public UserDatabaseDriver(File[] files, String driverClassName) {
		if (files == null) {
			throw new IllegalStateException("Driver file(s) cannot be null");
		}
		if (driverClassName == null) {
			throw new IllegalStateException("Driver class name cannot be null");
		}
		_files = files;
		_driverClassName = driverClassName;
	}

	public String getDriverClassName() {
		return _driverClassName;
	}

	public File[] getFiles() {
		return _files;
	}

	public UserDatabaseDriver loadDriver() throws IllegalStateException {
		try {
			URL[] urls = new URL[_files.length];
			for (int i = 0; i < urls.length; i++) {
				URL url = _files[i].toURI().toURL();
				logger.debug("Using URL: {}", url);
				urls[i] = url;
			}

			URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

			Class<?> loadedClass = Class.forName(_driverClassName, true, classLoader);
			logger.info("Loaded class: {}", loadedClass.getName());

			if (ReflectionUtils.is(loadedClass, Driver.class)) {
				_driverInstance = (Driver) loadedClass.newInstance();
				_registeredDriver = new DriverWrapper(_driverInstance);
				DriverManager.registerDriver(_registeredDriver);
				_loaded = true;
			} else {
				throw new IllegalStateException("Class is not a Driver class: " + _driverClassName);
			}
			return this;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void unloadDriver() {
		try {
			DriverManager.deregisterDriver(_registeredDriver);
			_registeredDriver = null;
			_driverInstance = null;
			_loaded = false;
		} catch (SQLException e) {
			logger.error("Exception occurred while unloading driver: " + _driverClassName, e);
		}
	}

	public boolean isLoaded() {
		return _loaded;
	}
}
