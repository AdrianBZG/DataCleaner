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
package org.eobjects.datacleaner.monitor.scheduling.quartz;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.NoSuchDatastoreException;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.PlaceholderDatastore;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

/**
 * Quartz job which encapsulates the process of executing a DataCleaner job and
 * writes the result to the repository.
 */
public class ExecuteJob extends AbstractQuartzJob {

    public static final String DETAIL_SCHEDULE_DEFINITION = "DataCleaner.schedule.definition";

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.debug("executeInternal({})", jobExecutionContext);

        final ApplicationContext applicationContext = getApplicationContext(jobExecutionContext);

        final TenantContextFactory contextFactory = applicationContext.getBean(TenantContextFactory.class);

        final JobDetail jobDetail = jobExecutionContext.getJobDetail();
        final ScheduleDefinition schedule = (ScheduleDefinition) jobDetail.getJobDataMap().get(
                DETAIL_SCHEDULE_DEFINITION);
        if (schedule == null) {
            throw new IllegalArgumentException("No schedule definition defined");
        }

        final String tenantId = schedule.getTenant().getId();
        logger.info("Tenant {} executing job {}", tenantId, schedule.getJob());

        final TenantContext context = contextFactory.getContext(tenantId);

        final ExecutionLog execution = new ExecutionLog(schedule, schedule.getTriggerType());

        executeJob(context, execution);
    }

    /**
     * Executes a DataCleaner job in the repository and stores the result
     * 
     * @param context
     *            the tenant's {@link TenantContext}
     * @param execution
     *            the execution log object
     * 
     * @return The expected result name, which can be used to get updates about
     *         execution status etc. at a later state.
     */
    public static String executeJob(TenantContext context, ExecutionLog execution) {
        final RepositoryFolder resultFolder = context.getResultFolder();
        final AnalysisListener analysisListener = new MonitorAnalysisListener(execution, resultFolder);

        try {
            final String jobName = execution.getJob().getName();

            final JobContext job = context.getJob(jobName);

            final AnalysisJob analysisJob = job.getAnalysisJob();

            if (analysisJob.getDatastore() instanceof PlaceholderDatastore) {
                // the job was materialized using a placeholder datastore - ie.
                // the real datastore was not found!
                throw new NoSuchDatastoreException(job.getSourceDatastoreName());
            }

            final AnalyzerBeansConfiguration configuration = context.getConfiguration();
            final AnalysisRunner runner = new AnalysisRunnerImpl(configuration, analysisListener);

            // fire and forget (the listener will do the rest)
            runner.run(analysisJob);
        } catch (Throwable e) {

            // only initialization issues are catched here, eg. failing to load
            // job or configuration. Other issues will be reported to the
            // listener by the runner.
            analysisListener.errorUknown(null, e);
        }

        // TODO: Check alerts

        return execution.getResultId();
    }
}
