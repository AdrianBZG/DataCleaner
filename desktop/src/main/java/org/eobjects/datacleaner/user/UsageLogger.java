/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.datacleaner.Version;
import org.eobjects.datacleaner.util.SystemProperties;
import org.eobjects.metamodel.util.SharedExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handles remote logging of usage data
 */
public final class UsageLogger {

    // Special username used for anonymous entries. This is the only
    // non-existing username that is allowed on server side.
    private static final String NOT_LOGGED_IN_USERNAME = "[not-logged-in]";
    
    private static final Logger logger = LoggerFactory.getLogger(UsageLogger.class);

    private final Charset charset = Charset.forName("UTF-8");
    private final UserPreferences _userPreferences;
    private final ExecutorService _executorService;

    private final String _javaVersion;
    private final String _osName;
    private final String _osArch;
    private final String _country;
    private final String _language;
    private final String _javaVendor;

    @Inject
    protected UsageLogger(UserPreferences userPreferences) {
        _userPreferences = userPreferences;
        _executorService = SharedExecutorService.get();

        _javaVendor = System.getProperty("java.vendor");
        _javaVersion = System.getProperty("java.version");
        _osName = System.getProperty("os.name");
        _osArch = System.getProperty("os.arch");

        final Locale defaultLocale = Locale.getDefault();
        _country = defaultLocale.getCountry();
        _language = defaultLocale.getLanguage();

        logger.debug(
                "Determined installation details as:\nJava version: {}\nJava vendor: {}\nOS name: {}\nOS arch: {}\nUser country: {}\nUser language: {}",
                _javaVersion, _javaVendor, _osName, _osArch, _country, _language);
    }

    public void logApplicationStartup() {
        final String embeddedClient = System.getProperty(SystemProperties.EMBED_CLIENT);
        log("Startup", embeddedClient);
    }

    public void logApplicationShutdown() {
        log("Shutdown", null);

        // order the executor service to shut down.
        _executorService.shutdown();
    }

    private void log(final String action, final String detail) {
        logger.debug("Logging action='{}', detail='{}'", action, detail);
        final Runnable runnable = new UsageLoggerRunnable(action, detail);
        _executorService.submit(runnable);
    }

    /**
     * Runnable implementation that does the actual remote notification. This is
     * executed in a separate thread to avoid waiting for the user.
     */
    private final class UsageLoggerRunnable implements Runnable {

        private final String _action;
        private final String _detail;

        public UsageLoggerRunnable(final String action, final String detail) {
            _action = action;
            _detail = detail;
        }

        @Override
        public void run() {
            try {
                final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                final HttpPost req = new HttpPost("http://datacleaner.org/ws/user_action");
                nameValuePairs.add(new BasicNameValuePair("username", NOT_LOGGED_IN_USERNAME));
                nameValuePairs.add(new BasicNameValuePair("action", _action));
                nameValuePairs.add(new BasicNameValuePair("detail", _detail));
                nameValuePairs.add(new BasicNameValuePair("version", Version.getVersion()));
                nameValuePairs.add(new BasicNameValuePair("edition", Version.getEdition()));
                nameValuePairs.add(new BasicNameValuePair("os_name", _osName));
                nameValuePairs.add(new BasicNameValuePair("os_arch", _osArch));
                nameValuePairs.add(new BasicNameValuePair("country", _country));
                nameValuePairs.add(new BasicNameValuePair("language", _language));
                nameValuePairs.add(new BasicNameValuePair("java_version", _javaVersion));
                nameValuePairs.add(new BasicNameValuePair("java_vendor", _javaVendor));
                req.setEntity(new UrlEncodedFormEntity(nameValuePairs, charset));

                HttpResponse resp = _userPreferences.createHttpClient().execute(req);
                InputStream content = resp.getEntity().getContent();
                String line = new BufferedReader(new InputStreamReader(content)).readLine();
                assert "success".equals(line);
                logger.debug("Usage logger response: {}", line);
            } catch (Exception e) {
                logger.warn("Could not dispatch usage log for action: {} ({})", _action, e.getMessage());
                logger.debug("Error occurred while dispatching usage log", e);
            }
        }
    }

    public void logComponentUsage(ComponentDescriptor<?> descriptor) {
        log("Add component", descriptor.getDisplayName());
    }
}
