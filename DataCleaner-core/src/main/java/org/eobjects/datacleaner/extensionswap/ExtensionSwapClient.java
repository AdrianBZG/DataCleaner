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

import java.io.File;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.DownloadFilesActionListener;
import org.eobjects.datacleaner.actions.FileDownloadListener;
import org.eobjects.datacleaner.bootstrap.WindowManager;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.ExtensionPackage;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.HttpXmlUtils;
import org.w3c.dom.Element;

public final class ExtensionSwapClient {

	public static final String DEFAULT_WEBSITE_HOSTNAME = "datacleaner.eobjects.org";

	private static final String EXTENSIONSWAP_ID_PROPERTY = "extensionswap.id";
	private static final String EXTENSIONSWAP_VERSION_PROPERTY = "extensionswap.version";

	private final HttpClient _httpClient;
	private final WindowManager _windowManager;
	private final String _baseUrl;

	public ExtensionSwapClient(WindowManager windowManager) {
		this(DEFAULT_WEBSITE_HOSTNAME, windowManager);
	}

	public ExtensionSwapClient(String websiteHostname, WindowManager windowManager) {
		this(HttpXmlUtils.getHttpClient(), websiteHostname, windowManager);
	}

	public ExtensionSwapClient(HttpClient httpClient, String websiteHostname, WindowManager windowManager) {
		_httpClient = httpClient;
		_windowManager = windowManager;
		_baseUrl = "http://" + websiteHostname + "/ws/extension/";
	}

	public ExtensionPackage registerExtensionPackage(ExtensionSwapPackage extensionSwapPackage, File jarFile) {
		String packageName = ExtensionPackage.autoDetectPackageName(jarFile);
		ExtensionPackage extensionPackage = new ExtensionPackage(extensionSwapPackage.getName(), packageName, true,
				new File[] { jarFile });
		extensionPackage.getAdditionalProperties().put(EXTENSIONSWAP_ID_PROPERTY, extensionSwapPackage.getId());
		extensionPackage.getAdditionalProperties().put(EXTENSIONSWAP_VERSION_PROPERTY,
				Integer.toString(extensionSwapPackage.getVersion()));
		extensionPackage.loadExtension(DCConfiguration.get().getDescriptorProvider());
		UserPreferences.getInstance().getExtensionPackages().add(extensionPackage);
		return extensionPackage;
	}

	public ExtensionSwapPackage getExtensionSwapPackage(String id) {
		final Element rootNode = HttpXmlUtils.getRootNode(_httpClient, _baseUrl + id);
		final String name = HttpXmlUtils.getChildNodeText(rootNode, "name");
		final int version = Integer.parseInt(HttpXmlUtils.getChildNodeText(rootNode, "version"));
		return new ExtensionSwapPackage(id, version, name);
	}

	public void registerExtensionPackage(final ExtensionSwapPackage extensionSwapPackage, final String username) {
		downloadJarFile(extensionSwapPackage, username, new FileDownloadListener() {
			@Override
			public void onFilesDownloaded(File[] files) {
				File jarFile = files[0];
				registerExtensionPackage(extensionSwapPackage, jarFile);
			}
		});
	}

	private void downloadJarFile(ExtensionSwapPackage extensionSwapPackage, String username, FileDownloadListener listener) {
		String url = _baseUrl + extensionSwapPackage.getId() + "/jarfile";
		if (!StringUtils.isNullOrEmpty(username)) {
			url = url + "?username=" + username;
		}
		String filename = extensionSwapPackage.getId() + ".jar";
		DownloadFilesActionListener actionListener = new DownloadFilesActionListener(new String[] { url },
				new String[] { filename }, listener, _windowManager);
		actionListener.actionPerformed(null);
	}

	public boolean isInstalled(ExtensionSwapPackage extensionSwapPackage) {
		List<ExtensionPackage> extensionPackages = UserPreferences.getInstance().getExtensionPackages();
		for (ExtensionPackage extensionPackage : extensionPackages) {
			String id = extensionPackage.getAdditionalProperties().get(EXTENSIONSWAP_ID_PROPERTY);
			if (extensionSwapPackage.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}
}
