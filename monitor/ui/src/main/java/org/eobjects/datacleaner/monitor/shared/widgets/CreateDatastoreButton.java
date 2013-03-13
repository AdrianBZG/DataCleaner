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
package org.eobjects.datacleaner.monitor.shared.widgets;

import org.eobjects.datacleaner.monitor.shared.WizardService;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * A button which will pop up a datastore wizard
 */
public class CreateDatastoreButton extends Button implements ClickHandler {

    private final WizardServiceAsync service = GWT.create(WizardService.class);

    private final TenantIdentifier _tenant;

    public CreateDatastoreButton(TenantIdentifier tenant) {
        super("New datastore");
        _tenant = tenant;

        addStyleDependentName("ImageTextButton");
        addStyleName("NewDatastoreButton");

        addClickHandler(this);
    }

    @Override
    public void onClick(ClickEvent event) {
        startWizard();
    }
    
    public void startWizard() {
        DCPopupPanel popup = new DatastoreWizardPopupPanel(service, _tenant);
        popup.center();
        popup.show();
    }
}
