package org.eobjects.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.util.DisplayNameComparator;
import org.eobjects.datacleaner.widgets.tooltip.DescriptorMenuItem;

public final class AddTransformerActionListener implements ActionListener {

	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisJobBuilder _analysisJobBuilder;

	public AddTransformerActionListener(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JPopupMenu popup = new JPopupMenu();

		Collection<TransformerBeanDescriptor<?>> descriptors = _configuration.getDescriptorProvider()
				.getTransformerBeanDescriptors();
		descriptors = CollectionUtils.sorted(descriptors, new DisplayNameComparator());
		for (final TransformerBeanDescriptor<?> descriptor : descriptors) {
			JMenuItem menuItem = new DescriptorMenuItem(descriptor);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					_analysisJobBuilder.addTransformer(descriptor);
				}
			});
			popup.add(menuItem);
		}

		Component source = (Component) e.getSource();
		popup.show(source, 0, source.getHeight());
	}
}
