package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

public class SelectDatastorePanel extends DCPanel {
    private static final long serialVersionUID = 1L;

    public SelectDatastorePanel() {
        setBorder(new CompoundBorder(WidgetUtils.BORDER_THIN, new EmptyBorder(10, 10, 10, 10)));
        setLayout(new GridBagLayout());

        DCLabel newDatastoreLabel = DCLabel.dark("Use new datastore");
        newDatastoreLabel.setFont(WidgetUtils.FONT_BANNER);
        newDatastoreLabel.setForeground(WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        add(newDatastoreLabel, c);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(new AddDataStorePanel(), c);
        
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 2;
        c.fill = GridBagConstraints.VERTICAL;
        add(new JSeparator(JSeparator.VERTICAL), c);
        
        DCLabel existingDatastoreLabel = DCLabel.dark("Use existing datastore");
        existingDatastoreLabel.setFont(WidgetUtils.FONT_BANNER);
        existingDatastoreLabel.setForeground(WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(0, 10, 0, 0);
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        add(existingDatastoreLabel, c);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        add(new ExistingDatastorePanel(), c);
    }
    
    public static void main(String[] args) {
        LookAndFeelManager.get().init();
        
        final JFrame frame = new JFrame("Create datastore test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.add(new SelectDatastorePanel(), BorderLayout.PAGE_START);
        frame.pack();
        frame.setVisible(true);
    }

}
