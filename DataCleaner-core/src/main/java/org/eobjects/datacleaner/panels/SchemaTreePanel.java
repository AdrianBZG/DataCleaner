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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.util.WindowManager;
import org.eobjects.datacleaner.widgets.LoadingIcon;
import org.eobjects.datacleaner.widgets.tree.SchemaTree;

public class SchemaTreePanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final WindowManager _windowManager;
	private JComponent _updatePanel;

	public SchemaTreePanel(AnalysisJobBuilder analysisJobBuilder, WindowManager windowManager) {
		super(imageManager.getImage("images/window/schema-tree-background.png"), 100, 100, WidgetUtils.BG_COLOR_BRIGHTEST,
				WidgetUtils.BG_COLOR_BRIGHT);
		_analysisJobBuilder = analysisJobBuilder;
		_windowManager = windowManager;
		setLayout(new BorderLayout());
		setBorder(WidgetUtils.BORDER_WIDE);
		setDatastore(null);
	}

	public void setDatastore(final Datastore datastore) {
		removeAll();
		if (datastore == null) {
			add(new DCPanel().setPreferredSize(150, 150), BorderLayout.CENTER);
			return;
		}

		add(new LoadingIcon().setPreferredSize(150, 150), BorderLayout.CENTER);

		// load the schema tree in the background because it will retrieve
		// metadata about the datastore (might take several seconds)
		new SwingWorker<SchemaTree, Void>() {
			@Override
			protected SchemaTree doInBackground() throws Exception {
				return new SchemaTree(datastore, _analysisJobBuilder, _windowManager);
			}

			protected void done() {
				try {
					SchemaTree schemaTree = get();
					final JScrollPane schemaTreeScroll = WidgetUtils.scrolleable(schemaTree);
					schemaTreeScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					schemaTree.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentResized(ComponentEvent e) {
							updateParentPanel();
						}

					});
					removeAll();
					add(schemaTreeScroll, BorderLayout.CENTER);
					updateParentPanel();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			};

		}.execute();
	}

	private void updateParentPanel() {
		if (_updatePanel != null) {
			_updatePanel.updateUI();
		}
	}

	public void setUpdatePanel(JComponent updatePanel) {
		_updatePanel = updatePanel;
	}
}
