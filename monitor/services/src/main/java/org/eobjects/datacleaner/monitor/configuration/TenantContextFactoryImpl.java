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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.concurrent.ConcurrentHashMap;

import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.configuration.InjectionManagerFactoryImpl;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory and tenant-wise cache for {@link TenantContext} objects.
 */
@Component("tenantContextFactory")
public class TenantContextFactoryImpl implements TenantContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextFactoryImpl.class);

    private final ConcurrentHashMap<String, TenantContext> _contexts;
    private final Repository _repository;
    private final InjectionManagerFactory _injectionManagerFactory;

    /**
     * Constructs a {@link TenantContextFactoryImpl}.
     * 
     * @param repository
     * @deprecated use
     *             {@link #TenantContextFactoryImpl(Repository, InjectionManagerFactory)}
     *             instead.
     */
    @Deprecated
    public TenantContextFactoryImpl(Repository repository) {
        this(repository, new InjectionManagerFactoryImpl());
    }

    /**
     * Constructs a {@link TenantContextFactoryImpl}.
     * 
     * @param repository
     * @param injectionManagerFactory
     */
    @Autowired
    public TenantContextFactoryImpl(Repository repository, InjectionManagerFactory injectionManagerFactory) {
        _repository = repository;
        _injectionManagerFactory = injectionManagerFactory;
        _contexts = new ConcurrentHashMap<String, TenantContext>();
    }

    public TenantContext getContext(TenantIdentifier tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant cannot be null");
        }
        return getContext(tenant.getId());
    }

    public TenantContext getContext(String tenantId) {
        if (StringUtils.isNullOrEmpty(tenantId)) {
            throw new IllegalArgumentException("Tenant cannot be null or empty string");
        }
        TenantContext context = _contexts.get(tenantId);
        if (context == null) {
            logger.info("Initializing tenant context: {}", tenantId);
            final TenantContext newContext = new TenantContextImpl(tenantId, _repository, _injectionManagerFactory);
            context = _contexts.putIfAbsent(tenantId, newContext);
            if (context == null) {
                context = newContext;
            }
        }
        return context;
    }
}
