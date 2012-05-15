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
package org.eobjects.datacleaner.repository;

import java.io.OutputStream;
import java.util.List;

import org.eobjects.metamodel.util.Action;

/**
 * Represents a folder in the {@link Repository}.
 */
public interface RepositoryFolder extends RepositoryNode {

    /**
     * Get (sub)folders of this folder.
     * 
     * @return (sub)folders of this folder.
     */
    public List<RepositoryFolder> getFolders();

    /**
     * Gets a (sub)folder of this folder, by name.
     * 
     * @param name
     *            the name of the (sub)folder.
     * @return a (sub)folder of this folder, by name.
     */
    public RepositoryFolder getFolder(String name);

    /**
     * Gets files in this folder.
     * 
     * @return files in this folder.
     */
    public List<RepositoryFile> getFiles();

    /**
     * Gets a file in this folder, by name.
     * 
     * @param name
     *            the name of the file.
     * @return a file in this folder, by name.
     */
    public RepositoryFile getFile(String name);

    /**
     * Creates a new file in particular folder.
     * 
     * @param name
     *            the name of the file.
     * @param writeCallback
     *            a callback which should define what to write to the file.
     * @return the {@link RepositoryFile} reference to the newly created file.
     */
    public RepositoryFile createFile(String name, Action<OutputStream> writeCallback);
}
