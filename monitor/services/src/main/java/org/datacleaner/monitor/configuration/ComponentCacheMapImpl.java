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
package org.datacleaner.monitor.configuration;

import org.datacleaner.monitor.server.components.ComponentHandler;
import org.datacleaner.repository.RepositoryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for caching and storing Components and their configurations.
 * @since 24.7.15
 */
public class ComponentCacheMapImpl implements ComponentCache {
    private static final Logger logger = LoggerFactory.getLogger(ComponentCacheMapImpl.class);
    private static final long CHECK_INTERVAL = 5 * 60 * 1000;
    private static final long CLOSE_TIMEOUT = 60 * 1000;

    private TenantContextFactory _tenantContextFactory;

    private ConcurrentHashMap<String, ComponentCacheConfigWrapper> data = new ConcurrentHashMap<>();
    private Thread checkerThread;
    private TimeoutChecker checker;

    public ComponentCacheMapImpl(TenantContextFactory _tenantContextFactory) {
        this._tenantContextFactory = _tenantContextFactory;
        checker = new TimeoutChecker();
        checkerThread = new Thread(checker);
        checkerThread.start();
    }

    /**
     * Put configuration of component to the cache
     *
     * @param tenant
     * @param tenantContext
     * @param componentsHolder
     */
    public void put(String tenant, TenantContext tenantContext, ComponentStoreHolder componentsHolder) {
        logger.info("Put component. name: {}, componentId: {}.", componentsHolder.getComponentName(),
                componentsHolder.getComponentId());
        ComponentHandler handler = ComponentHandlerFactory.createComponent(
                tenantContext, componentsHolder.getComponentName(), componentsHolder.getCreateInput().configuration);
        ComponentCacheConfigWrapper wrapper = new ComponentCacheConfigWrapper(tenant, componentsHolder, handler);
        data.put(componentsHolder.getComponentId(), wrapper);
        tenantContext.getComponentStore().storeConfiguration(wrapper.getComponentStoreHolder());
    }

    /**
     * Read configuration from cache. If configurationHolder is not in cache, is loaded from repository,
     * but in this case in holder is only configuration
     *
     * @param id
     * @return
     */
    public ComponentCacheConfigWrapper get(String id, String tenant, TenantContext tenantContext) {
        logger.info("Get component with id: " + id);
        ComponentCacheConfigWrapper componentCacheConfigWrapper = data.get(id);
        if (componentCacheConfigWrapper == null) {
            logger.warn("Configuration {} does not exist in cache.", id);
            ComponentStore store = tenantContext.getComponentStore();
            ComponentStoreHolder storeConfig = store.getConfiguration(id);
            if (storeConfig == null) {
                logger.warn("Configuration {} does not exist in store.", id);
                return null;
            } else {
                ComponentHandler componentHandler = ComponentHandlerFactory.createComponent(tenantContext,
                        storeConfig.getComponentName(), storeConfig.getCreateInput().configuration);
                componentCacheConfigWrapper = new ComponentCacheConfigWrapper(tenant, storeConfig, componentHandler);
                data.put(id, componentCacheConfigWrapper);
            }
        }
        componentCacheConfigWrapper.updateTimeStamp();
        return componentCacheConfigWrapper;
    }

    /**
     * Remove configuration from memory and store. And this component is destroyed.
     *
     * @param id
     * @param tenantContext
     */
    public boolean remove(String id, TenantContext tenantContext) {
        boolean inCache = removeConfigurationOnlyFromCache(id);
        boolean inStore = removeConfigurationOnlyFromStore(id, tenantContext);
        logger.info("Component {} was removed from cache and closed.", id);
        return inCache || inStore;
    }


    private boolean removeConfigurationOnlyFromCache(String id) {
        ComponentCacheConfigWrapper config = data.get(id);
        if (config != null) {
            data.remove(id);
            config.getHandler().closeComponent();
            return true;
        } else {
            return false;
        }
    }

    private boolean removeConfigurationOnlyFromStore(String id, TenantContext tenantContext) {
        ComponentStore store = tenantContext.getComponentStore();
        return store.removeConfiguration(id);
    }

    /**
     * Close all component in memory. All components configuration are still in repository.
     *
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        logger.info("Closing Components cache.");

        synchronized (checker) {
            checker.stop();
            checker.notifyAll();
        }
        long maxTime = System.currentTimeMillis() + CLOSE_TIMEOUT;
        while (checkerThread.isAlive()) {
            Thread.sleep(500);
            if (maxTime < System.currentTimeMillis()) {
                logger.error("Problem with closing checking thread.");
                break;
            }
        }

        for (ComponentCacheConfigWrapper componentCacheConfigWrapper : data.values()) {
            componentCacheConfigWrapper.getHandler().closeComponent();
            logger.info("Component with id: {} was closed.", componentCacheConfigWrapper.getComponentStoreHolder().getComponentId());
            // Configuration is still in store.
        }

        data.clear();
        logger.info("Components cache was closed.");
    }


    /**
     * Thread for checking timeout for each components and also do update configuration is store.
     */
    private class TimeoutChecker implements Runnable {
        boolean running = true;
        boolean firstRun = true;

        @Override
        public void run() {
            while (running) {
                if (firstRun) {
                    firstRun = false;
                } else {
                    Iterator<RepositoryFolder> repositoryFolderIterator = _tenantContextFactory.getRepositoryFolderIterator();
                    Set<String> allIdInCache = new HashSet<>(data.keySet());

                    while (repositoryFolderIterator.hasNext()) {
                        String tenantName = repositoryFolderIterator.next().getName();
                        TenantContext tenantContext = _tenantContextFactory.getContext(tenantName);
                        List<ComponentStoreHolder> configurationList = tenantContext.getComponentStore().getAllConfiguration();
                        for (ComponentStoreHolder storeHolder : configurationList) {
                            String componentId = storeHolder.getComponentId();
                            allIdInCache.remove(componentId);
                            //is in cache?
                            ComponentCacheConfigWrapper cache = data.get(componentId);
                            if (cache == null) {
                                //is only in store
                                if (!storeHolder.isValid()) {
                                    //remove from store
                                    logger.info("CacheChecker - Remove old configuration {} from store of tenant {}.", componentId, tenantContext.getTenantId());
                                    removeConfigurationOnlyFromStore(componentId, tenantContext);
                                }
                            } else {
                                long maxTimestamp = Math.max(cache.getComponentStoreHolder().getUseTimestamp(), storeHolder.getUseTimestamp());
                                if (maxTimestamp + storeHolder.getTimeout() < System.currentTimeMillis()) {
                                    // too old
                                    logger.info("CacheChecker - Remove old configuration {} from store and cache of tenant {}.", componentId, tenantContext.getTenantId());
                                    removeConfigurationOnlyFromCache(componentId);
                                    removeConfigurationOnlyFromStore(componentId, tenantContext);
                                } else {
                                    if (cache.getComponentStoreHolder().getUseTimestamp() <= storeHolder.getUseTimestamp()) {
                                        //update cache
                                        logger.info("CacheChecker - Update timestamp of component {} in cache from store.", componentId);
                                        cache.setComponentStoreHolder(storeHolder);
                                    } else {
                                        //update store
                                        logger.info("CacheChecker - Update timestamp of component {} in store from cache.", componentId);
                                        tenantContext.getComponentStore().storeConfiguration(cache.getComponentStoreHolder());
                                    }
                                }
                            }
                        }
                    }

                    for (String componentId : allIdInCache) {
                        //These components are without configuration in store
                        removeConfigurationOnlyFromCache(componentId);
                        logger.info("CacheChecker - Configuration {} is not in store. It was removed from cache.", componentId);
                    }

                }
                synchronized (this) {
                    if (running) {
                        try {
                            wait(CHECK_INTERVAL);
                        } catch (InterruptedException e) {
                            running = false;
                            logger.error("Thread for checking component cache was been interrupted.", e);
                        }
                    }
                }
            }
            logger.info("CacheChecker close");
        }

        public void stop() {
            running = false;
        }
    }
}
