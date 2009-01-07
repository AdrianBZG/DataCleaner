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
package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.commons.lang.ArrayUtils;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.NamedConnection;
import dk.eobjects.datacleaner.gui.setup.GuiConfiguration;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.schema.TableType;

public class OpenDatabaseDialog extends BanneredDialog {

	private static final int FIELD_COLUMNS = 22;
	private static final long serialVersionUID = 3900489965164055702L;
	private DataContextSelection _dataContextSelection;
	private JComboBox _nameComboBox;
	private JTextField _connectionStringField;
	private JTextField _usernameField;
	private JPasswordField _passwordField;
	private JTextField _catalogField;
	private JCheckBox _typeTableCheckBox;
	private JCheckBox _typeViewCheckBox;

	@Override
	public void dispose() {
		super.dispose();
		_dataContextSelection = null;
	}

	public OpenDatabaseDialog(DataContextSelection dataContextSelection) {
		super();
		setModal(true);
		_dataContextSelection = dataContextSelection;
		setSize(400, 520);

		JTextArea aboutDatabases = GuiHelper.createLabelTextArea()
				.toComponent();
		aboutDatabases
				.setText("The named connections are pre-configured database connections that have been stored for easy reuse. To add or modify the named connections edit the '"
						+ GuiConfiguration.CONFIGURATION_FILE
						+ "' file in the root of the DataCleaner installation.");
		add(aboutDatabases, BorderLayout.SOUTH);
	}

	@Override
	protected Component getContent() {
		JPanel panel = GuiHelper.createPanel().toComponent();
		GuiHelper
				.addToGridBag(
						new JLabel(
								"Please fill out the form below to connect to your database..."),
						panel, 0, 0, 2, 1);
		GuiHelper.addToGridBag(new JLabel("Named connection:"), panel, 0, 1, 1,
				1);
		GuiHelper.addToGridBag(new JLabel("Connection string:"), panel, 0, 2,
				1, 1);
		GuiHelper.addToGridBag(new JLabel("Username:"), panel, 0, 3, 1, 1);
		GuiHelper.addToGridBag(new JLabel("Password:"), panel, 0, 4, 1, 1);
		GuiHelper.addToGridBag(new JLabel("Catalog:"), panel, 0, 5, 1, 1);
		GuiHelper.addToGridBag(new JLabel("Table types:"), panel, 0, 6, 1, 1);

		_connectionStringField = new JTextField(FIELD_COLUMNS);
		_connectionStringField.setName("connectionStringField");
		_connectionStringField.getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "nextTemplateItem");
		_connectionStringField.getActionMap().put("nextTemplateItem",
				getNextTemplateItemAction());
		GuiHelper.addToGridBag(_connectionStringField, panel, 1, 2, 1, 1);

		final JButton samplesButton = new JButton(GuiHelper
				.getImageIcon("images/hint.png"));
		samplesButton
				.setToolTipText("Click to select among configuration samples.");
		samplesButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JPopupMenu popup = new JPopupMenu("Samples");
				popup.add(sampleItem("MySQL sample",
						"jdbc:mysql://<hostname>:3306/<database>",
						"images/database_mysql.png"));
				popup.add(sampleItem("PostgreSQL sample",
						"jdbc:postgresql://<hostname>:5432/<database>",
						"images/database_postgresql.png"));
				popup.add(sampleItem("Oracle sample",
						"jdbc:oracle:thin:@<hostname>:1521:<schema>",
						"images/database_oracle.png"));
				popup.add(sampleItem("SQL Server sample",
						"jdbc:jtds:sqlserver://<hostname>:1434/<database>",
						"images/database_sqlserver.png"));
				popup.add(sampleItem("Firebird sample",
						"jdbc:firebirdsql:<hostname>:<path/to/database.fdb>",
						"images/database_firebird.png"));
				popup.add(sampleItem("Derby sample",
						"jdbc:derby://<hostname>:1527/<path/to/database>",
						"images/database_derby.png"));
				popup.add(sampleItem("SQLite sample",
						"jdbc:sqlite:<path/to/database.db>",
						"images/database_sqlite.png"));
				popup.add(sampleItem("Ingres sample",
						"jdbc:ingres://<hostname>:II7/<database>",
						"images/database_ingres.png"));
				popup.show(samplesButton, 0, samplesButton.getHeight());
			}

			private JMenuItem sampleItem(String title,
					final String connectionString, String iconPath) {
				JMenuItem item = new JMenuItem(title, GuiHelper
						.getImageIcon(iconPath));
				item.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						_connectionStringField.setFocusTraversalKeysEnabled(false);
						_connectionStringField.setText(connectionString);
						_connectionStringField
								.setSelectionStart(connectionString
										.indexOf('<'));
						_connectionStringField.setSelectionEnd(connectionString
								.indexOf('>') + 1);
						_connectionStringField.requestFocus();
					}
				});
				return item;
			}
		});
		GuiHelper.addToGridBag(samplesButton, panel, 2, 2, 1, 1);

		_usernameField = new JTextField(FIELD_COLUMNS);
		GuiHelper.addToGridBag(_usernameField, panel, 1, 3, 1, 1);

		_passwordField = new JPasswordField(FIELD_COLUMNS);
		GuiHelper.addToGridBag(_passwordField, panel, 1, 4, 1, 1);

		_catalogField = new JTextField(FIELD_COLUMNS);
		GuiHelper.addToGridBag(_catalogField, panel, 1, 5, 1, 1);

		JPanel tableTypePanel = GuiHelper.createPanel().applyVerticalLayout()
				.toComponent();

		_typeTableCheckBox = GuiHelper.createCheckBox("Table", true)
				.toComponent();
		tableTypePanel.add(_typeTableCheckBox);
		_typeViewCheckBox = GuiHelper.createCheckBox("View", false)
				.toComponent();
		tableTypePanel.add(_typeViewCheckBox);

		GuiHelper.addToGridBag(tableTypePanel, panel, 1, 6, 1, 1);

		final NamedConnection[] namedConnections = GuiConfiguration
				.getBeansOfClass(NamedConnection.class).toArray(
						new NamedConnection[0]);
		final Object[] connectionNames = ReflectionHelper.getProperties(
				namedConnections, "name");

		_nameComboBox = new JComboBox(connectionNames);
		_nameComboBox.setEditable(false);
		_nameComboBox.setName("Named connection");
		_nameComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Object selectedItem = _nameComboBox.getSelectedItem();
				for (int i = 0; i < connectionNames.length; i++) {
					if (connectionNames[i].equals(selectedItem)) {
						updateForm(namedConnections[i]);
						break;
					}
				}
			}
		});
		GuiHelper.addToGridBag(_nameComboBox, panel, 1, 1, 1, 1);

		JButton connectButton = new JButton();
		connectButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				String connectionString = _connectionStringField.getText();
				String password = new String(_passwordField.getPassword());
				String username = _usernameField.getText();
				if ("".equals(username) && "".equals(password)) {
					username = null;
					password = null;
				}
				String catalog = _catalogField.getText();
				if (catalog != null && catalog.length() == 0) {
					catalog = null;
				}
				List<TableType> types = new ArrayList<TableType>();
				if (_typeTableCheckBox.isSelected()) {
					types.add(TableType.TABLE);
				}
				if (_typeViewCheckBox.isSelected()) {
					types.add(TableType.VIEW);
				}

				try {
					_dataContextSelection.selectDatabase(connectionString,
							catalog, username, password, types
									.toArray(new TableType[types.size()]));
					setVisible(false);
					dispose();
				} catch (SQLException e) {
					GuiHelper
							.showErrorMessage(
									"Could not open connection",
									"An error occurred while trying to open connection to the database. Make sure that you've installed the database driver and that the connection string and credentials are valid.",
									e);
				}
			}

		});
		connectButton.setText("Connect to database");
		connectButton.setIcon(GuiHelper
				.getImageIcon("images/toolbar_database.png"));
		GuiHelper.addToGridBag(connectButton, panel, 0, 8, 2, 1);

		if (namedConnections.length > 0) {
			updateForm(namedConnections[0]);
		}

		return panel;
	}

	private Action getNextTemplateItemAction() {
		return new AbstractAction() {
			private static final long serialVersionUID = -953964742803082696L;

			public void actionPerformed(ActionEvent e) {
				String text = _connectionStringField.getText();
				int selectionEnd = _connectionStringField.getSelectionEnd();
				int selectionStart = text.indexOf('<', selectionEnd);
				if (selectionStart != -1) {
					selectionEnd = text.indexOf('>', selectionStart);
				}

				if (selectionStart != -1 && selectionEnd != -1) {
					_connectionStringField.setSelectionStart(selectionStart);
					_connectionStringField.setSelectionEnd(selectionEnd + 1);
					_connectionStringField.requestFocus();
				} else {
					selectionStart = text.indexOf('<');
					if (selectionStart != -1) {
						selectionEnd = text.indexOf('>', selectionStart);
						if (selectionEnd != -1) {
							_connectionStringField
									.setSelectionStart(selectionStart);
							_connectionStringField
									.setSelectionEnd(selectionEnd + 1);
							_connectionStringField.requestFocus();
						}
					}
				}
			}
		};
	}

	private void updateForm(NamedConnection namedConnection) {
		if (namedConnection != null) {
			_connectionStringField.setText(namedConnection
					.getConnectionString());
			_usernameField.setText(namedConnection.getUsername());
			_passwordField.setText(namedConnection.getPassword());
			_catalogField.setText(namedConnection.getCatalog());
			String[] tableTypes = namedConnection.getTableTypes();
			if (tableTypes != null && tableTypes.length > 0) {
				_typeTableCheckBox.setSelected(ArrayUtils.indexOf(tableTypes,
						"TABLE") != -1);
				_typeViewCheckBox.setSelected(ArrayUtils.indexOf(tableTypes,
						"VIEW") != -1);
			} else {
				_typeTableCheckBox.setEnabled(true);
				_typeViewCheckBox.setEnabled(false);
			}
		}
	}

	@Override
	protected String getDialogTitle() {
		return "Open database";
	}

}