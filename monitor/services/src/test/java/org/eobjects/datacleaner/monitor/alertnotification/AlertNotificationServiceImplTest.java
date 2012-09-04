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
package org.eobjects.datacleaner.monitor.alertnotification;

import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class AlertNotificationServiceImplTest extends TestCase {
    
    public void testNotify() throws Exception {
        final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                "context/application-context.xml");
        final AlertNotificationService alertNotificationService = applicationContext.getBean(AlertNotificationService.class);
        
        TenantIdentifier tenant = new TenantIdentifier("tenant1");
        JobIdentifier job = new JobIdentifier("product_profiling");
        DatastoreIdentifier datastoreIdentifier = new DatastoreIdentifier("orderdb");
        ScheduleDefinition schedule = new ScheduleDefinition(tenant, job, datastoreIdentifier);
        ExecutionLog execution = new ExecutionLog(schedule, TriggerType.MANUAL);
        execution.setResultId("product_profiling-3.analysis.result.dat");
        alertNotificationService.notifySubscribers(execution);
    }

    public void testIsBeyondThreshold() throws Exception {
        AlertNotificationServiceImpl service = new AlertNotificationServiceImpl(null, null);
        
        assertFalse(service.isBeyondThreshold(10, 5, 15));
        assertFalse(service.isBeyondThreshold(10, null, null));
        
        assertTrue(service.isBeyondThreshold(10, 11, 15));
        assertTrue(service.isBeyondThreshold(10, 5, 9));
        
        assertTrue(service.isBeyondThreshold(10, null, 9));
        assertTrue(service.isBeyondThreshold(10, 11, null));
        
        assertFalse(service.isBeyondThreshold(10, null, 11));
        assertFalse(service.isBeyondThreshold(10, 5, null));
    }
}
