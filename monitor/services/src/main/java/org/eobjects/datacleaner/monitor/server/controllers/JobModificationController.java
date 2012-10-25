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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.events.JobModificationEvent;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/{tenant}/jobs/{job}.modify")
public class JobModificationController {

    private static final String EXTENSION = FileFilters.ANALYSIS_XML.getExtension();

    private static final Logger logger = LoggerFactory.getLogger(JobModificationController.class);

    @Autowired
    ApplicationEventPublisher _eventPublisher;

    @Autowired
    TenantContextFactory _contextFactory;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    @RolesAllowed({ SecurityRoles.ADMIN })
    public Map<String, String> modifyResult(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestBody final JobModificationPayload input) {

        logger.info("Request payload: {}", input);

        jobName = jobName.replaceAll("\\+", " ");

        final TenantContext tenantContext = _contextFactory.getContext(tenant);

        final JobContext oldJob = tenantContext.getJob(jobName);
        final RepositoryFile existingFile = oldJob.getJobFile();

        final String nameInput = input.getName();

        final String newFilename = nameInput + EXTENSION;

        final RepositoryFolder jobFolder = tenantContext.getJobFolder();
        final RepositoryFile newFile = jobFolder.getFile(newFilename);

        final boolean overwrite = input.getOverwrite() != null && input.getOverwrite().booleanValue();

        final Action<OutputStream> writeAction = new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                oldJob.toXml(out);
            }
        };

        if (newFile == null) {
            jobFolder.createFile(newFilename, writeAction);
        } else {
            if (overwrite) {
                newFile.writeFile(writeAction);
            } else {
                throw new IllegalStateException("A job file with the name '" + newFilename
                        + "' already exists, and the 'overwrite' flag is non-true.");
            }
        }

        existingFile.delete();

        final JobContext newJob = tenantContext.getJob(newFilename);

        _eventPublisher.publishEvent(new JobModificationEvent(this, tenant, oldJob.getName(), newJob.getName()));

        final Map<String, String> response = new TreeMap<String, String>();
        response.put("old_job_name", oldJob.getName());
        response.put("new_job_name", newJob.getName());
        response.put("repository_url", "/" + tenant + "/jobs/" + newFilename);
        logger.debug("Response payload: {}", response);

        return response;
    }
}