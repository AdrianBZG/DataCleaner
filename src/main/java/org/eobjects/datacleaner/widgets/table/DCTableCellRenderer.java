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
package org.eobjects.datacleaner.widgets.table;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.eobjects.datacleaner.widgets.Alignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DCTableCellRenderer implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DCTableCellRenderer.class);

	private final Map<Integer, Alignment> _alignmentOverrides;
	private final DefaultTableCellRenderer _delegate;

	public DCTableCellRenderer() {
		super();
		_alignmentOverrides = new HashMap<Integer, Alignment>();
		_delegate = new DefaultTableCellRenderer();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, final Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		logger.debug("getTableCellRendererComponent({},{})", row, column);
		if (value instanceof Icon) {
			final JLabel label = new JLabel((Icon) value);
			label.setOpaque(true);
			return label;
		}

		Alignment alignment = _alignmentOverrides.get(column);
		if (alignment == null) {
			alignment = Alignment.LEFT;
		}

		if (value instanceof JComponent) {
			final JComponent component = (JComponent) value;
			component.setOpaque(true);
			if (value instanceof JLabel) {
				((JLabel) value).setHorizontalAlignment(alignment.getSwingContstantsAlignment());
			} else if (value instanceof JPanel) {
				final LayoutManager layout = ((JPanel) value).getLayout();
				if (layout instanceof FlowLayout) {
					final FlowLayout flowLayout = (FlowLayout) layout;
					flowLayout.setAlignment(alignment.getFlowLayoutAlignment());
				}
			}
			return component;
		}

		final Component result = _delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		assert result instanceof JLabel;
		if (result instanceof JLabel) {
			final JLabel label = (JLabel) result;
			label.setHorizontalAlignment(alignment.getSwingContstantsAlignment());
		}

		return result;
	}

	public void setAlignment(int column, Alignment alignment) {
		_alignmentOverrides.put(column, alignment);
	}

}
