package org.eobjects.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;

import org.eobjects.analyzer.beans.api.RowProcessingAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.RowProcessingAnalyzerJobBuilder;
import org.eobjects.datacleaner.output.beans.AbstractOutputWriterAnalyzer;
import org.eobjects.datacleaner.output.beans.OutputWriterAnalyzer;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.properties.ChangeRequirementButton;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.tooltip.DescriptorMenuItem;
import org.jdesktop.swingx.VerticalLayout;

public class FilterJobBuilderPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final AnalyzerBeansConfiguration _configuration;
	private final PropertyWidgetFactory _propertyWidgetFactory;
	private final FilterJobBuilder<?, ?> _filterJobBuilder;
	private final ChangeRequirementButton _requirementButton;
	private final AnalysisJobBuilder _analysisJobBuilder;
	private final FilterBeanDescriptor<?, ?> _descriptor;

	public FilterJobBuilderPanel(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder,
			FilterJobBuilder<?, ?> filterJobBuilder) {
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;
		_filterJobBuilder = filterJobBuilder;
		_propertyWidgetFactory = new PropertyWidgetFactory(analysisJobBuilder, filterJobBuilder);
		_requirementButton = new ChangeRequirementButton(analysisJobBuilder, filterJobBuilder);
		_descriptor = _filterJobBuilder.getDescriptor();

		final JButton removeButton = new JButton("Remove filter", ImageManager.getInstance().getImageIcon(
				"images/actions/remove.png", IconUtils.ICON_SIZE_SMALL));
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_analysisJobBuilder.removeFilter(_filterJobBuilder);
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new VerticalLayout(4));
		buttonPanel.add(_requirementButton);
		buttonPanel.add(removeButton);

		WidgetUtils.addToGridBag(buttonPanel, this, 2, 0, 1, 1, GridBagConstraints.NORTHEAST, 4);

		int i = 0;
		for (ConfiguredPropertyDescriptor propertyDescriptor : _descriptor.getConfiguredProperties()) {
			JLabel label = new JLabel(propertyDescriptor.getName());
			label.setOpaque(false);
			WidgetUtils.addToGridBag(label, this, 0, i, 1, 1, GridBagConstraints.NORTHEAST, 4);

			PropertyWidget<?> propertyWidget = _propertyWidgetFactory.create(propertyDescriptor);
			WidgetUtils.addToGridBag(propertyWidget.getWidget(), this, 1, i, 1, 1, GridBagConstraints.NORTHWEST, 4);
			i++;
		}

		final DCPanel outcomePanel = new DCPanel();
		outcomePanel.setBorder(new TitledBorder("Outcomes"));

		final Set<String> categoryNames = _descriptor.getCategoryNames();
		for (final String categoryName : categoryNames) {
			final JButton outcomeButton = new JButton(categoryName);
			outcomeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JPopupMenu popup = new JPopupMenu();

					Collection<AnalyzerBeanDescriptor<?>> descriptors = _configuration.getDescriptorProvider()
							.getAnalyzerBeanDescriptors();
					for (final AnalyzerBeanDescriptor<?> descriptor : descriptors) {
						if (descriptor.isRowProcessingAnalyzer()
								&& descriptor.getAnnotation(OutputWriterAnalyzer.class) != null) {

							JMenuItem outputWriterMenuItem = new DescriptorMenuItem(descriptor);
							outputWriterMenuItem.addActionListener(new ActionListener() {
								@SuppressWarnings("unchecked")
								@Override
								public void actionPerformed(ActionEvent e) {
									Class<? extends RowProcessingAnalyzer<?>> beanClass = (Class<? extends RowProcessingAnalyzer<?>>) descriptor
											.getBeanClass();

									RowProcessingAnalyzerJobBuilder<? extends RowProcessingAnalyzer<?>> ajb = _analysisJobBuilder
											.addRowProcessingAnalyzer(beanClass);

									RowProcessingAnalyzer<?> configurableBean = ajb.getConfigurableBean();
									if (configurableBean instanceof AbstractOutputWriterAnalyzer) {
										((AbstractOutputWriterAnalyzer) configurableBean).configureForOutcome(_descriptor,
												categoryName);
									}

									ajb.setRequirement(_filterJobBuilder, categoryName);
									ajb.onConfigurationChanged();
								}
							});
							popup.add(outputWriterMenuItem);
						}
					}

					popup.show(outcomeButton, 0, outcomeButton.getHeight());
				}
			});
			outcomePanel.add(outcomeButton);
		}

		WidgetUtils.addToGridBag(outcomePanel, this, 1, i, 2, 1, GridBagConstraints.NORTHWEST, 4);
	}

	public PropertyWidgetFactory getPropertyWidgetFactory() {
		return _propertyWidgetFactory;
	}

	public void onRequirementChanged() {
		_requirementButton.updateText();
	}
}
