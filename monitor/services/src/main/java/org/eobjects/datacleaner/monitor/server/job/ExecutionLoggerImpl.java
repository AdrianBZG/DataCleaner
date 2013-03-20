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
package org.eobjects.datacleaner.monitor.server.job;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.events.JobExecutedEvent;
import org.eobjects.datacleaner.monitor.events.JobFailedEvent;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.server.jaxb.JaxbExecutionLogWriter;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Default implementation of the {@link ExecutionLogger} interface. Writes log
 * to {@link FileFilters#ANALYSIS_EXECUTION_LOG_XML} files in the result folder
 * of the tenant.
 */
public class ExecutionLoggerImpl implements ExecutionLogger {

    private final ApplicationEventPublisher _eventPublisher;
    private final ExecutionLog _execution;
    private final StringBuilder _log;
    private final JaxbExecutionLogWriter _executionLogWriter;
    private final RepositoryFile _logFile;

    public ExecutionLoggerImpl(ExecutionLog execution, RepositoryFolder resultFolder,
            ApplicationEventPublisher eventPublisher) {
        _execution = execution;
        _eventPublisher = eventPublisher;
        _executionLogWriter = new JaxbExecutionLogWriter();

        _log = new StringBuilder();

        final String resultId = execution.getResultId();
        final String logFilename = resultId + FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension();
        _logFile = resultFolder.createFile(logFilename, new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                _executionLogWriter.write(_execution, out);
            }
        });
    }

    @Override
    public void setStatusRunning() {
        _execution.setExecutionStatus(ExecutionStatus.RUNNING);
        _execution.setJobBeginDate(new Date());

        log("Job execution BEGIN");
    }

    @Override
    public void setStatusFailed(Object component, Object data, Throwable throwable) {
        _execution.setJobEndDate(new Date());
        _execution.setExecutionStatus(ExecutionStatus.FAILURE);

        final StringWriter stringWriter = new StringWriter();
        stringWriter.write("Job execution FAILURE");

        if (throwable != null && !StringUtils.isNullOrEmpty(throwable.getMessage())) {
            stringWriter.write("\n - ");
            stringWriter.write(throwable.getMessage());
            stringWriter.write(" (");
            stringWriter.write(throwable.getClass().getSimpleName());
            stringWriter.write(")");
        }

        if (component != null) {
            stringWriter.write('\n');
            stringWriter.write(" - Failure component: " + component);
        }

        if (data != null) {
            stringWriter.write('\n');
            stringWriter.write(" - Failure input data: " + data);
        }

        if (throwable != null) {
            stringWriter.write('\n');
            stringWriter.write(" - Exception stacktrace of failure condition:");
            stringWriter.write('\n');
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
        }

        stringWriter.write("\nCheck the server logs for more details, warnings and debug information.");

        log(stringWriter.toString());
        flushLog();

        if (_eventPublisher != null) {
            _eventPublisher.publishEvent(new JobFailedEvent(this, _execution, component, data, throwable));
        }
    }

    @Override
    public void setStatusSuccess(Object result) {
        log("Job execution SUCCESS");
        _execution.setJobEndDate(new Date());
        _execution.setExecutionStatus(ExecutionStatus.SUCCESS);

        flushLog();

        if (_eventPublisher != null) {
            _eventPublisher.publishEvent(new JobExecutedEvent(this, _execution, result));
        }
    }

    @Override
    public void log(String message) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String dateString = dateFormat.format(new Date());

        synchronized (_log) {
            if (_log.length() > 0) {
                _log.append('\n');
            }
            _log.append(dateString);
            _log.append(" - ");
            _log.append(message);
        }
    }

    @Override
    public void log(String message, Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        if (message != null) {
            stringWriter.write(message);
            stringWriter.write('\n');
        }

        final PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();

        log(stringWriter.toString());
    }

    @Override
    public void flushLog() {
        _execution.setLogOutput(_log.toString());

        _logFile.writeFile(new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                // synchronize while writing
                synchronized (_log) {
                    _executionLogWriter.write(_execution, out);
                }
            }
        });
    }

}
