/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.datacleaner.actions.OpenAnalysisJobActionListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.OpenAnalysisJobMenuItem;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.datacleaner.windows.AnalysisJobBuilderWindow.AnalysisWindowPanelType;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomePanel extends DCPanel {
    private static final Logger logger = LoggerFactory.getLogger(WelcomePanel.class);

    private static final long serialVersionUID = 1L;

    private static final Font WELCOME_BANNER_FONT = WidgetUtils.FONT_UBUNTU_PLAIN.deriveFont(30f);
    private static final Font WELCOME_SUBBANNER_FONT = WidgetUtils.FONT_UBUNTU_PLAIN.deriveFont(24f);
    private static final Font WELCOME_OTHER_OPTIONS_FONT = WidgetUtils.FONT_HEADER2;

    private static final Border CONTAINER_BORDER = new EmptyBorder(20, 40, 20, 40);

    private static final Image BACKGROUND_IMAGE = ImageManager.get().getImage(
            "images/window/welcome-panel-background.jpg");

    private final UserPreferences _userPreferences;
    private final AnalysisJobBuilderWindow _window;
    private final OpenAnalysisJobActionListener _openAnalysisJobActionListener;

    public WelcomePanel(final AnalysisJobBuilderWindow window, final UserPreferences userPreferences,
            final OpenAnalysisJobActionListener openAnalysisJobActionListener) {
        super(BACKGROUND_IMAGE, 50, 100, WidgetUtils.BG_COLOR_DARKEST);
        _window = window;
        _userPreferences = userPreferences;
        _openAnalysisJobActionListener = openAnalysisJobActionListener;

        setBorder(CONTAINER_BORDER);
        setLayout(new BorderLayout());

        final DCLabel welcomeLabel = new DCLabel(false, "Welcome!", WidgetUtils.BG_COLOR_BLUE_MEDIUM, null);
        welcomeLabel.setFont(WELCOME_BANNER_FONT);
        welcomeLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        final DCPanel containerPanel = createWizardListPanel();
        add(containerPanel, BorderLayout.CENTER);

        final DCPanel buttonPanel = createButtonPanel();
        buttonPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        add(buttonPanel, BorderLayout.SOUTH);

    }

    private DCPanel createButtonPanel() {
        final DCLabel otherOptionsLabel = DCLabel.bright("Other options:");
        otherOptionsLabel.setFont(WELCOME_OTHER_OPTIONS_FONT);

        final JButton newJobButton = WidgetFactory.createPrimaryButton("New job from scratch", IconUtils.MODEL_JOB);
        newJobButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _window.changePanel(AnalysisWindowPanelType.SELECT_DS);
            }
        });

        final PopupButton recentJobsButton = WidgetFactory.createDarkPopupButton("Recent jobs",
                IconUtils.FILE_HOME_FOLDER);

        final JButton browseJobsButton = WidgetFactory.createDarkButton("Browse jobs", IconUtils.FILE_FOLDER);
        browseJobsButton.addActionListener(_openAnalysisJobActionListener);

        final List<FileObject> recentJobFiles = getRecentJobFiles();
        final JPopupMenu recentJobsMenu = recentJobsButton.getMenu();
        for (int i = 0; i < recentJobFiles.size(); i++) {
            final FileObject jobFile = recentJobFiles.get(i);
            final JMenuItem menuItem = new OpenAnalysisJobMenuItem(jobFile, _openAnalysisJobActionListener);
            recentJobsMenu.add(menuItem);
        }

        final JButton manageDatastoresButton = WidgetFactory.createDarkButton("Manage datastores",
                IconUtils.GENERIC_DATASTORE_IMAGEPATH);
        manageDatastoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _window.changePanel(AnalysisWindowPanelType.MANAGE_DS);
            }
        });

        final DCPanel buttonPanel = new DCPanel(WidgetUtils.BG_SEMI_TRANSPARENT_BRIGHT);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        // buttonPanel.add(otherOptionsLabel);
        // buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(newJobButton);
        buttonPanel.add(browseJobsButton);
        buttonPanel.add(recentJobsButton);
        buttonPanel.add(manageDatastoresButton);

        final DCPanel containerPanel = new DCPanel(WidgetUtils.BG_SEMI_TRANSPARENT_BRIGHT);
        containerPanel.setLayout(new BorderLayout());
        containerPanel.setBorder(CONTAINER_BORDER);
        containerPanel.add(buttonPanel, BorderLayout.CENTER);

        return containerPanel;
    }

    private DCPanel createWizardListPanel() {
        final DCPanel wizardListPanel = new DCPanel(WidgetUtils.BG_SEMI_TRANSPARENT_BRIGHT);
        wizardListPanel.setLayout(new VerticalLayout(10));
        wizardListPanel.setBorder(CONTAINER_BORDER);

        final DCLabel subtitleLabel = DCLabel.bright("What's your question for DataCleaner?");
        subtitleLabel.setFont(WELCOME_SUBBANNER_FONT);
        wizardListPanel.add(subtitleLabel);
        subtitleLabel.setBorder(WidgetUtils.BORDER_EMPTY);
        final DetailPanel questionPanel1 = new DetailPanel(
                "<html>Are my <b>addresses correct</b> and <b>up-to-date</b>?</html>",
                "Use the Neopost Address Correction and Mail Suppression services on your contact list to correct your addresses and check if people have moved to new places or if they have passed away.");
        questionPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                _window.changePanel(AnalysisWindowPanelType.SELECT_DS);
            }
        });
        wizardListPanel.add(questionPanel1);

        final DetailPanel questionPanel2 = new DetailPanel(
                "<html>Do I have <b>duplicate</b> customers?</html>",
                "Inspect your customers with DataCleaner’s Duplicate Detection function to identify the possible duplicated records in your database or file.");
        wizardListPanel.add(questionPanel2);

        final DetailPanel questionPanel3 = new DetailPanel(
                "<html>Are my records properly <b>filled</b>?</html>",
                "Validate the proper completeness and conformity with rules of your records. Use this wizard to configure common data profiling features to suit the fields of your data set.");
        wizardListPanel.add(questionPanel3);

        return wizardListPanel;
    }

    private List<FileObject> getRecentJobFiles() {
        final List<FileObject> recentJobFiles = _userPreferences.getRecentJobFiles();
        final List<FileObject> result = new ArrayList<>();
        for (FileObject fileObject : recentJobFiles) {
            try {
                if (fileObject.exists()) {
                    result.add(fileObject);
                    if (result.size() == 10) {
                        break;
                    }
                }
            } catch (FileSystemException ex) {
                logger.debug("Skipping file {} because of unexpected error", ex);
            }
        }
        return result;
    }

}
