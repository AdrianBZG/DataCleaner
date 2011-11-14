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
package org.eobjects.datacleaner.windows;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPasswordField;

import org.eobjects.analyzer.connection.MongoDbDatastore;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXTextField;

public class MongoDbDatastoreDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final MutableDatastoreCatalog _catalog;
	private final MongoDbDatastore _originalDatastore;

	private final JXTextField _hostnameTextField;
	private final JXTextField _portTextField;
	private final JXTextField _databaseNameTextField;
	private final JXTextField _usernameTextField;
	private final JPasswordField _passwordField;
	private final JXTextField _datastoreNameTextField;

	@Inject
	public MongoDbDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog catalog,
			@Nullable MongoDbDatastore datastore) {
		super(windowContext, imageManager.getImage("images/window/banner-datastores.png"));
		_catalog = catalog;
		_originalDatastore = datastore;

		_datastoreNameTextField = WidgetFactory.createTextField();
		_hostnameTextField = WidgetFactory.createTextField();
		_portTextField = WidgetFactory.createTextField();
		_portTextField.setDocument(new NumberDocument(false));
		_databaseNameTextField = WidgetFactory.createTextField();
		_usernameTextField = WidgetFactory.createTextField();
		_passwordField = new JPasswordField(17);

		if (_originalDatastore == null) {
			_hostnameTextField.setText("localhost");
			_portTextField.setText("27017");
		} else {
			_datastoreNameTextField.setText(_originalDatastore.getName());
			_datastoreNameTextField.setEnabled(false);
			_hostnameTextField.setText(_originalDatastore.getHostname());
			_portTextField.setText(_originalDatastore.getPort() + "");
			_databaseNameTextField.setText(_originalDatastore.getDatabaseName());
			_usernameTextField.setText(_originalDatastore.getUsername());
			_passwordField.setText(new String(_originalDatastore.getPassword()));
		}
	}

	@Override
	public String getWindowTitle() {
		return "MongoDB database";
	}

	@Override
	protected String getBannerTitle() {
		return "MongoDB database";
	}

	@Override
	protected int getDialogWidth() {
		return 400;
	}

	@Override
	protected JComponent getDialogContent() {
		final DCPanel panel = new DCPanel();
		int row = 0;
		WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), panel, 0, row);
		WidgetUtils.addToGridBag(_datastoreNameTextField, panel, 1, row);
		row++;
		
		WidgetUtils.addToGridBag(DCLabel.bright("Hostname:"), panel, 0, row);
		WidgetUtils.addToGridBag(_hostnameTextField, panel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Port:"), panel, 0, row);
		WidgetUtils.addToGridBag(_portTextField, panel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Database name:"), panel, 0, row);
		WidgetUtils.addToGridBag(_databaseNameTextField, panel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Username:"), panel, 0, row);
		WidgetUtils.addToGridBag(_usernameTextField, panel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Password:"), panel, 0, row);
		WidgetUtils.addToGridBag(_passwordField, panel, 1, row);
		row++;

		final JButton saveButton = WidgetFactory.createButton("Save datastore", "images/model/datastore.png");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MongoDbDatastore datastore = createDatastore();

				if (_originalDatastore != null) {
					_catalog.removeDatastore(_originalDatastore);
				}
				_catalog.addDatastore(datastore);
				MongoDbDatastoreDialog.this.dispose();
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		buttonPanel.add(saveButton);

		WidgetUtils.addToGridBag(buttonPanel, panel, 1, row, 2, 1);
		return panel;
	}

	protected MongoDbDatastore createDatastore() {
		String name = _datastoreNameTextField.getText();
		String hostname = _hostnameTextField.getText();
		Integer port = Integer.parseInt(_portTextField.getText());
		String databaseName = _databaseNameTextField.getText();
		String username = _usernameTextField.getText();
		char[] password = _passwordField.getPassword();
		return new MongoDbDatastore(name, hostname, port, databaseName, username, password);
	}
}