package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobWriter;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

public final class SaveAnalysisJobActionListener implements ActionListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final AnalysisJobBuilderWindow _window;

	public SaveAnalysisJobActionListener(AnalysisJobBuilderWindow window, AnalysisJobBuilder analysisJobBuilder) {
		_window = window;
		_analysisJobBuilder = analysisJobBuilder;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		AnalysisJob analysisJob = null;
		try {
			analysisJob = _analysisJobBuilder.toAnalysisJob();
		} catch (Exception e) {
			WidgetUtils
					.showErrorMessage("Errors in job", "Please fix the errors that exist in the job before saving it:\n\n"
							+ _window.getStatusLabelText(), null);
			return;
		}

		UserPreferences userPreferences = UserPreferences.getInstance();

		JFileChooser fileChooser = new JFileChooser(userPreferences.getAnalysisJobDirectory());
		fileChooser.setFileFilter(FileFilters.ANALYSIS_XML);

		int result = fileChooser.showSaveDialog(_window);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();

			if (!file.getName().endsWith(".xml")) {
				file = new File(file.getParentFile(), file.getName() + FileFilters.ANALYSIS_XML.getExtension());
			}

			userPreferences.setAnalysisJobDirectory(file.getParentFile());

			if (file.exists()) {
				int overwrite = JOptionPane.showConfirmDialog(_window, "Are you sure you want to overwrite the file '"
						+ file.getName() + "'?", "Overwrite existing file?", JOptionPane.YES_NO_OPTION);
				if (overwrite != JOptionPane.YES_OPTION) {
					return;
				}
			}

			JaxbJobWriter writer = new JaxbJobWriter();
			BufferedOutputStream outputStream = null;
			try {
				outputStream = new BufferedOutputStream(new FileOutputStream(file));
				writer.write(analysisJob, outputStream);
				outputStream.flush();
				outputStream.close();
			} catch (IOException e1) {
				throw new IllegalStateException(e1);
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception e2) {
						// do nothing
					}
				}
			}

			_window.setJobFilename(file.getName());
		}
	}
}
