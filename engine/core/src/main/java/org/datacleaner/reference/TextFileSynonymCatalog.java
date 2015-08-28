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
package org.datacleaner.reference;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.configuration.DataCleanerConfiguration;

import au.com.bytecode.opencsv.CSVParser;

/**
 * Synonym catalog based on a text file.
 * 
 * Each line in the file should contain a master term with trailing
 * comma-separated synonyms.
 * 
 * Example:
 * 
 * <pre>
 * DK,Denmark,Danmark,Dänemark
 * NL,Holland,The Netherlands
 * FR,France
 * </pre>
 */
public final class TextFileSynonymCatalog extends AbstractReferenceData implements SynonymCatalog {

    private static final long serialVersionUID = 1L;

    private final String _filename;
    private final boolean _caseSensitive;
    private final String _encoding;

    public TextFileSynonymCatalog(String name, String filename, boolean caseSensitive, String encoding) {
        super(name);
        _filename = filename;
        _caseSensitive = caseSensitive;
        _encoding = encoding;
    }

    public TextFileSynonymCatalog(String name, File file, boolean caseSensitive, String encoding) {
        super(name);
        _filename = file.getPath();
        _caseSensitive = caseSensitive;
        _encoding = encoding;
    }

    @Override
    public SynonymCatalogConnection openConnection(DataCleanerConfiguration configuration) {
        final Map<String, String> synonyms = new HashMap<>();

        final CSVParser parser = new CSVParser(',', '"', '\\');
        final File file = new File(_filename);
        final BufferedReader reader = FileHelper.getBufferedReader(file, _encoding);
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                final String[] values = parser.parseLine(line);
                if (values.length > 1) {
                    for (int i = 1; i < values.length; i++) {
                        synonyms.put(values[i], values[0]);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(reader);
        }

        return new SimpleSynonymCatalog(getName(), synonyms).openConnection(configuration);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            final TextFileSynonymCatalog other = (TextFileSynonymCatalog) obj;
            return Objects.equals(_filename, other._filename) && Objects.equals(_caseSensitive, other._caseSensitive)
                    && Objects.equals(_encoding, other._encoding);
        }
        return false;
    }

    @Override
    public String toString() {
        return "TextFileSynonymCatalog[name=" + getName() + ", filename=" + _filename + ", caseSensitive="
                + _caseSensitive + ", encoding=" + _encoding + "]";
    }

    public String getEncoding() {
        return _encoding;
    }

    public String getFilename() {
        return _filename;
    }

    public boolean isCaseSensitive() {
        return _caseSensitive;
    }
}
