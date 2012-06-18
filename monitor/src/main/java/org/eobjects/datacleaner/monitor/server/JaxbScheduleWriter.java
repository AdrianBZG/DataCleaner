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
package org.eobjects.datacleaner.monitor.server;

import java.io.OutputStream;
import java.util.List;

import org.eobjects.datacleaner.monitor.jaxb.Alert;
import org.eobjects.datacleaner.monitor.jaxb.MetricType;
import org.eobjects.datacleaner.monitor.jaxb.Schedule;
import org.eobjects.datacleaner.monitor.jaxb.Schedule.Alerts;
import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;

/**
 * Jaxb based Schedule writer for .schedule.xml files.
 */
public class JaxbScheduleWriter extends JaxbWriter<Schedule> {

    public void write(ScheduleDefinition scheduleDefinition, OutputStream outputStream) {
        Schedule schedule = createSchedule(scheduleDefinition);

        marshal(schedule, outputStream);
    }

    public Schedule createSchedule(ScheduleDefinition scheduleDefinition) {

        final Schedule schedule = getObjectFactory().createSchedule();
        schedule.setActive(scheduleDefinition.isActive());
        schedule.setScheduleExpression(scheduleDefinition.getScheduleExpression());

        final JobIdentifier scheduleAfterJob = scheduleDefinition.getScheduleAfterJob();
        if (scheduleAfterJob != null) {
            schedule.setScheduleAfterJob(scheduleAfterJob.getName());
        }

        final Alerts alerts = new Alerts();
        final List<AlertDefinition> alertDefinitions = scheduleDefinition.getAlerts();
        for (AlertDefinition alertDefinition : alertDefinitions) {
            final Alert alert = createAlert(alertDefinition);
            alerts.getAlert().add(alert);
        }
        schedule.setAlerts(alerts);

        return schedule;
    }

    private Alert createAlert(AlertDefinition alertDefinition) {
        final Alert alert = getObjectFactory().createAlert();
        alert.setDescription(alertDefinition.getDescription());
        alert.setMinimumValue((alertDefinition.getMinimumValue() == null ? null : alertDefinition.getMinimumValue()
                .intValue()));
        alert.setMaximumValue((alertDefinition.getMaximumValue() == null ? null : alertDefinition.getMaximumValue()
                .intValue()));
        alert.setMetric(createMetric(alertDefinition.getMetricIdentifier()));
        return alert;
    }

    private MetricType createMetric(MetricIdentifier metricIdentifier) {
        final MetricType metric = getObjectFactory().createMetricType();
        metric.setAnalyzerDescriptorName(metricIdentifier.getAnalyzerDescriptorName());
        metric.setAnalyzerInput(metricIdentifier.getAnalyzerInputName());
        metric.setAnalyzerName(metricIdentifier.getAnalyzerName());
        metric.setMetricDescriptorName(metricIdentifier.getMetricDescriptorName());
        metric.setMetricParamColumnName(metricIdentifier.getParamColumnName());
        metric.setMetricParamQueryString(metricIdentifier.getParamQueryString());
        return metric;
    }

}