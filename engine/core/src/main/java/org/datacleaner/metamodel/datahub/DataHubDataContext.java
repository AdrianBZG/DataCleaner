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
package org.datacleaner.metamodel.datahub;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static org.datacleaner.metamodel.datahub.DataHubConnection.DATASTORES_PATH;
import static org.datacleaner.metamodel.datahub.DataHubConnection.DEFAULT_SCHEMA;
import static org.datacleaner.metamodel.datahub.DataHubConnection.SCHEMA_EXTENSION;
import static org.datacleaner.metamodel.datahub.DataHubConnectionHelper.validateReponseStatusCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.metamodel.AbstractDataContext;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Schema;
import org.datacleaner.metamodel.datahub.utils.JsonSchemasResponseParser;
import org.datacleaner.util.http.MonitorHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataHubDataContext extends AbstractDataContext implements UpdateableDataContext {
    private static final Logger logger = LoggerFactory.getLogger(DataHubDataContext.class);

    private DataHubConnection _connection;
    private Map<String, DataHubSchema> _schemas;

    public DataHubDataContext(DataHubConnection connection) {
        _connection = connection;
        _schemas = getDatahubSchemas();
    }

    private Map<String, DataHubSchema> getDatahubSchemas() {
        Map<String, DataHubSchema> schemas = new HashMap<String, DataHubSchema>();
        for (final String datastoreName : getDataStoreNames()) {
            final String uri = _connection.getRepositoryUrl() + DATASTORES_PATH + "/"
                    + urlPathSegmentEscaper().escape(datastoreName) + SCHEMA_EXTENSION;
            logger.debug("request {}", uri);
            final HttpGet request = new HttpGet(uri);
            final HttpResponse response = executeRequest(request);
            final HttpEntity entity = response.getEntity();
            final JsonSchemasResponseParser parser = new JsonSchemasResponseParser();
            try {
                final DataHubSchema schema = parser.parseJsonSchema(entity.getContent());
                schema.setDatastoreName(datastoreName);
                schemas.put(schema.getName(), schema);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return schemas;
    }

    @Override
    public void executeUpdate(UpdateScript script) {
        final DataHubUpdateCallback callback = new DataHubUpdateCallback(this);
        try {
            script.run(callback);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Override
    public DataSet executeQuery(final Query query) {
        return new DataHubDataSet(query, _connection);
    }

    private List<String> getDataStoreNames() {
        String uri = _connection.getRepositoryUrl() + DATASTORES_PATH;
        logger.debug("request {}", uri);
        HttpGet request = new HttpGet(uri);
        HttpResponse response = executeRequest(request);
        HttpEntity entity = response.getEntity();
        JsonSchemasResponseParser parser = new JsonSchemasResponseParser();
        try {
            return parser.parseDataStoreArray(entity.getContent());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private HttpResponse executeRequest(HttpGet request) {

        MonitorHttpClient httpClient = _connection.getHttpClient();
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        validateReponseStatusCode(response);
        
        return response;
    }

    @Override
    protected String[] getSchemaNamesInternal() {
        return _schemas.keySet().toArray(new String[_schemas.size()]);
    }

    @Override
    protected String getDefaultSchemaName() {
        return DEFAULT_SCHEMA;
    }

    @Override
    protected Schema getSchemaByNameInternal(String name) {
        return _schemas.get(name);
    }

    public DataHubConnection getConnection() {
        return _connection;
    }

}
