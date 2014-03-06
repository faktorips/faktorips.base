/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * 
 * The {@link IVersionProvider} provide its callers with an adequate version of an
 * {@link IpsObjectPartContainer}.
 */
public interface IVersionProvider {

    /**
     * Creates a new Version by using an inputString
     * 
     * @param versionAsString The String that a user specified for a version
     * @return the new Version
     */
    public IVersion getVersion(String versionAsString);

    /**
     * Returns the correct IVersion by searching through the given packageFragmentRoot
     * 
     * @param packageFragmentRoot containing the set of package fragment
     * @return IVersion of the according model
     */
    public IVersion getModelVersion(IIpsPackageFragmentRoot packageFragmentRoot);

    /**
     * Sets the correct Version for the according model in the packageFragmentRoot
     * 
     * @param packageFragmentRoot containing the set of package fragment
     * @param version of the according model
     */
    public void setModelVersion(IIpsPackageFragmentRoot packageFragmentRoot, IVersion version);
}
