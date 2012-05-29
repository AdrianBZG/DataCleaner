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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.util.InputColumnComparator;
import org.eobjects.analyzer.util.LabelUtils;
import org.eobjects.datacleaner.actions.PreviewSourceDataActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.table.TableColumnExt;

public final class ColumnListTable extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final String[] headers = new String[] { "Name", "Type", "" };
	private final ImageManager imageManager = ImageManager.getInstance();
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final Table _table;
	private final DCTable _columnTable;

	// private final SortedSet<InputColumn<?>> _columns = new
	// TreeSet<InputColumn<?>>(new InputColumnComparator());

	// TODO: Temporary patch to ensure correct ordering. Replace with previous
	// SortedSet when comparator is working.
	private final List<InputColumn<?>> _columns = new ArrayList<InputColumn<?>>();

	private final WindowContext _windowContext;

	public ColumnListTable(Collection<? extends InputColumn<?>> columns, AnalysisJobBuilder analysisJobBuilder,
			boolean addShadowBorder, WindowContext windowContext) {
		this(null, columns, analysisJobBuilder, addShadowBorder, windowContext);
	}

	public ColumnListTable(Table table, AnalysisJobBuilder analysisJobBuilder, boolean addShadowBorder,
			WindowContext windowContext) {
		this(table, null, analysisJobBuilder, addShadowBorder, windowContext);
	}

	private ColumnListTable(Table table, Collection<? extends InputColumn<?>> columns,
			AnalysisJobBuilder analysisJobBuilder, boolean addShadowBorder, WindowContext windowContext) {
		super();
		_table = table;
		_analysisJobBuilder = analysisJobBuilder;
		_windowContext = windowContext;

		setLayout(new BorderLayout());

		if (table != null) {
			DCPanel headerPanel = new DCPanel();
			headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			JLabel tableNameLabel = new JLabel(table.getQualifiedLabel(), imageManager.getImageIcon(
					"images/model/column.png", IconUtils.ICON_SIZE_SMALL), JLabel.LEFT);
			tableNameLabel.setOpaque(false);
			tableNameLabel.setFont(WidgetUtils.FONT_HEADER1);

			JButton previewButton = WidgetFactory.createSmallButton("images/actions/preview_data.png");
			previewButton.setToolTipText("Preview table rows");
			previewButton.addActionListener(new PreviewSourceDataActionListener(_windowContext, _analysisJobBuilder
					.getDatastore(), _columns));

			JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
			removeButton.setToolTipText("Remove table from source");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Column[] cols = _table.getColumns();
					for (Column col : cols) {
						_analysisJobBuilder.removeSourceColumn(col);
					}
				}
			});

			headerPanel.add(tableNameLabel);
			headerPanel.add(Box.createHorizontalStrut(4));
			headerPanel.add(previewButton);
			headerPanel.add(Box.createHorizontalStrut(4));
			headerPanel.add(removeButton);
			add(headerPanel, BorderLayout.NORTH);
		}

		_columnTable = new DCTable(headers);
		_columnTable.setSortable(false);
		_columnTable.setColumnControlVisible(false);
		_columnTable.setRowHeight(IconUtils.ICON_SIZE_SMALL + 4);

		JPanel tablePanel = _columnTable.toPanel();

		if (addShadowBorder) {
			tablePanel.setBorder(new CompoundBorder(WidgetUtils.BORDER_SHADOW, WidgetUtils.BORDER_THIN));
		}
		add(tablePanel, BorderLayout.CENTER);

		if (columns != null) {
			for (InputColumn<?> column : columns) {
				_columns.add(column);
			}
		}
		updateComponents();
	}

	private void updateComponents() {
		TableModel model = new DefaultTableModel(headers, _columns.size());
		int i = 0;
		for (final InputColumn<?> column : _columns) {
		    final Icon icon = IconUtils.getColumnIcon(column, IconUtils.ICON_SIZE_SMALL);
			if (column instanceof MutableInputColumn<?>) {
				final JXTextField textField = WidgetFactory.createTextField("Column name");
				textField.setText(column.getName());
				final MutableInputColumn<?> mutableInputColumn = (MutableInputColumn<?>) column;
				textField.addFocusListener(new FocusAdapter() {
					@Override
					public void focusLost(FocusEvent e) {
						if (!mutableInputColumn.getName().equals(textField.getText())) {
							mutableInputColumn.setName(textField.getText());

							TransformerJobBuilder<?> tjb = _analysisJobBuilder.getOriginatingTransformer(mutableInputColumn);
							if (tjb != null) {
								tjb.onOutputChanged();
							}
						}
					}
				});

				final JButton resetButton = getResetButton(textField, mutableInputColumn);
				final DCPanel panel = new DCPanel();
				panel.setLayout(new HorizontalLayout(4));
				panel.add(new JLabel(icon));
				panel.add(textField);
				panel.add(resetButton);

				model.setValueAt(panel, i, 0);
			} else {
				model.setValueAt(new JLabel(column.getName(), icon, JLabel.LEFT), i, 0);
			}

			final Class<?> dataType = column.getDataType();
			final String dataTypeString = LabelUtils.getDataTypeLabel(dataType);
			model.setValueAt(dataTypeString, i, 1);

			JButton removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE);
			removeButton.setToolTipText("Remove column from source");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					_analysisJobBuilder.removeSourceColumn(column.getPhysicalColumn());
				}
			});

			DCPanel buttonPanel = new DCPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
			// buttonPanel.add(transformButton);
			// buttonPanel.add(filterButton);
			if (column.isPhysicalColumn()) {
				buttonPanel.add(removeButton);
			}

			model.setValueAt(buttonPanel, i, 2);
			i++;
		}
		_columnTable.setModel(model);

		TableColumnExt columnExt = _columnTable.getColumnExt(2);
		columnExt.setMinWidth(26);
		columnExt.setMaxWidth(80);
		columnExt.setPreferredWidth(30);
	}

	private JButton getResetButton(final JXTextField textField, final MutableInputColumn<?> mutableInputColumn) {
		JButton button = WidgetFactory.createSmallButton("images/actions/reset.png");
		button.setToolTipText("Reset output column name");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				textField.setText(mutableInputColumn.getInitialName());
			}
		});
		return button;
	}

	public Table getTable() {
		return _table;
	}

	public void addColumn(InputColumn<?> column) {
		// TODO: Temporary patch to ensure correct ordering. Remove the
		// if-sentence when comparator is working for transformed columns
		if (!_columns.contains(column)) {
			_columns.add(column);
			Collections.sort(_columns, new InputColumnComparator());
		}
		updateComponents();
	}

	public void removeColumn(InputColumn<?> column) {
		_columns.remove(column);
		updateComponents();
	}

	public void setColumns(List<? extends InputColumn<?>> columns) {
		_columns.clear();
		for (InputColumn<?> column : columns) {
			_columns.add(column);
		}
		updateComponents();
	}

	public int getColumnCount() {
		return _columns.size();
	}
}
