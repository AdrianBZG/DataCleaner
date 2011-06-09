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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.datacleaner.output.beans.CsvOutputAnalyzer;
import org.eobjects.datacleaner.panels.RowProcessingAnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WindowManager;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.datacleaner.windows.AbstractDialog;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.eobjects.metamodel.schema.Table;

/**
 * Provides an action for the user to save a table as a CSV file
 * 
 * @author Kasper Sørensen
 */
public final class SaveTableAsCsvFileActionListener implements ActionListener {

	private final Datastore _datastore;
	private final Table _table;
	private final WindowManager _windowManager;

	public SaveTableAsCsvFileActionListener(Datastore datastore, Table table, WindowManager windowManager) {
		_datastore = datastore;
		_table = table;
		_windowManager = windowManager;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final AnalyzerBeansConfiguration configuration = DCConfiguration.get();
		final AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
		ajb.setDatastore(_datastore);
		ajb.addSourceColumns(_table.getColumns());

		final RowProcessingAnalyzerJobBuilder<CsvOutputAnalyzer> csvOutputAnalyzerBuilder = ajb
				.addRowProcessingAnalyzer(CsvOutputAnalyzer.class);
		csvOutputAnalyzerBuilder.addInputColumns(ajb.getSourceColumns());
		File directory = UserPreferences.getInstance().getConfiguredFileDirectory();
		csvOutputAnalyzerBuilder.getConfigurableBean().setFile(new File(directory, _table.getName() + ".csv"));

		final RowProcessingAnalyzerJobBuilderPanel presenter = new RowProcessingAnalyzerJobBuilderPanel(
				csvOutputAnalyzerBuilder, false);

		final AbstractDialog dialog = new AbstractDialog(_windowManager) {
			private static final long serialVersionUID = 1L;

			@Override
			public String getWindowTitle() {
				return "Save " + _table.getName() + " as CSV file";
			}

			@Override
			protected int getDialogWidth() {
				return 500;
			}

			@Override
			protected JComponent getDialogContent() {
				final AnalyzerBeanDescriptor<CsvOutputAnalyzer> descriptor = csvOutputAnalyzerBuilder.getDescriptor();
				final CloseableTabbedPane tabbedPane = new CloseableTabbedPane();
				tabbedPane.addTab(descriptor.getDisplayName(),
						IconUtils.getDescriptorIcon(descriptor, IconUtils.ICON_SIZE_LARGE), presenter.createJComponent());
				tabbedPane.setUnclosableTab(0);
				return tabbedPane;
			}

			@Override
			protected String getBannerTitle() {
				return "Save " + _table.getName() + "\nas CSV file";
			}
		};

		final JButton runButton = new JButton("Run", ImageManager.getInstance().getImageIcon("images/actions/execute.png"));
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ResultWindow window = new ResultWindow(configuration, ajb.toAnalysisJob(), "Save " + _table.getName()
						+ " as CSV file", _windowManager);
				window.setVisible(true);
				dialog.dispose();
				window.startAnalysis();
			}
		});

		presenter.addToButtonPanel(runButton);

		dialog.setVisible(true);
	}
}
