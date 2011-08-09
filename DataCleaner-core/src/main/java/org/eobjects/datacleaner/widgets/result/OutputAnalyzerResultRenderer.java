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
package org.eobjects.datacleaner.widgets.result;

import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.actions.PreviewSourceDataActionListener;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.output.beans.OutputAnalyzerResult;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.metamodel.schema.Table;
import org.jdesktop.swingx.VerticalLayout;

@RendererBean(SwingRenderingFormat.class)
public class OutputAnalyzerResultRenderer extends AbstractRenderer<OutputAnalyzerResult, JComponent> {

	private final ImageManager imageManager = ImageManager.getInstance();

	@Inject
	WindowContext windowContext;

	@Override
	public JComponent render(OutputAnalyzerResult result) {
		final EmptyBorder border = new EmptyBorder(10, 10, 10, 10);

		int rowCount = result.getWrittenRowCount();
		if (rowCount == 0) {
			JLabel label = new JLabel("No rows written!", imageManager.getImageIcon("images/status/warning.png"),
					JLabel.LEFT);
			label.setBorder(border);
			return label;
		} else {
			final JLabel label = new JLabel(rowCount + " rows written!",
					imageManager.getImageIcon("images/status/valid.png"), JLabel.LEFT);
			final DCPanel buttonPanel = createButtonPanel(result);

			final DCPanel panel = new DCPanel();
			panel.setLayout(new VerticalLayout(4));
			panel.add(label);
			panel.add(buttonPanel);
			panel.setBorder(border);

			return panel;
		}
	}

	private DCPanel createButtonPanel(final OutputAnalyzerResult result) {
		final DCPanel panel = new DCPanel();
		panel.setLayout(new FlowLayout(Alignment.LEFT.getFlowLayoutAlignment(), 0, 4));

		final AnalyzerBeansConfiguration configuration = DCConfiguration.get();
		final DatastoreCatalog datastoreCatalog = configuration.getDatastoreCatalog();
		final Datastore datastore = result.getDatastore(datastoreCatalog);
		final Insets buttonMargin = new Insets(1, 4, 1, 4);
		if (datastore != null && datastore.getName() != null) {
			final Datastore ds = datastoreCatalog.getDatastore(datastore.getName());
			if (!datastore.equals(ds)) {
				final JButton addDatastoreButton = new JButton("Add to datastores",
						imageManager.getImageIcon("images/actions/add.png"));
				addDatastoreButton.setMargin(buttonMargin);
				addDatastoreButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						MutableDatastoreCatalog mutableDatastoreCatalog = (MutableDatastoreCatalog) datastoreCatalog;
						mutableDatastoreCatalog.addDatastore(datastore);
						addDatastoreButton.setEnabled(false);
					}
				});
				panel.add(addDatastoreButton);
				panel.add(Box.createHorizontalStrut(4));
			}

			final JButton analyzeButton = new JButton("Analyze this datastore",
					imageManager.getImageIcon("images/filetypes/analysis_job.png"));
			analyzeButton.setMargin(buttonMargin);
			analyzeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO: GUICE
					// final AnalysisJobBuilderWindow window = new
					// AnalysisJobBuilderWindow(configuration, datastore,
					// windowContext);
					// window.setVisible(true);
				}
			});
			panel.add(analyzeButton);
			panel.add(Box.createHorizontalStrut(4));

			final JButton previewButton = new JButton("Preview table",
					imageManager.getImageIcon("images/actions/preview_data.png"));
			previewButton.setMargin(buttonMargin);
			previewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final DataContextProvider dcp = datastore.getDataContextProvider();
					final Table previewTable = result.getPreviewTable(datastore);
					if (previewTable == null) {
						throw new IllegalStateException("Result did not return any preview table: " + result);
					} else {
						final PreviewSourceDataActionListener actionListener = new PreviewSourceDataActionListener(
								windowContext, dcp, previewTable);
						actionListener.actionPerformed(null);
					}
				}
			});
			panel.add(previewButton);
		}
		return panel;
	}
}
