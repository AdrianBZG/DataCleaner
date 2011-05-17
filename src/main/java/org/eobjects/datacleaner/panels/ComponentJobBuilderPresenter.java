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
package org.eobjects.datacleaner.panels;

import javax.swing.JComponent;

/**
 * Interface for all job builder presenter objects. These are objects that are
 * used to present the configuration screen (eg. the builder objects) of a
 * component.
 * 
 * @author Kasper Sørensen
 */
public interface ComponentJobBuilderPresenter {

	/**
	 * Gets the job builder object that is being presented.
	 * 
	 * @return
	 */
	public Object getJobBuilder();

	/**
	 * Gets the {@link JComponent} that is the visual representation of the job
	 * builder.
	 * 
	 * @return
	 */
	public JComponent getJComponent();

	/**
	 * Invoked before execution, the presenter should make sure all configured
	 * properties are set on the job builder.
	 */
	public void applyPropertyValues();

	/**
	 * Invoked when a configured property changes.
	 */
	public void onConfigurationChanged();
}
