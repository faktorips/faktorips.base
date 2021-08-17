/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.IVersionProvider;

/**
 * This interface is implemented by every {@link IIpsElement} that supports the since-version
 * mechanism.
 * <p>
 * The since-version is a documentation property that tells the user since which version a element
 * is available in the model.
 * 
 */
public interface IVersionControlledElement extends IIpsObjectPartContainer {

    public static final String PROPERTY_SINCE_VERSION_STRING = "sinceVersionString"; //$NON-NLS-1$

    /**
     * Sets the Version since which this part is available in the model using a version string
     * representation.
     * 
     * @param version The version-string that should be set as since-version
     */
    public void setSinceVersionString(String version);

    /**
     * Returns the version since which this part is available as a string. The version was set by
     * {@link #setSinceVersionString(String)}.
     * 
     * @return the version since which this element is available
     * @see #getSinceVersion()
     */
    public String getSinceVersionString();

    /**
     * Returns the version since which this part is available. The version was set by
     * {@link #setSinceVersionString(String)}. Returns <code>null</code> if no since version is set.
     * 
     * @return the version since which this element is available
     * @throws IllegalArgumentException if the current since version is no valid version according
     *             to the configured {@link IVersionProvider}
     * @see #isValidSinceVersion()
     */
    public IVersion<?> getSinceVersion();

    /**
     * Returns <code>true</code> if the version set by {@link #setSinceVersionString(String)} is a
     * valid version according to the configured {@link IVersionProvider}. If there is no version (
     * {@link #getSinceVersionString()} returns null or empty string) this method also returns
     * <code>false</code>. That means no version is no valid version!
     * 
     * @return <code>true</code> if the version is correct and {@link #getSinceVersion()} would
     *         return a valid version. Otherwise <code>false</code>.
     */
    public boolean isValidSinceVersion();

}
