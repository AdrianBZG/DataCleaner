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
package org.eobjects.datacleaner.monitor.shared.model;

/**
 * Enumerates the security roles used in the DC monitor
 */
public interface SecurityRoles {

    public static final String VIEWER = "ROLE_VIEWER";
    public static final String JOB_EDITOR = "ROLE_JOB_EDITOR";
    public static final String DASHBOARD_EDITOR = "ROLE_DASHBOARD_EDITOR";
    public static final String SCHEDULE_EDITOR = "ROLE_SCHEDULE_EDITOR";

    // super roles
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String ENGINEER = "ROLE_ENGINEER";
}
