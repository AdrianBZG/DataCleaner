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

import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;

import org.eobjects.datacleaner.monitor.dashboard.DashboardService;
import org.eobjects.datacleaner.monitor.dashboard.model.JobMetrics;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineData;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineDefinition;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineGroup;
import org.eobjects.datacleaner.monitor.dashboard.model.TimelineIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Servlet wrapper/proxy for the {@link DashboardService}. Passes all service
 * requests on to a delegate, see {@link #setDelegate(DashboardService)} and
 * {@link #getDelegate()}.
 */
public class DashboardServiceServlet extends SecureGwtServlet implements DashboardService {

    private static final long serialVersionUID = 1L;

    private DashboardService _delegate;

    @Override
    public void init() throws ServletException {
        super.init();

        if (_delegate == null) {
            WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            DashboardService delegate = applicationContext.getBean(DashboardService.class);
            if (delegate == null) {
                throw new ServletException("No delegate found in application context!");
            }
            _delegate = delegate;
        }
    }

    public void setDelegate(DashboardService delegate) {
        _delegate = delegate;
    }

    public DashboardService getDelegate() {
        return _delegate;
    }

    @Override
    public List<JobIdentifier> getJobs(TenantIdentifier tenant) {
        return _delegate.getJobs(tenant);
    }

    @Override
    public JobMetrics getJobMetrics(TenantIdentifier tenant, JobIdentifier job) {
        return _delegate.getJobMetrics(tenant, job);
    }

    @Override
    public List<TimelineIdentifier> getTimelines(TenantIdentifier tenant, TimelineGroup group) {
        return _delegate.getTimelines(tenant, group);
    }

    @Override
    public TimelineDefinition getTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline) {
        return _delegate.getTimelineDefinition(tenant, timeline);
    }

    @Override
    public TimelineData getTimelineData(TenantIdentifier tenant, TimelineDefinition timeline) {
        return _delegate.getTimelineData(tenant, timeline);
    }

    @Override
    public TimelineIdentifier updateTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition) {
        return _delegate.updateTimelineDefinition(tenant, timeline, timelineDefinition);
    }

    @Override
    public TimelineIdentifier createTimelineDefinition(TenantIdentifier tenant, TimelineIdentifier timeline,
            TimelineDefinition timelineDefinition) {
        return _delegate.createTimelineDefinition(tenant, timeline, timelineDefinition);
    }

    @Override
    public Collection<String> getMetricParameterSuggestions(TenantIdentifier tenant, JobIdentifier job,
            MetricIdentifier metric) {
        return _delegate.getMetricParameterSuggestions(tenant, job, metric);
    }

    @Override
    public Boolean removeTimeline(TenantIdentifier tenant, TimelineIdentifier timeline) {
        return _delegate.removeTimeline(tenant, timeline);
    }

    @Override
    public List<TimelineGroup> getTimelineGroups(final TenantIdentifier tenant) {
        return _delegate.getTimelineGroups(tenant);
    }

    @Override
    public TimelineGroup addTimelineGroup(TenantIdentifier tenant, String name) {
        return _delegate.addTimelineGroup(tenant, name);
    }

    @Override
    public Boolean removeTimelineGroup(TenantIdentifier tenant, TimelineGroup timelineGroup) {
        return _delegate.removeTimelineGroup(tenant, timelineGroup);
    }

    @Override
    public boolean isDashboardEditor(TenantIdentifier tenant) {
        return hasRole(SecurityRoles.DASHBOARD_EDITOR) && _delegate.isDashboardEditor(tenant);
    }
}
