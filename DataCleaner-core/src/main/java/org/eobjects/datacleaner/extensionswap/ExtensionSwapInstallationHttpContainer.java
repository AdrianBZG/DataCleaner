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
package org.eobjects.datacleaner.extensionswap;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowManager;
import org.eobjects.metamodel.util.FileHelper;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionSwapInstallationHttpContainer implements Container {

	private static final Logger logger = LoggerFactory.getLogger(ExtensionSwapInstallationHttpContainer.class);

	private final ExtensionSwapClient _client;

	public ExtensionSwapInstallationHttpContainer(WindowManager windowManager) {
		this(new ExtensionSwapClient(windowManager));
	}

	public ExtensionSwapInstallationHttpContainer(ExtensionSwapClient extensionSwapClient) {
		_client = extensionSwapClient;
	}

	@Override
	public void handle(Request req, Response resp) {
		PrintStream out = null;
		try {
			out = resp.getPrintStream();
			final String extensionId = req.getParameter("extensionId");
			if (extensionId == null) {
				throw new IllegalArgumentException("extensionId cannot be null");
			}
			
			final String callback = req.getParameter("callback");

			logger.info("Initiating transfer of extension: {}", extensionId);

			final ExtensionSwapPackage extensionSwapPackage = _client.getExtensionSwapPackage(extensionId);
			logger.info("Fetched ExtensionSwap package: {}", extensionSwapPackage);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					int confirmation = JOptionPane.showConfirmDialog(null,
							"Do you want to download and install the extension '" + extensionSwapPackage.getName() + "'");

					if (confirmation == JOptionPane.YES_OPTION) {
						_client.registerExtensionPackage(extensionSwapPackage);
					}
				}
			});
			
			if (callback != null) {
				out.print(callback + "({\"success\":true})");
			}

			resp.setCode(200);
		} catch (IOException e) {
			logger.error("IOException occurred while processing HTTP request", e);
			resp.setCode(400);
		} finally {
			FileHelper.safeClose(out);
		}
	}

	/**
	 * Example main method used for testing
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame("ExtensionSwap dummy server");
		frame.setVisible(true);
		
		ExtensionSwapClient client = new ExtensionSwapClient("localhost:8000", new DCWindowContext());
		ExtensionSwapInstallationHttpContainer container = new ExtensionSwapInstallationHttpContainer(client);
		Connection connection = new SocketConnection(container);
		SocketAddress address = new InetSocketAddress(31389);

		connection.connect(address);
		System.out.println("Ready!");
	}
}
