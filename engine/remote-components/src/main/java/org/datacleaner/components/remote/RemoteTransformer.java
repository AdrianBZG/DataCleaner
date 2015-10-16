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
package org.datacleaner.components.remote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.util.EqualsBuilder;
import org.datacleaner.api.*;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.restclient.*;
import org.datacleaner.util.batch.BatchSink;
import org.datacleaner.util.batch.BatchSource;
import org.datacleaner.util.batch.BatchTransformer;
import org.datacleaner.util.convert.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Transformer that is actually a proxy to a remote transformer sitting at DataCleaner Monitor server.
 * Instances of this transformer can be created only by
 * {@link org.datacleaner.descriptors.RemoteTransformerDescriptorImpl} component descriptors.
 *
 * @Since 9/1/15
 */
public class RemoteTransformer extends BatchTransformer {

    private static final Logger logger = LoggerFactory.getLogger(RemoteTransformer.class);
    private static final ObjectMapper mapper = Serializator.getJacksonObjectMapper();

    private String baseUrl;
    private String componentDisplayName;
    private String username;
    private String password;
    private OutputColumns cachedOutputColumns;
    private ComponentRESTClient client;
    private Map<String, Object> configuredProperties = new TreeMap<>();

    public RemoteTransformer(String baseUrl, String componentDisplayName, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.componentDisplayName = componentDisplayName;
    }

    @Initialize
    public void initClient() {
        logger.debug("Initializing '{}' @{}", componentDisplayName, this.hashCode());
        client = new ComponentRESTClient(baseUrl, username, password);
    }

    @Close
    public void closeClient() {
        logger.debug("closing '{}' @{}", componentDisplayName, this.hashCode());
        client = null;
        // TODO: client now misses a "close" method (although Jersey client has a "destroy" method).
    }

    @Override
    public OutputColumns getOutputColumns() {
        OutputColumns outCols = cachedOutputColumns;
        if(outCols != null) { return outCols; }

        boolean wasInit = false;
        if(client == null) {
            wasInit = true;
            initClient();
        }
        try {
            CreateInput createInput = new CreateInput();
            createInput.configuration = getConfiguration(getUsedInputColumns());

            org.datacleaner.restclient.OutputColumns columnsSpec = client.getOutputColumns(componentDisplayName, createInput);

            outCols = new OutputColumns(columnsSpec.getColumns().size(), Object.class);
            int i = 0;
            for (org.datacleaner.restclient.OutputColumns.OutputColumn colSpec : columnsSpec.getColumns()) {
                outCols.setColumnName(i, colSpec.name);
                try {
                    outCols.setColumnType(i, Class.forName(colSpec.type));
                } catch (ClassNotFoundException e) {
                    // TODO: what to do with data types - classes that are not on our classpath?
                    // Provide it as pure JsonNode?
                    outCols.setColumnType(i, JsonNode.class);
                }
                i++;
            }
            cachedOutputColumns = outCols;
            return outCols;
        } catch(Exception e) {
            return new OutputColumns(String.class, "Unknown");
        } finally {
            if(wasInit) {
                closeClient();
            }
        }
    }

    private ComponentConfiguration getConfiguration(List<InputColumn> inputColumns) {
        ComponentConfiguration configuration = new ComponentConfiguration();
        for(Map.Entry<String, Object> propertyE: configuredProperties.entrySet()) {
            configuration.getProperties().put(propertyE.getKey(), mapper.valueToTree(propertyE.getValue()));
        }

        for(InputColumn col: inputColumns) {
            configuration.getColumns().add(ComponentsRestClientUtils.createInputColumnSpecification(
                    col.getName(),
                    col.getDataType(),
                    ColumnTypeImpl.convertColumnType(col.getDataType()).getName(),
                    mapper.getNodeFactory()));
        }
        return configuration;
    }

    private List<InputColumn> getUsedInputColumns() {
        ArrayList<InputColumn> columns = new ArrayList<>();
        for(Object propValue: configuredProperties.values()) {
            if(propValue instanceof InputColumn) {
                columns.add((InputColumn) propValue);
            } else if(propValue instanceof InputColumn[]) {
                for(InputColumn col: ((InputColumn[])propValue)) {
                    columns.add(col);
                }
            } else if(propValue instanceof Collection) {
                for(Object value: ((Collection)propValue)) {
                    if(value instanceof InputColumn) {
                        columns.add((InputColumn)value);
                    } else {
                        // don't iterate the rest if the first item is not an input column.
                        break;
                    }
                }
            }
            // TODO: are maps possible?
        }
        return columns;
    }

    private void convertOutputRows(JsonNode rows, BatchSink<Object[]> sink, int sinkSize) {
        OutputColumns outCols = getOutputColumns();
        if(rows == null || rows.size() < 1) { throw new RuntimeException("Expected exactly 1 row in response"); }

        int rowI = 0;
        for(JsonNode row: rows) {
            if(rowI >= sinkSize) {
                throw new RuntimeException("Expected " + sinkSize + " rows, but got more");
            }
            List values = new ArrayList();
            int i = 0;
            for(JsonNode value: row) {
                // TODO: should JsonNode be the default?
                Class cl = String.class;
                if(i < outCols.getColumnCount()) {
                    cl = outCols.getColumnType(i);
                }
                values.add(convertOutputValue(value, cl));
                i++;
            }
            logger.debug("Setting output {}. Sink: @{}", rowI, sink.hashCode());
            sink.setOutput(rowI, values.toArray(new Object[values.size()]));
            rowI++;
        }
        if(rowI < sinkSize) {
            throw new RuntimeException("Expected " + sinkSize + " rows, but got only " + rowI);
        }
    }

    private Object convertOutputValue(JsonNode value, Class cl) {
        try {
            if(cl == JsonNode.class) {
                return value;
            }
            if(cl == File.class) {
                return StringConverter.simpleInstance().deserialize(value.asText(), cl);
            }
            return mapper.readValue(value.traverse(), cl);
        } catch(Exception e) {
            throw new RuntimeException("Cannot convert table value of type '" + cl + "': " + value.toString(), e);
        }
    }

    public void setPropertyValue(String propertyName, Object value) {
        if(EqualsBuilder.equals(value, configuredProperties.get(propertyName))) {
            return;
        }
        logger.debug("Setting '{}'.'{}' = {}", componentDisplayName, propertyName, value);

        if(value == null) {
            configuredProperties.remove(propertyName);
        } else {
            configuredProperties.put(propertyName, value);
        }
        // invalidate the cached output columns
        cachedOutputColumns = null;
    }

    public Object getPropertyValue(String propertyName) {
        return configuredProperties.get(propertyName);
    }

    @Override
    public void map(BatchSource<InputRow> source, BatchSink<Object[]> sink) {
        List<InputColumn> cols = getUsedInputColumns();
        int size = source.size();
        Object[] rows = new Object[size];
        for(int i = 0; i < size; i++) {
            InputRow inputRow = source.getInput(i);
            Object[] values = new Object[cols.size()];
            int j = 0;
            for(InputColumn col: cols) {
                values[j] = inputRow.getValue(col);
                j++;
            }
            rows[i] = values;
        }

        ProcessStatelessInput input = new ProcessStatelessInput();
        input.configuration = getConfiguration(cols);
        input.data = mapper.valueToTree(rows);
        logger.debug("Processing remotely {} rows", size);
        ProcessStatelessOutput out = client.processStateless(componentDisplayName, input);
        convertOutputRows(out.rows, sink, size);
    }

}
