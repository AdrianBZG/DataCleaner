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
package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.windows.AnalysisJobBuilderWindow;

import com.google.inject.Guice;
import com.google.inject.Injector;

@Singleton
public final class NewAnalysisJobActionListener implements ActionListener {

	private final AnalyzerBeansConfiguration _configuration;
	private final WindowContext _windowContext;

	@Inject
	protected NewAnalysisJobActionListener(AnalyzerBeansConfiguration configuration, WindowContext windowContext) {
		_configuration = configuration;
		_windowContext = windowContext;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Injector injector = Guice.createInjector(new DCModule(_configuration, _windowContext));
		injector.getInstance(AnalysisJobBuilderWindow.class).setVisible(true);
	}

}
