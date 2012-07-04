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
package org.eobjects.datacleaner.monitor.scheduling.model;

import java.util.Date;

import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a running (or finished) execution of a job.
 */
public class ExecutionLog extends ExecutionIdentifier implements IsSerializable {

    // metadata about the job and the trigger
    private ScheduleDefinition _schedule;
    private JobIdentifier _job;

    // execution level output
    private String _logOutput;
    private Date _jobEndDate;

    // no-args constructor
    public ExecutionLog() {
        super();
    }

    public ExecutionLog(ScheduleDefinition schedule, TriggerType triggerType) {
        super(schedule.getJob().getName() + "-" + new Date().getTime(), triggerType);
        _schedule = schedule;
        _job = schedule.getJob();
    }

    public ScheduleDefinition getSchedule() {
        return _schedule;
    }
    
    public void setSchedule(ScheduleDefinition schedule) {
        _schedule = schedule;
    }

    public String getLogOutput() {
        return _logOutput;
    }

    public void setLogOutput(String logOutput) {
        _logOutput = logOutput;
    }

    public Date getJobEndDate() {
        return _jobEndDate;
    }

    public void setJobEndDate(Date jobEndDate) {
        _jobEndDate = jobEndDate;
    }

    public JobIdentifier getJob() {
        return _job;
    }
}
