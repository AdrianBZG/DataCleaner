/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.eobjects.datacleaner.actions;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.ChangeRequirementMenu;

/**
 * Abstract class containing the action method that will display a popup with
 * options as to change a job builder.
 */
public abstract class AbstractJobBuilderPopupListener {

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final AbstractBeanJobBuilder<?, ?, ?> _jobBuilder;

    public AbstractJobBuilderPopupListener(AbstractBeanJobBuilder<?, ?, ?> jobBuilder,
            AnalysisJobBuilder analysisJobBuilder) {
        _jobBuilder = jobBuilder;
        _analysisJobBuilder = analysisJobBuilder;
    }

    public AbstractBeanJobBuilder<?, ?, ?> getJobBuilder() {
        return _jobBuilder;
    }

    public AnalysisJobBuilder getAnalysisJobBuilder() {
        return _analysisJobBuilder;
    }

    public void showPopup(Component parentComponent, int x, int y) {
        final Icon renameIcon = ImageManager.get().getImageIcon(IconUtils.ACTION_RENAME, IconUtils.ICON_SIZE_SMALL);
        final JMenuItem renameMenuItem = WidgetFactory.createMenuItem("Rename component", renameIcon);
        renameMenuItem.addActionListener(new RenameComponentActionListener(_jobBuilder) {
            @Override
            protected void onNameChanged() {
                AbstractJobBuilderPopupListener.this.onNameChanged();
            }
        });

        final JMenuItem removeMenuItem = new RemoveComponentMenuItem(_analysisJobBuilder, _jobBuilder) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onRemoved() {
                AbstractJobBuilderPopupListener.this.onRemoved();
            }
        };

        JPopupMenu popup = new JPopupMenu();
        popup.add(renameMenuItem);
        popup.add(removeMenuItem);
        popup.add(new ChangeRequirementMenu(_jobBuilder));
        popup.show(parentComponent, x, y);
    }

    protected abstract void onNameChanged();

    protected abstract void onRemoved();
}
