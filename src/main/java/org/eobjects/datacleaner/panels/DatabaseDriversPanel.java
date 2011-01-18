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
package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.actions.DownloadFilesActionListener;
import org.eobjects.datacleaner.actions.FileDownloadListener;
import org.eobjects.datacleaner.database.DatabaseDriverCatalog;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.datacleaner.database.DatabaseDriverState;
import org.eobjects.datacleaner.user.UserDatabaseDriver;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the panel in the Options dialog where the user can get an overview
 * and configure database drivers.
 * 
 * @author Kasper Sørensen
 */
public class DatabaseDriversPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DatabaseDriversPanel.class);
	private final UserPreferences userPreferences = UserPreferences.getInstance();
	private final ImageManager imageManager = ImageManager.getInstance();
	private final Set<String> _usedDriverClassNames = new HashSet<String>();
	private final DatabaseDriverCatalog _databaseDriverCatalog = new DatabaseDriverCatalog();

	public DatabaseDriversPanel(AnalyzerBeansConfiguration configuration) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		setLayout(new BorderLayout());

		DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
		String[] datastoreNames = datastoreCatalog.getDatastoreNames();
		for (String name : datastoreNames) {
			Datastore datastore = datastoreCatalog.getDatastore(name);
			if (datastore instanceof JdbcDatastore) {
				String driverClass = ((JdbcDatastore) datastore).getDriverClass();
				if (driverClass != null) {
					_usedDriverClassNames.add(driverClass);
				}
			}
		}

		updateComponents();
	}

	private void updateComponents() {
		this.removeAll();
		JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(WidgetFactory.createToolBarSeparator());

		JButton addDriverButton = WidgetFactory.createButton("Add database driver",
				imageManager.getImageIcon("images/actions/add.png"));

		toolBar.add(addDriverButton);

		DCTable table = getDatabaseDriverTable();
		this.add(toolBar, BorderLayout.NORTH);
		this.add(table.toPanel(), BorderLayout.CENTER);
	}

	private DCTable getDatabaseDriverTable() {
		final List<DatabaseDriverDescriptor> databaseDrivers = _databaseDriverCatalog.getDatabaseDrivers();
		final TableModel tableModel = new DefaultTableModel(new String[] { "", "Database", "Driver class", "Installed?",
				"Used?" }, databaseDrivers.size());

		final DCTable table = new DCTable(tableModel);

		final Icon validIcon = imageManager.getImageIcon("images/status/valid.png", IconUtils.ICON_SIZE_SMALL);
		final Icon invalidIcon = imageManager.getImageIcon("images/status/error.png", IconUtils.ICON_SIZE_SMALL);

		int row = 0;
		for (final DatabaseDriverDescriptor dd : databaseDrivers) {
			final String driverClassName = dd.getDriverClassName();
			final String displayName = dd.getDisplayName();

			final Icon driverIcon = imageManager.getImageIcon(_databaseDriverCatalog.getIconImagePath(dd),
					IconUtils.ICON_SIZE_SMALL);

			tableModel.setValueAt(driverIcon, row, 0);
			tableModel.setValueAt(displayName, row, 1);
			tableModel.setValueAt(driverClassName, row, 2);
			tableModel.setValueAt("", row, 3);
			tableModel.setValueAt("", row, 4);

			final int installedCol = 3;

			final DatabaseDriverState state = _databaseDriverCatalog.getState(dd);
			if (state == DatabaseDriverState.INSTALLED_WORKING) {
				tableModel.setValueAt(validIcon, row, installedCol);
			} else if (state == DatabaseDriverState.INSTALLED_NOT_WORKING) {
				tableModel.setValueAt(invalidIcon, row, installedCol);
			} else if (state == DatabaseDriverState.NOT_INSTALLED) {
				final String[] downloadUrls = dd.getDownloadUrls();
				if (downloadUrls != null) {
					final DCPanel buttonPanel = new DCPanel();
					buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));

					final JButton downloadButton = WidgetFactory.createSmallButton("images/actions/download.png");
					downloadButton.setToolTipText("Download and install the driver for " + dd.getDisplayName());

					downloadButton.addActionListener(new DownloadFilesActionListener(dd.getDownloadUrls(),
							new FileDownloadListener() {
								@Override
								public void onFilesDownloaded(File[] files) {
									String driverClassName = dd.getDriverClassName();

									logger.info("Registering and loading driver '{}' in files '{}'", driverClassName, files);

									final UserDatabaseDriver userDatabaseDriver = new UserDatabaseDriver(files,
											driverClassName);
									userPreferences.getDatabaseDrivers().add(userDatabaseDriver);

									try {
										userDatabaseDriver.loadDriver();
									} catch (IllegalStateException e) {
										WidgetUtils.showErrorMessage("Error while loading driver",
												"Error message: " + e.getMessage(), e);
									}
									updateComponents();
								}
							}));
					buttonPanel.add(downloadButton);

					tableModel.setValueAt(buttonPanel, row, installedCol);
				}
			}

			if (isUsed(driverClassName)) {
				tableModel.setValueAt(validIcon, row, 4);
			}

			row++;
		}

		table.setRowHeight(IconUtils.ICON_SIZE_SMALL + 4);
		table.getColumn(0).setMaxWidth(IconUtils.ICON_SIZE_SMALL + 4);
		table.getColumn(3).setMaxWidth(84);
		table.getColumn(4).setMaxWidth(70);
		table.setColumnControlVisible(false);
		return table;
	}

	private boolean isUsed(String driverClassName) {
		return _usedDriverClassNames.contains(driverClassName);
	}
}
