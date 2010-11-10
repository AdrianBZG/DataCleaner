package org.eobjects.datacleaner.output.csv;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.datacleaner.output.OutputRow;
import org.eobjects.datacleaner.output.OutputWriter;

import au.com.bytecode.opencsv.CSVWriter;

final class CsvOutputWriter implements OutputWriter {

	private final InputColumn<?>[] _columns;
	private final CSVWriter _csvWriter;
	private final String _filename;

	public CsvOutputWriter(CSVWriter csvWriter, String filename, 
			InputColumn<?>... columns) {
		_csvWriter = csvWriter;
		_filename = filename;
		_columns = columns;
	}

	@Override
	public void close() {
		CsvOutputWriterFactory.release(_filename);
	}

	public void createHeader(String... headers) {
		if (_columns.length != headers.length) {
			throw new IllegalStateException("Columns and header length doesn't match. Expected " + _columns.length
					+ " but found " + headers.length);
		}
		_csvWriter.writeNext(headers);
	}

	@Override
	public OutputRow createRow() {
		return new CsvOutputRow(_csvWriter, _columns);
	}

}
