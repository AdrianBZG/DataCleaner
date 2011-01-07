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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.MergeInput;
import org.eobjects.analyzer.job.MergedOutcome;
import org.eobjects.analyzer.job.MergedOutcomeJob;
import org.eobjects.analyzer.job.Outcome;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.RendererFactory;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.panels.ProgressInformationPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.LoadingIcon;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that displays a collection of results in task panes.
 * 
 * @author Kasper Sørensen
 */
public class ResultListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ResultListPanel.class);

	private final RendererFactory _rendererFactory;
	private final JXTaskPaneContainer _taskPaneContainer;
	private final ProgressInformationPanel _progressInformationPanel;

	public ResultListPanel(RendererFactory rendererFactory, ProgressInformationPanel progressInformationPanel) {
		super(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		_rendererFactory = rendererFactory;
		setLayout(new BorderLayout());
		_taskPaneContainer = WidgetFactory.createTaskPaneContainer();
		_progressInformationPanel = progressInformationPanel;
		add(WidgetUtils.scrolleable(_taskPaneContainer), BorderLayout.CENTER);
	}

	public void addResult(final AnalyzerJob analyzerJob, final AnalyzerResult result) {
		final JXTaskPane taskPane = new JXTaskPane();
		taskPane.setFocusable(false);

		AnalyzerBeanDescriptor<?> descriptor = analyzerJob.getDescriptor();
		taskPane.setIcon(IconUtils.getDescriptorIcon(descriptor));

		StringBuilder sb = new StringBuilder();

		String jobName = analyzerJob.getName();
		if (StringUtils.isNullOrEmpty(jobName)) {
			sb.append(descriptor.getDisplayName());
		} else {
			sb.append(jobName);
			sb.append(" (");
			sb.append(descriptor.getDisplayName());
			sb.append(')');
		}

		InputColumn<?>[] input = analyzerJob.getInput();
		if (input.length > 0) {
			sb.append(" (");
			if (input.length < 4) {
				for (int i = 0; i < input.length; i++) {
					if (i != 0) {
						sb.append(',');
					}
					sb.append(input[i].getName());
				}
			} else {
				sb.append(input.length);
				sb.append(" columns");
			}
			sb.append(")");
		}

		Outcome[] requirements = analyzerJob.getRequirements();
		if (requirements != null && requirements.length != 0) {
			sb.append(" (");
			for (int i = 0; i < requirements.length; i++) {
				if (i != 0) {
					sb.append(" ,");
				}
				appendRequirement(sb, requirements[i]);
			}
			sb.append(")");
		}

		final String resultLabel = sb.toString();
		taskPane.setTitle(resultLabel);
		taskPane.add(new LoadingIcon());
		_progressInformationPanel.addUserLog("Rendering result for " + resultLabel);

		synchronized (this) {
			_taskPaneContainer.add(taskPane);
		}

		// use a swing worker to run the rendering in the background
		new SwingWorker<JComponent, Task>() {

			@Override
			protected JComponent doInBackground() throws Exception {
				Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(result,
						SwingRenderingFormat.class);
				if (renderer == null) {
					throw new IllegalStateException("No renderer found for result type " + result.getClass().getName());
				}
				JComponent component = renderer.render(result);
				return component;
			}

			protected void done() {
				taskPane.removeAll();
				JComponent component;
				try {
					component = get();
					taskPane.add(component);
					_progressInformationPanel.addUserLog("Result rendered for " + resultLabel);
				} catch (Exception e) {
					logger.error("Error occurred while rendering result", e);
					_progressInformationPanel.addUserLog("Error occurred while rendering result", e);
					taskPane.add(new JLabel("An error occurred while rendering result, check the status tab"));
				}
				taskPane.updateUI();
			};

		}.execute();
	}

	private void appendRequirement(StringBuilder sb, Outcome req) {
		if (req instanceof FilterOutcome) {
			FilterJob filterJob = ((FilterOutcome) req).getFilterJob();
			Enum<?> category = ((FilterOutcome) req).getCategory();

			sb.append(filterJob.getDescriptor().getDisplayName());
			sb.append("=");
			sb.append(category);
		} else if (req instanceof MergedOutcome) {
			sb.append('[');
			MergedOutcomeJob mergedOutcomeJob = ((MergedOutcome) req).getMergedOutcomeJob();

			MergeInput[] mergeInputs = mergedOutcomeJob.getMergeInputs();
			for (int i = 0; i < mergeInputs.length; i++) {
				if (i != 0) {
					sb.append(',');
				}
				MergeInput mergeInput = mergeInputs[i];
				Outcome outcome = mergeInput.getOutcome();
				appendRequirement(sb, outcome);
			}
			sb.append(']');
		} else {
			// should not happen
			sb.append(req.toString());
		}
	}
}
