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
package dk.eobjects.datacleaner.gui.website;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdesktop.swingx.action.OpenBrowserAction;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.dialogs.BanneredDialog;
import dk.eobjects.datacleaner.gui.dialogs.NewTaskDialog;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.thirdparty.jcrypt.Jcrypt;

public class UserRegistrationDialog extends BanneredDialog {

	private static final long serialVersionUID = -7029803817618552617L;
	private JTextField _usernameField;
	private JPasswordField _passwordField;

	public UserRegistrationDialog() {
		super(420, 375);
	}

	@Override
	protected String getDialogTitle() {
		return "DataCleaner user registration";
	}

	@Override
	protected Component getContent() {
		JPanel panel = GuiHelper.createPanel().applyBorderLayout()
				.toComponent();
		panel.setBorder(GuiHelper.BORDER_WIDE);
		JPanel loginPanel = GuiHelper.createPanel().toComponent();

		GuiHelper.addToGridBag(new JLabel("Username:"), loginPanel, 0, 0);

		_usernameField = new JTextField(18);
		GuiHelper.addToGridBag(_usernameField, loginPanel, 1, 0);

		GuiHelper.addToGridBag(new JLabel("Password:"), loginPanel, 0, 1);

		_passwordField = new JPasswordField(18);
		GuiHelper.addToGridBag(_passwordField, loginPanel, 1, 1);

		JButton registerButton = GuiHelper.createButton("Register",
				"images/toolbar_login.png").toComponent();
		try {
			registerButton.addActionListener(new OpenBrowserAction(
					"http://datacleaner.eobjects.org/?register"));
		} catch (MalformedURLException e) {
			_log.error(e);
		}

		JButton loginButton = GuiHelper.createButton("Login",
				"images/toolbar_login.png").toComponent();
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new Thread() {
					@Override
					public void run() {
						boolean validated = false;
						String username = _usernameField.getText();

						_log
								.info("Querying http://datacleaner.eobjects.org/ws/get_salt for salt.");
						Map<String, String> params = new HashMap<String, String>();
						params.put("username", username);
						String salt = getUrlContent(
								"http://datacleaner.eobjects.org/ws/get_salt",
								params);

						if (salt != null && !"not found".equals(salt)) {
							String hashedPassword = Jcrypt.crypt(salt,
									new String(_passwordField.getPassword()));

							params.put("hashed_password", hashedPassword);
							_log.info("Querying for user credentials.");
							String accepted = getUrlContent(
									"http://datacleaner.eobjects.org/ws/login",
									params);

							if ("true".equals(accepted)) {
								validated = true;
							}
						}

						if (validated) {
							GuiSettings settings = GuiSettings.getSettings();
							settings.setUsername(username);
							GuiSettings.saveSettings(settings);
							dispose();
							new NewTaskDialog().setVisible(true);
						} else {
							GuiHelper
									.showErrorMessage(
											"Invalid username and password",
											"Invalid username and password, please try again.",
											null);
						}
					}
				}.start();
			}
		});

		JPanel buttonPanel = GuiHelper.createPanel().toComponent();
		buttonPanel.add(registerButton);
		buttonPanel.add(loginButton);

		GuiHelper.addToGridBag(buttonPanel, loginPanel, 0, 2, 2, 1);

		JButton skipButton = GuiHelper.createButton("Skip registration",
				"images/toolbar_skip.png").toComponent();
		skipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				new NewTaskDialog().setVisible(true);
			}
		});

		JPanel leftPanel = GuiHelper.createPanel().toComponent();
		leftPanel.add(new JLabel(GuiHelper
				.getImageIcon("images/toolbar_visit_website_large.png")));

		JToolBar toolBar = GuiHelper.createToolBar();
		toolBar.add(new JSeparator(JSeparator.VERTICAL));
		toolBar.add(skipButton);

		JTextArea aboutUserRegistrationLabel = GuiHelper.createLabelTextArea()
				.toComponent();
		aboutUserRegistrationLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		aboutUserRegistrationLabel
				.setText("Thank you for using DataCleaner. We kindly ask you to identify yourself by "
						+ "providing us with your eobjects.org user credentials.\n"
						+ "If you are not registered yet, we hope that you will do so now, giving "
						+ "the DataCleaner development community a better sense of it's users and audience.");
		panel.add(aboutUserRegistrationLabel, BorderLayout.NORTH);

		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(toolBar, BorderLayout.SOUTH);
		panel.add(loginPanel, BorderLayout.CENTER);

		return panel;
	}

	private String getUrlContent(String url, Map<String, String> params) {
		PostMethod method = new PostMethod(url);
		for (Entry<String, String> entry : params.entrySet()) {
			method.addParameter(entry.getKey(), entry.getValue());
		}
		try {
			GuiHelper.getHttpClient().executeMethod(method);
			return method.getResponseBodyAsString();
		} catch (HttpException e) {
			_log.error(e);
			throw new IllegalArgumentException(url);
		} catch (IOException e) {
			_log.warn(e);
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
					"Could not establish connection.\nError type: "
							+ e.getClass().getSimpleName() + "\nError message: "
							+ e.getMessage() + "\n\nRetry?",
					"Connection error", JOptionPane.YES_NO_OPTION)) {
				_log.info("Retrying...");
				return getUrlContent(url, params);
			} else {
				return null;
			}
		}
	}
}