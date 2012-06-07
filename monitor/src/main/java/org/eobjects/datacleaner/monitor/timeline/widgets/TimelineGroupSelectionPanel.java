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
package org.eobjects.datacleaner.monitor.timeline.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.timeline.TimelineServiceAsync;
import org.eobjects.datacleaner.monitor.timeline.model.TimelineGroup;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A panel which shows and let's the user select different timeline groups
 */
public class TimelineGroupSelectionPanel extends FlowPanel {

    private final TenantIdentifier _tenant;
    private final TimelineServiceAsync _service;
    private final SimplePanel _targetPanel;
    private final List<Anchor> _anchors;

    public TimelineGroupSelectionPanel(TenantIdentifier tenant, TimelineServiceAsync service, SimplePanel targetPanel) {
        super();

        _tenant = tenant;
        _service = service;
        _targetPanel = targetPanel;
        _anchors = new ArrayList<Anchor>();

        addStyleName("TimelineGroupSelectionPanel");

        // add the default/"welcome" group
        Anchor defaultAnchor = addGroup(null);
        defaultAnchor.fireEvent(new ClickEvent() {
        });

        // load all other groups
        _service.getTimelineGroups(_tenant, new DCAsyncCallback<List<TimelineGroup>>() {
            @Override
            public void onSuccess(List<TimelineGroup> result) {
                for (TimelineGroup group : result) {
                    addGroup(group);
                }
            }
        });
    }

    public Anchor addGroup(final TimelineGroup group) {
        final String groupName;
        if (group == null) {
            groupName = "(default)";
        } else {
            groupName = group.getName();
        }

        final Anchor anchor = new Anchor(groupName);
        anchor.addClickHandler(new ClickHandler() {
            private TimelineGroupPanel panel = null;

            @Override
            public void onClick(ClickEvent event) {
                for (Anchor anchor : _anchors) {
                    anchor.removeStyleName("selected");
                }
                anchor.addStyleName("selected");

                if (panel == null) {
                    panel = new TimelineGroupPanel(_service, _tenant, group);
                }
                _targetPanel.setWidget(panel);
            }
        });
        add(anchor);

        _anchors.add(anchor);
        return anchor;
    }

}
