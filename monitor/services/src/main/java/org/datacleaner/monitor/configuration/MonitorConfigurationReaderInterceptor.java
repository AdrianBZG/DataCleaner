package org.datacleaner.monitor.configuration;

import java.io.File;
import java.util.List;

import org.datacleaner.configuration.ConfigurationReaderInterceptor;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DefaultConfigurationReaderInterceptor;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.util.convert.RepositoryFileResourceTypeHandler;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigurationReaderInterceptor} with overrides suitable for the
 * DataCleaner monitor webapp.
 */
public class MonitorConfigurationReaderInterceptor extends DefaultConfigurationReaderInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MonitorConfigurationReaderInterceptor.class);

    private final TenantContext _tenantContext;
    private final DataCleanerEnvironment _environment;
    private final Repository _repository;

    public MonitorConfigurationReaderInterceptor(Repository repository, TenantContext tenantContext,
            DataCleanerEnvironment environment) {
        _repository = repository;
        _tenantContext = tenantContext;
        _environment = environment;
    }

    @Override
    protected File getRelativeParentDirectory() {
        final File relativeParentDirectory = super.getRelativeParentDirectory();
        if (relativeParentDirectory == null) {
            final String userHome = System.getProperty("user.home");
            final String result = userHome + File.separator + ".datacleaner/repository/" + _tenantContext.getTenantId();
            return new File(result);
        }
        return relativeParentDirectory;
    }

    @Override
    public String createFilename(String filename) {
        if (isAbsolute(filename)) {
            return super.createFilename(filename);
        }

        if (_tenantContext.getTenantRootFolder() instanceof FileRepositoryFolder) {
            // for FileRepository implementations, the super
            // implementation will also "just work" because of the above
            // getRelativeParentDirectory method.
            return super.createFilename(filename);
        }

        final String userHome = System.getProperty("user.home");
        final String result = userHome + File.separator + ".datacleaner/repository/" + _tenantContext.getTenantId()
                + File.separator + filename;

        logger.warn("File path is relative, but repository is not file-based: {}. Returning: {}", filename, result);

        return result;
    }

    @Override
    protected List<ResourceTypeHandler<?>> getResourceTypeHandlers() {
        List<ResourceTypeHandler<?>> handlers = super.getResourceTypeHandlers();
        handlers.add(new RepositoryFileResourceTypeHandler(_repository, _tenantContext.getTenantId()));
        return handlers;
    }

    @Override
    public DataCleanerEnvironment createBaseEnvironment() {
        return _environment;
    }

    @Override
    public RepositoryFolder getHomeFolder() {
        return _tenantContext.getTenantRootFolder();
    }

    private boolean isAbsolute(String filename) {
        assert filename != null;

        File file = new File(filename);
        return file.isAbsolute();
    }
}
