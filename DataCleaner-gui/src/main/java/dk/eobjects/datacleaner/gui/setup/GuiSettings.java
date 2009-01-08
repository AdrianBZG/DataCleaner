/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.catalog.NamedRegex;
import dk.eobjects.datacleaner.catalog.TextFileDictionary;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.DatabaseDriver;
import dk.eobjects.datacleaner.util.WeakObservable;
import dk.eobjects.datacleaner.validator.dictionary.DictionaryManager;
import dk.eobjects.metamodel.util.ObjectComparator;

/**
 * The GuiSettings represents the users settings, that are modified during use
 * of DataCleaner. Thus the settings are in contrast to the configuration which
 * cannot be * modified or saved during runtime.
 */
public class GuiSettings extends WeakObservable implements Serializable {

	public static final String SETTINGS_FILE = "datacleaner-settings.data";
	public static final String REGEXES_FILE = "datacleaner-regexes.properties";
	public static final String DICTIONARIES_SAMPLES = "samples/dictionaries";

	private static final long serialVersionUID = -8625459660466339757L;
	private static final Log _log = LogFactory.getLog(GuiSettings.class);
	private static GuiSettings _cachedSettings;

	private List<IDictionary> _dictionaries;
	private List<DatabaseDriver> _databaseDrivers;
	private String _lookAndFeelClassName;
	private boolean _horisontalMatrixTables;

	// Regexes are not serialized, but loaded from REGEXES_FILE
	private transient List<NamedRegex> _regexes;

	protected GuiSettings() {
		super();
		_dictionaries = new ArrayList<IDictionary>();
		_databaseDrivers = new ArrayList<DatabaseDriver>();
		_regexes = new ArrayList<NamedRegex>();
		_horisontalMatrixTables = true;
	}

	/**
	 * Initializes settings on program start-up (only called once)
	 */
	public static void initialize() {
		_cachedSettings = null;
		GuiSettings settings = getSettings();

		// Initialize look and feel
		LookAndFeel lookAndFeel = null;

		if (LookUtils.IS_OS_WINDOWS) {
			WindowsLookAndFeel windowsLookAndFeel = new WindowsLookAndFeel();
			installLookAndFeel(windowsLookAndFeel);
			lookAndFeel = windowsLookAndFeel;
		}

		try {
			// Ticket #213: Temporary workaround untill Looks 2.2.0 can be
			// retrieved from Central Maven repository
			PlasticXPLookAndFeel plasticXPLookAndFeel = new PlasticXPLookAndFeel();
			installLookAndFeel(plasticXPLookAndFeel);
			Plastic3DLookAndFeel plastic3DLookAndFeel = new Plastic3DLookAndFeel();
			installLookAndFeel(plastic3DLookAndFeel);
			PlasticLookAndFeel plasticLookAndFeel = new PlasticLookAndFeel();
			installLookAndFeel(plasticLookAndFeel);

			lookAndFeel = plasticXPLookAndFeel;
		} catch (Exception e) {
			_log.warn(e);
		}

		try {
			String settingsLookAndFeel = settings.getLookAndFeelClassName();
			if (settingsLookAndFeel != null) {
				UIManager.setLookAndFeel(settingsLookAndFeel);
			} else if (lookAndFeel != null) {
				UIManager.setLookAndFeel(lookAndFeel);
			}
		} catch (Exception e) {
			_log.error(e);
		}
		settings.setLookAndFeelClassName(UIManager.getLookAndFeel().getClass()
				.getName());

		List<IDictionary> dictionaries = settings.getDictionaries();

		// If no dictionaries exist, load dictionaries from samples directory
		if (dictionaries == null || dictionaries.isEmpty()) {
			File dictionaryDir = new File(DICTIONARIES_SAMPLES);
			if (dictionaryDir.exists() && dictionaryDir.isDirectory()) {
				File[] dictionaryFiles = dictionaryDir.listFiles();
				for (int i = 0; i < dictionaryFiles.length; i++) {
					File dictionaryFile = dictionaryFiles[i];
					if (dictionaryFile.getName().endsWith(".txt")) {
						dictionaries.add(new TextFileDictionary(dictionaryFile
								.getName(), dictionaryFile));
					}
				}
			} else {
				_log.warn("Could not find dictionary samples directory: "
						+ DICTIONARIES_SAMPLES);
			}
		}
		sortDictionaries(dictionaries);

		// Initialize dictionary manager
		DictionaryManager.setDictionaries(dictionaries);
	}

	private static void installLookAndFeel(LookAndFeel lookAndFeel) {
		String lookAndFeelClass = lookAndFeel.getClass().getCanonicalName();
		LookAndFeelInfo[] installedLookAndFeels = UIManager
				.getInstalledLookAndFeels();
		for (int i = 0; i < installedLookAndFeels.length; i++) {
			if (installedLookAndFeels[i].getClassName()
					.equals(lookAndFeelClass)) {
				return;
			}
		}
		UIManager.installLookAndFeel(lookAndFeel.getName(), lookAndFeelClass);
	}

	public static void saveSettings(GuiSettings settings) {
		try {
			if (settings != null) {
				List<IDictionary> dictionaries = settings.getDictionaries();
				DictionaryManager.setDictionaries(dictionaries);
				sortDictionaries(dictionaries);
				ObjectOutputStream outputStream = new ObjectOutputStream(
						new FileOutputStream(new File(SETTINGS_FILE)));
				outputStream.writeObject(settings);
				outputStream.close();

				List<NamedRegex> regexes = settings.getRegexes();
				NamedRegex.saveToFile(regexes, new File(REGEXES_FILE));

				_cachedSettings = settings;
				settings.setChanged();
				settings.notifyObservers();
			}
		} catch (Exception e) {
			_log.error(e);
			GuiHelper.showErrorMessage("Error saving settings", e.getMessage(),
					e);
		}
	}
	
	public boolean isDriverInstalled(String driverClassName) {
		if (driverClassName != null) {
			List<DatabaseDriver> databaseDrivers = getDatabaseDrivers();
			for (DatabaseDriver driver : databaseDrivers) {
				if (driverClassName.equals(driver.getDriverClass())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Retrieves the settings (will be called each time the user accesses or
	 * modifies settings)
	 */
	public static GuiSettings getSettings() {
		if (_cachedSettings == null) {
			// Gets settings from file
			File settingsFile = new File(SETTINGS_FILE);
			File regexesFile = new File(REGEXES_FILE);
			if (settingsFile.exists()) {
				try {
					ObjectInputStream inputStream = new ObjectInputStream(
							new FileInputStream(settingsFile));
					_cachedSettings = (GuiSettings) inputStream.readObject();
					inputStream.close();

				} catch (Exception e) {
					GuiHelper.showErrorMessage("Error loading settings", e
							.getMessage(), e);
				}
			} else {
				_cachedSettings = new GuiSettings();
			}
			if (regexesFile.exists()) {
				List<NamedRegex> regexes = NamedRegex.loadFromFile(regexesFile);
				Collections.sort(regexes, ObjectComparator.getComparator());
				_cachedSettings.setRegexes(regexes);
			} else {
				_log.warn("Regex file does not exist: " + REGEXES_FILE);
				_cachedSettings.setRegexes(new ArrayList<NamedRegex>());
			}
		}
		return _cachedSettings;
	}

	private static void sortDictionaries(List<IDictionary> dictionaries) {
		final Comparator<Object> comparator = ObjectComparator.getComparator();
		Collections.sort(dictionaries, new Comparator<IDictionary>() {
			public int compare(IDictionary o1, IDictionary o2) {
				return comparator.compare(o1.getName(), o2.getName());
			}
		});
	}

	public boolean isHorisontalMatrixTables() {
		return _horisontalMatrixTables;
	}

	public void setHorisontalMatrixTables(boolean horisontalMatrixTables) {
		_horisontalMatrixTables = horisontalMatrixTables;
	}

	public String getLookAndFeelClassName() {
		return _lookAndFeelClassName;
	}

	public GuiSettings setLookAndFeelClassName(String lookAndFeelClassName) {
		_lookAndFeelClassName = lookAndFeelClassName;
		return this;
	}

	public List<IDictionary> getDictionaries() {
		return _dictionaries;
	}

	public GuiSettings setDictionaries(List<IDictionary> dictionaries) {
		_dictionaries = dictionaries;
		return this;
	}

	public List<NamedRegex> getRegexes() {
		return _regexes;
	}

	public GuiSettings setRegexes(List<NamedRegex> regexes) {
		_regexes = regexes;
		return this;
	}

	@Override
	public String toString() {
		return "GuiSettings[lookAndFeelClassName=" + _lookAndFeelClassName
				+ ",dictionaries="
				+ ArrayUtils.toString(_dictionaries.toArray())
				+ ",databaseDrivers="
				+ ArrayUtils.toString(_databaseDrivers.toArray()) + ",regexes="
				+ ArrayUtils.toString(_regexes.toArray()) + "]";
	}

	public List<DatabaseDriver> getDatabaseDrivers() {
		return _databaseDrivers;
	}

	public GuiSettings setDatabaseDrivers(List<DatabaseDriver> driverLocations) {
		_databaseDrivers = driverLocations;
		return this;
	}
}