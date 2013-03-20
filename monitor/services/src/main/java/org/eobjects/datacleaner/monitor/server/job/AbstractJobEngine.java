package org.eobjects.datacleaner.monitor.server.job;

import java.util.List;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.HasNameMapper;

/**
 * Abstract helper implementation of some of the {@link JobEngine} methods
 */
public abstract class AbstractJobEngine<T extends JobContext> implements JobEngine<T> {

    private final String _fileExtension;

    public AbstractJobEngine(String fileExtension) {
        _fileExtension = fileExtension;
    }

    /**
     * Utility method to get the filename of a job.
     * 
     * @param jobName
     * @return
     */
    protected final String getJobFilename(String jobName) {
        if (!jobName.endsWith(_fileExtension)) {
            jobName = jobName + _fileExtension;
        }
        return jobName;
    }

    @Override
    public T getJobContext(TenantContext tenantContext, String jobName) {
        final String jobFilename = getJobFilename(jobName);
        final RepositoryFile file = tenantContext.getJobFolder().getFile(jobFilename);
        if (file == null) {
            throw new IllegalArgumentException("No such job: " + jobName);
        }
        return getJobContext(tenantContext, file);
    }

    protected abstract T getJobContext(TenantContext tenantContext, RepositoryFile file);

    @Override
    public final List<String> getJobNames(TenantContext tenantContext) {
        final RepositoryFolder jobsFolder = tenantContext.getJobFolder();
        final List<RepositoryFile> files = jobsFolder.getFiles(null, _fileExtension);
        final List<String> filenames = CollectionUtils.map(files, new HasNameMapper());
        final List<String> jobNames = CollectionUtils.map(filenames, new Func<String, String>() {
            @Override
            public String eval(String filename) {
                String jobName = filename.substring(0, filename.length() - _fileExtension.length());
                return jobName;
            }
        });
        return jobNames;
    }

    @Override
    public final boolean containsJob(TenantContext tenantContext, String jobName) {
        if (!jobName.endsWith(_fileExtension)) {
            jobName = jobName + _fileExtension;
        }
        final RepositoryFile file = tenantContext.getJobFolder().getFile(jobName);
        if (file == null) {
            return false;
        }
        return true;
    }
}
