/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * An interface to a storage mechanism for making ips object editor settings persistent until the
 * editor is closed.
 * 
 * The store manages a collection of key/value pairs per ips sorcce file. The keys and values must
 * be strings.
 * 
 * @author Jan Ortmann
 */
public interface IIpsObjectEditorSettings {

    /**
     * Stores the given key/value pair for the given ips source file. Does nothing if the file is
     * <code>null</code>. If the value is <code>null</code>, the method behaves as the remove(file,
     * key) method. Note that the keys must not contain a blank character / space.
     * 
     * @throws NullPointerException if key is <code>null</code>.
     * @throws IllegalArgumentException if the key contains a space.
     * @see #remove(IIpsSrcFile, String)
     */
    void put(IIpsSrcFile file, String key, String value);

    /**
     * Put for boolean.
     * 
     * @see #put(IIpsSrcFile, String, String)
     */
    void put(IIpsSrcFile file, String key, boolean value);

    /**
     * Returns the value for the given ips source file and key. Returns <code>null</code> if no
     * value is found the file or key.
     */
    String get(IIpsSrcFile file, String key);

    /**
     * Returns the boolean value for the given ips source file and key. Returns <code>false</code>
     * if no such key is defined for the given file.
     * 
     * @see #get(IIpsSrcFile, String)
     */
    boolean getBoolean(IIpsSrcFile file, String key);

    /**
     * Removes all key/value pairs for the given file. Does nothing if the file is <code>null</code>
     * .
     */
    void remove(IIpsSrcFile file);

    /**
     * Removes the key/value pair for the given file and key. Does nothing if file or key is
     * <code>null</code>.
     */
    void remove(IIpsSrcFile file, String key);
}
