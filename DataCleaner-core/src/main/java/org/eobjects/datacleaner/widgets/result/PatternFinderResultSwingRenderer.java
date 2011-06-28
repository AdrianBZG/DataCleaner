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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.JaxbConfigurationReader;
import org.eobjects.analyzer.connection.DataContextProvider;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.result.PatternFinderResult;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.DataCleanerHome;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.ChartUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.Alignment;
import org.eobjects.datacleaner.widgets.table.CrosstabPanel;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.datacleaner.windows.ResultWindow;
import org.eobjects.metamodel.schema.Table;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Renderer for {@link PatternFinderAnalyzer} results. Displays as a crosstab
 * with an optional chart displaying the distribution of the patterns.
 * 
 * @author Kasper Sørensen
 * 
 */
@RendererBean(SwingRenderingFormat.class)
public class PatternFinderResultSwingRenderer extends AbstractCrosstabResultSwingRenderer<PatternFinderResult> {

	private final MutableReferenceDataCatalog _catalog = (MutableReferenceDataCatalog) DCConfiguration.get()
			.getReferenceDataCatalog();

	@Override
	public JComponent render(PatternFinderResult result) {
		final CrosstabPanel crosstabPanel = super.renderInternal(result);
		final DCTable table = crosstabPanel.getTable();
		if (isInitiallyCharted(table) || isTooLimitedToChart(table)) {
			return crosstabPanel;
		}

		final DCPanel headerPanel = new DCPanel();
		headerPanel.setLayout(new FlowLayout(Alignment.RIGHT.getFlowLayoutAlignment(), 1, 1));

		final JButton chartButton = new JButton("Show distribution chart", ImageManager.getInstance().getImageIcon(
				"images/chart-types/bar.png"));
		chartButton.setMargin(new Insets(1, 1, 1, 1));
		chartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				headerPanel.setVisible(false);
				displayChart(table, crosstabPanel.getDisplayChartCallback());
			}
		});

		headerPanel.add(chartButton);

		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(headerPanel, BorderLayout.NORTH);
		panel.add(crosstabPanel, BorderLayout.CENTER);

		return panel;
	}

	protected void displayChart(DCTable table, DisplayChartCallback displayChartCallback) {
		final int rowCount = table.getRowCount();
		final DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
		for (int i = 0; i < rowCount; i++) {
			final Object expressionObject = table.getValueAt(i, 0);
			final String expression = extractString(expressionObject);

			final Object countObject = table.getValueAt(i, 1);
			final String countString = extractString(countObject);
			final int count = Integer.parseInt(countString);
			categoryDataset.addValue(count, expression, "");
		}

		JFreeChart chart = ChartFactory.createBarChart("Pattern distribution", "Pattern", "Match count", categoryDataset,
				PlotOrientation.VERTICAL, false, true, false);
		ChartUtils.applyStyles(chart);
		displayChartCallback.displayChart(new ChartPanel(chart));
	}

	@Override
	protected void decorate(PatternFinderResult result, DCTable table, DisplayChartCallback displayChartCallback) {
		super.decorate(result, table, displayChartCallback);

		table.setAlignment(1, Alignment.RIGHT);

		final int rowCount = table.getRowCount();

		for (int i = 0; i < rowCount; i++) {
			final Object expressionObject = table.getValueAt(i, 0);
			final String expression = extractString(expressionObject);

			final String synonymCatalogName = "PF: " + expression;

			if (!_catalog.containsSynonymCatalog(synonymCatalogName)) {
				DCPanel panel = new DCPanel();
				panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

				panel.add(Box.createHorizontalStrut(4));
				panel.add(new JLabel(expression));

				final JButton button = WidgetFactory.createSmallButton("images/actions/save.png");
				button.setToolTipText("Save as string pattern");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						_catalog.addStringPattern(new SimpleStringPattern(synonymCatalogName, expression));
						button.setEnabled(false);
					}
				});
				panel.add(Box.createHorizontalStrut(4));
				panel.add(button);

				table.setValueAt(panel, i, 0);
			}
		}

		if (isInitiallyCharted(table)) {
			displayChart(table, displayChartCallback);
		}
	}

	private boolean isInitiallyCharted(DCTable table) {
		return table.getRowCount() >= 8;
	}

	private boolean isTooLimitedToChart(DCTable table) {
		return table.getRowCount() <= 1;
	}

	private String extractString(Object obj) {
		if (obj == null) {
			return null;
		} else if (obj instanceof String) {
			return (String) obj;
		} else if (obj instanceof JPanel) {
			Component[] components = ((JPanel) obj).getComponents();
			for (Component component : components) {
				if (component instanceof JLabel) {
					return extractString(component);
				}
			}
			return null;
		} else if (obj instanceof JLabel) {
			return ((JLabel) obj).getText();
		} else {
			return obj.toString();
		}
	}

	/**
	 * A main method that will display the results of a few example pattern
	 * finder analyzers. Useful for tweaking the charts and UI.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LookAndFeelManager.getInstance().init();

		// run a small job
		AnalyzerBeansConfiguration conf = new JaxbConfigurationReader().create(new File(DataCleanerHome.get(), "conf.xml"));
		AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf);
		Datastore ds = conf.getDatastoreCatalog().getDatastore("orderdb");
		DataContextProvider dcp = ds.getDataContextProvider();
		Table table = dcp.getSchemaNavigator().convertToTable("PUBLIC.CUSTOMERS");
		ajb.setDatastore(ds);
		ajb.addSourceColumns(table.getLiteralColumns());
		ajb.addRowProcessingAnalyzer(PatternFinderAnalyzer.class).addInputColumns(ajb.getSourceColumns());

		WindowContext windowContext = new DCWindowContext();
		ResultWindow resultWindow = new ResultWindow(conf, ajb.toAnalysisJob(), null, windowContext);
		resultWindow.setVisible(true);
		resultWindow.startAnalysis();
	}
}
