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

import com.google.common.io.Files;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.file.FileRepository;
import org.datacleaner.repository.file.FileRepositoryFolder;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Class ComponentsStoreTest
 * 
 * @since 28.7.15
 */
public class ComponentStoreTest {
    private final String tenantId = "tenant";
    private final String componentName = "componentName";
    private final String componentId1 = "id1";
    private final String componentId2 = "id2";

    @Test
    public void testStore() throws Exception {
        File tempFolder = Files.createTempDir();
        tempFolder.deleteOnExit();

        FileRepositoryFolder repo = new FileRepositoryFolder(null, tempFolder);
        Repository repository = new FileRepository(repo.getFile());
        repository.createFolder(tenantId);
        ComponentStore store = new ComponentStoreImpl(repository, tenantId);

        ComponentStoreHolder conf1 = createHolder(componentId1);
        store.storeConfiguration(conf1);

        File tenantDir = new File(tempFolder, tenantId);
        File componentFolder = new File(tenantDir, ComponentStoreImpl.FOLDER_NAME);
        Assert.assertTrue(componentFolder.exists());
        File confFile = new File(componentFolder, componentId1);
        Assert.assertTrue(confFile.exists());

        ComponentStoreHolder conf2 = store.getConfiguration(componentId1);
        Assert.assertEquals(conf1.getComponentId(), conf2.getComponentId());
        Assert.assertEquals(conf1.getTimeout(), conf2.getTimeout());
        Assert.assertEquals(null, store.getConfiguration(componentId2));
        store.removeConfiguration(componentId1);
        Assert.assertEquals(null, store.getConfiguration(componentId1));
    }

    private ComponentStoreHolder createHolder(String componentId) {
        return new ComponentStoreHolder(10l, null, componentId, componentName);
    }
}
