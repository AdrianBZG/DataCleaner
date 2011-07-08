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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * {@link PropertyWidget} for String arrays. Displays string arrays as a set of
 * text boxes and plus/minus buttons to grow/shrink the array.
 * 
 * @author Kasper Sørensen
 */
public class MultipleStringPropertyWidget extends AbstractPropertyWidget<String[]> {

	private static final long serialVersionUID = 1L;
	private final DCPanel _textFieldPanel;

	public MultipleStringPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		_textFieldPanel = new DCPanel();
		_textFieldPanel.setLayout(new VerticalLayout(2));

		String[] currentValue = (String[]) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue == null) {
			currentValue = new String[2];
		}
		updateComponents(currentValue);

		final JButton addButton = WidgetFactory.createSmallButton("images/actions/add.png");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTextField("", true);
				fireValueChanged();
			}
		});

		final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int componentCount = _textFieldPanel.getComponentCount();
				if (componentCount > 0) {
					_textFieldPanel.remove(componentCount - 1);
					_textFieldPanel.updateUI();
					fireValueChanged();
				}
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
		buttonPanel.setLayout(new VerticalLayout(2));
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);

		final DCPanel outerPanel = new DCPanel();
		outerPanel.setLayout(new BorderLayout());

		outerPanel.add(_textFieldPanel, BorderLayout.CENTER);
		outerPanel.add(buttonPanel, BorderLayout.EAST);

		add(outerPanel);
	}

	public void updateComponents(String[] values) {
		if (values == null) {
			values = new String[2];
		}
		final String[] previousValues = getValue();
		if (!EqualsBuilder.equals(values, previousValues)) {
			for (int i = 0; i < Math.min(previousValues.length, values.length); i++) {
				// modify text boxes
				if (!EqualsBuilder.equals(previousValues[i], values[i])) {
					JTextComponent component = (JTextComponent) _textFieldPanel.getComponent(i);
					component.setText(values[i]);
				}
			}

			while (_textFieldPanel.getComponentCount() < values.length) {
				// add text boxes if there are too few
				String nextValue = values[_textFieldPanel.getComponentCount()];
				addTextField(nextValue, false);
			}

			while (_textFieldPanel.getComponentCount() > values.length) {
				// remove text boxes if there are too many
				_textFieldPanel.remove(_textFieldPanel.getComponentCount() - 1);
			}
			_textFieldPanel.updateUI();
		}
	}

	private void addTextField(String value, boolean updateUI) {
		JXTextField textField = WidgetFactory.createTextField();
		if (value != null) {
			textField.setText(value);
		}
		textField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				fireValueChanged();
			}
		});
		_textFieldPanel.add(textField);
		if (updateUI) {
			_textFieldPanel.updateUI();
		}
	}

	@Override
	public String[] getValue() {
		Component[] components = _textFieldPanel.getComponents();
		String[] result = new String[components.length];
		for (int i = 0; i < components.length; i++) {
			JXTextField textField = (JXTextField) components[i];
			result[i] = textField.getText();
		}
		return result;
	}

	@Override
	protected void setValue(String[] value) {
		updateComponents(value);
	}

}
