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
package org.datacleaner.panels.fuse;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

class StreamColumnListPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final Table _table;
    private final List<DCCheckBox<InputColumn<?>>> _checkBoxes;
    private final DCCheckBox.Listener<InputColumn<?>> _checkBoxListener;

    public StreamColumnListPanel(AnalysisJobBuilder rootAnalysisJobBuilder, Table table,
            DCCheckBox.Listener<InputColumn<?>> checkBoxListener) {
        super();
        _table = table;
        _checkBoxes = new ArrayList<>();
        _checkBoxListener = checkBoxListener;

        setLayout(new VerticalLayout(0));

        final String iconPath;
        if (rootAnalysisJobBuilder.containsSourceTable(table)) {
            iconPath = IconUtils.MODEL_TABLE;
        } else {
            iconPath = IconUtils.OUTPUT_DATA_STREAM_PATH;
        }

        final DCLabel streamHeader = DCLabel.dark(table.getName());
        streamHeader.setFont(WidgetUtils.FONT_SMALL.deriveFont(Font.BOLD));
        streamHeader.setIcon(ImageManager.get().getImageIcon(iconPath, IconUtils.ICON_SIZE_SMALL));
        add(streamHeader);
        add(Box.createVerticalStrut(4));
    }

    public void addInputColumn(InputColumn<?> inputColumn, boolean coalesced) {
        final DCCheckBox<InputColumn<?>> checkBox = new DCCheckBox<>(inputColumn.getName(), coalesced);
        checkBox.addListener(_checkBoxListener);
        checkBox.setValue(inputColumn);
        add(checkBox);
        _checkBoxes.add(checkBox);
    }

    public List<InputColumn<?>> getAllInputColumns() {
        final List<InputColumn<?>> result = new ArrayList<>();
        for (DCCheckBox<InputColumn<?>> checkBox : _checkBoxes) {
            result.add(checkBox.getValue());
        }
        return result;
    }

    public List<InputColumn<?>> getCoalescedInputColumns() {
        final List<InputColumn<?>> result = new ArrayList<>();
        for (DCCheckBox<InputColumn<?>> checkBox : _checkBoxes) {
            if (checkBox.isSelected()) {
                result.add(checkBox.getValue());
            }
        }
        return result;
    }

    public Table getTable() {
        return _table;
    }
}
