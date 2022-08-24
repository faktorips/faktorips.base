/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import org.faktorips.devtools.model.ipsproject.IClasspathContentsChangeListener;

/**
 * Provides a classloader for the classpath defined in a given Java project.
 */
public interface IClassLoaderProvider {

    /**
     * Returns the classloader for the Java project this is a provider for.
     */
    ClassLoader getClassLoader();

    /**
     * Adds the listener as one to be informed about changes to the classpath contents. In this case
     * the listener should get a new classloader if he wants to use classes that are up-to-date .
     */
    void addClasspathChangeListener(IClasspathContentsChangeListener listener);

    /**
     * Removes the listener from the list.
     */
    void removeClasspathChangeListener(IClasspathContentsChangeListener listener);

}
