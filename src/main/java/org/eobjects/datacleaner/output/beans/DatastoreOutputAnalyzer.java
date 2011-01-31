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
package org.eobjects.datacleaner.output.beans;

import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.output.OutputWriter;
import org.eobjects.datacleaner.output.datastore.DatastoreOutputWriterFactory;

@AnalyzerBean("Write to Datastore")
@OutputWriterAnalyzer
public class DatastoreOutputAnalyzer extends AbstractOutputWriterAnalyzer {

	/**
	 * Write mode for the datastore output analyzer. Determines if the datastore
	 * will be truncated before writing data or if a new/separate table should
	 * be created for this output.
	 */
	public static enum WriteMode {
		TRUNCATE, NEW_TABLE
	}

	private OutputWriter _outputWriter;

	@Configured
	String datastoreName;

	@Configured
	WriteMode writeMode = WriteMode.TRUNCATE;

	@Override
	public void configureForOutcome(AnalysisJobBuilder ajb, FilterBeanDescriptor<?, ?> descriptor, String categoryName) {
		final String dsName = ajb.getDataContextProvider().getDatastore().getName();
		datastoreName = "output-" + dsName + "-" + descriptor.getDisplayName() + "-" + categoryName;
	}

	@Override
	public OutputWriter getOutputWriter() {
		if (_outputWriter == null) {
			synchronized (this) {
				String[] headers = new String[columns.length];
				for (int i = 0; i < headers.length; i++) {
					headers[i] = columns[i].getName();
				}

				if (_outputWriter == null) {
					boolean truncate = (writeMode == WriteMode.TRUNCATE);
					_outputWriter = DatastoreOutputWriterFactory.getWriter(datastoreName, truncate, columns);
				}
			}
		}
		return _outputWriter;
	}

	@Override
	protected OutputAnalyzerResult getResultInternal(int rowCount) {
		return new DatastoreOutputAnalyzerResult(rowCount, datastoreName);
	}

	public void setOutputWriter(OutputWriter outputWriter) {
		_outputWriter = outputWriter;
	}

	public String getDatastoreName() {
		return datastoreName;
	}

	public void setDatastoreName(String datastoreName) {
		this.datastoreName = datastoreName;
	}
}