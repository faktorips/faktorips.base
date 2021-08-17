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
 * A factory used to create {@link IVersionProvider} instances. This factory is registered in the
 * extension point versionProvider.
 * 
 */
public interface IVersionProviderFactory {

    /**
     * Creates a new {@link IVersionProvider} for the specified project.
     * 
     * @param ipsProject The project for which a version provider should be instantiated
     * 
     * @return The new {@link IVersionProvider}
     */
    public IVersionProvider<?> createVersionProvider(IIpsProject ipsProject);

}
