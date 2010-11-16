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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.ProgressInformationPanel;
import org.eobjects.datacleaner.util.AnalysisRunnerSwingWorker;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;

import dk.eobjects.metamodel.schema.Table;

public final class ResultWindow extends AbstractWindow {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.getInstance();

	private final CloseableTabbedPane _tabbedPane = new CloseableTabbedPane();
	private final Map<Table, ResultListPanel> _resultPanels = new HashMap<Table, ResultListPanel>();
	private final AnalysisJob _job;
	private final AnalyzerBeansConfiguration _configuration;
	private final ProgressInformationPanel _progressInformationPanel;
	private final RendererFactory _rendererFactory;

	public ResultWindow(AnalyzerBeansConfiguration configuration, AnalysisJob job) {
		super();
		_configuration = configuration;
		_job = job;
		_rendererFactory = new RendererFactory(configuration.getDescriptorProvider());

		_progressInformationPanel = new ProgressInformationPanel();
		_tabbedPane.addTab("Progress information", imageManager.getImageIcon("images/model/result.png"),
				_progressInformationPanel);
		_tabbedPane.setUnclosableTab(0);
	}

	public void startAnalysis() {
		AnalysisRunnerSwingWorker worker = new AnalysisRunnerSwingWorker(_configuration, _job, this,
				_progressInformationPanel);
		worker.execute();
	}

	private void addTableResultPanel(Table table) {
		ResultListPanel panel = new ResultListPanel(_rendererFactory);
		_resultPanels.put(table, panel);
		_tabbedPane.addTab(table.getName(), imageManager.getImageIcon("images/model/table.png"), panel);
	}

	private ResultListPanel getTableResultPanel(Table table) {
		if (!_resultPanels.containsKey(table)) {
			addTableResultPanel(table);
		}
		return _resultPanels.get(table);
	}

	public void addResult(Table table, AnalyzerJob analyzerJob, AnalyzerResult result) {
		getTableResultPanel(table).addResult(analyzerJob, result);
		if (_tabbedPane.getTabCount() == 2) {
			// switch to the first available result panel
			_tabbedPane.setSelectedIndex(1);
		}
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	protected String getWindowTitle() {
		return "Analysis results";
	}

	@Override
	protected Image getWindowIcon() {
		return imageManager.getImage("images/model/result.png");
	}

	@Override
	protected JComponent getWindowContent() {
		DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		panel.setLayout(new BorderLayout());
		panel.add(new DCBannerPanel(getWindowTitle()), BorderLayout.NORTH);
		panel.add(_tabbedPane, BorderLayout.CENTER);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;

		int height = 550;
		if (screenHeight > 1000) {
			height = 900;
		} else if (screenHeight > 750) {
			height = 700;
		}

		int width = 750;
		if (screenWidth > 1200) {
			width = 1100;
		} else if (screenWidth > 1000) {
			width = 900;
		}

		panel.setPreferredSize(width, height);
		return panel;
	}
}
