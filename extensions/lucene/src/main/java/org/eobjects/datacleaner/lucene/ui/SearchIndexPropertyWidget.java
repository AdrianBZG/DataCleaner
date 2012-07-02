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
package org.eobjects.datacleaner.lucene.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.lucene.SearchIndex;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.properties.AbstractPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.jdesktop.swingx.HorizontalLayout;

/**
 * A {@link PropertyWidget} for selecting a {@link SearchIndex} in a combobox.
 */
public class SearchIndexPropertyWidget extends AbstractPropertyWidget<SearchIndex> {

    private final DCComboBox<String> _comboBox;
    private final SearchIndexCatalog _catalog;
    private final WindowContext _windowContext;

    public SearchIndexPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor, SearchIndexCatalog catalog, WindowContext windowContext) {
        super(beanJobBuilder, propertyDescriptor);

        _catalog = catalog;
        _windowContext = windowContext;

        final String[] names = catalog.getSearchIndexNames();
        _comboBox = new DCComboBox<String>(names);

        final SearchIndex currentValue = getCurrentValue();
        if (currentValue != null) {
            _comboBox.setSelectedItem(currentValue.getName());
        }

        final ImageIcon icon = ImageManager.getInstance().getImageIcon("images/search_index.png",
                IconUtils.ICON_SIZE_MEDIUM, getClass().getClassLoader());
        final JButton button = new JButton("Configure indices", icon);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final ConfigureSearchIndicesDialog dialog = new ConfigureSearchIndicesDialog(_windowContext, _catalog);
                dialog.open();
            }
        });

        final DCPanel panel = new DCPanel();
        panel.setLayout(new HorizontalLayout());
        panel.add(_comboBox);
        panel.add(Box.createHorizontalStrut(4));
        panel.add(button);
        add(panel);
    }

    @Override
    public SearchIndex getValue() {
        String name = _comboBox.getSelectedItem();
        return _catalog.getSearchIndex(name);
    }

    @Override
    protected void setValue(SearchIndex value) {
        _comboBox.setSelectedItem(value.getName());
    }

}
