/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

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
    public void put(IIpsSrcFile file, String key, String value);

    /**
     * Put for boolean.
     * 
     * @see #put(IIpsSrcFile, String, String)
     */
    public void put(IIpsSrcFile file, String key, boolean value);

    /**
     * Returns the value for the given ips source file and key. Returns <code>null</code> if no
     * value is found the file or key.
     */
    public String get(IIpsSrcFile file, String key);

    /**
     * Returns the boolean value for the given ips source file and key. Returns <code>false</code>
     * if no such key is defined for the given file.
     * 
     * @see #get(IIpsSrcFile, String)
     */
    public boolean getBoolean(IIpsSrcFile file, String key);

    /**
     * Removes all key/value pairs for the given file. Does nothing if the file is <code>null</code>
     * .
     */
    public void remove(IIpsSrcFile file);

    /**
     * Removes the key/value pair for the given file and key. Does nothing if file or key is
     * <code>null</code>.
     */
    public void remove(IIpsSrcFile file, String key);
}
