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

import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Abstraction for the way an {@link IClassLoaderProvider} is created from the project, for example
 * from the IDE.
 */
// Yes, LoaderProviderFactory is a stupid name. Find a better one if it bothers you...
public interface IClassLoaderProviderFactory {

    /**
     * Returns an {@link IClassLoaderProvider} for the given project.
     */
    IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject);

    /**
     * Returns an {@link IClassLoaderProvider} for the given project.
     */
    IClassLoaderProvider getClassLoaderProvider(IIpsProject ipsProject, ClassLoader parent);
}
