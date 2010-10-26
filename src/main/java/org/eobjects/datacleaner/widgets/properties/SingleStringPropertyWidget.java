package org.eobjects.datacleaner.widgets.properties;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTextField;

public class SingleStringPropertyWidget extends AbstractPropertyWidget<String> {

	private static final long serialVersionUID = 1L;

	private final JXTextField _textField;

	public SingleStringPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(propertyDescriptor);
		_textField = WidgetUtils.createTextField(propertyDescriptor.getName());
		String currentValue = (String) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_textField.setText(currentValue);
		}
		_textField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				fireValueChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				fireValueChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				fireValueChanged();
			}
		});
		add(_textField);
	}

	@Override
	public boolean isSet() {
		return _textField.getText() != null && _textField.getText().length() > 0;
	}

	@Override
	public String getValue() {
		return _textField.getText();
	}
}
