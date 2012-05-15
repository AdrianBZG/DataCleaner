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

import java.util.List;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.ClasspathScanDescriptorProvider;
import org.eobjects.analyzer.descriptors.DescriptorProvider;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Bean factory for {@link AnalyzerBeansConfiguration} elements in the DC
 * monitor application, like the {@link TaskRunner} and
 * {@link DescriptorProvider}.
 */
public class ConfigurationFactory {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFactory.class);

    private List<String> _scannedPackages;
    private int _numThreads;

    public List<String> getScannedPackages() {
        return _scannedPackages;
    }

    public void setScannedPackages(List<String> scannedPackages) {
        _scannedPackages = scannedPackages;
    }

    public int getNumThreads() {
        return _numThreads;
    }

    public void setNumThreads(int numThreads) {
        _numThreads = numThreads;
    }

    public TaskRunner createTaskRunner() {
        logger.info("Creating shared task runner with {} threads", _numThreads);
        return new MultiThreadedTaskRunner(_numThreads);
    }

    @Autowired
    public DescriptorProvider createDescriptorProvider(TaskRunner taskRunner) {
        logger.info("Creating shared descriptor provider with packages: {}", _scannedPackages);
        ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider(taskRunner);
        for (String packageName : _scannedPackages) {
            descriptorProvider.scanPackage(packageName, true);
        }
        return descriptorProvider;
    }
}
