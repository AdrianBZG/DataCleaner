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
package org.eobjects.datacleaner.monitor.shared.widgets;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A {@link PopupPanel} which has a heading in the top and a button panel in the
 * bottom.
 */
public class DCPopupPanel extends PopupPanel {

    private final SimplePanel _panel;
    private final ButtonPanel _buttonPanel;

    public DCPopupPanel(String heading) {
        super(true, true);
        addStyleName("DCPopupPanel");
        setGlassEnabled(true);
        _buttonPanel = new ButtonPanel();
        _panel = new SimplePanel();
        _panel.setStyleName("DCPopupPanelContent");

        final FlowPanel outerPanel = new FlowPanel();
        outerPanel.add(new HeadingLabel(heading));
        outerPanel.add(_panel);
        outerPanel.add(_buttonPanel);

        super.setWidget(outerPanel);
    }
    
    public void addButton(Button button) {
        getButtonPanel().addButton(button);
    }
    
    public void removeButton(Button button) {
        getButtonPanel().removeButton(button);
    }

    public ButtonPanel getButtonPanel() {
        return _buttonPanel;
    }
    
    public void removeButtons() {
        getButtonPanel().clear();
    }

    @Override
    public void setWidget(Widget w) {
        _panel.setWidget(w);
    }

    @Override
    public void setWidget(IsWidget w) {
        _panel.setWidget(w);
    }
}
