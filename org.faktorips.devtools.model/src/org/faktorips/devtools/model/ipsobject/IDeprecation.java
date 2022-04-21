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

import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.IVersionProvider;
import org.faktorips.devtools.model.XmlSupport;

/**
 * The implementation of this interface is used to provide deprecation information for an
 * {@link IVersionControlledElement}. It describes the version since the part is deprecated, whether
 * the part is marked for removal and supports attaching {@link IDescription IDescriptions} in
 * different languages.
 */
public interface IDeprecation extends IDescribedElement, XmlSupport {

    public static final String XML_TAG = "Deprecation"; //$NON-NLS-1$

    public static final String XML_ATTRIBUTE_DEPRECATION_VERSION = "since"; //$NON-NLS-1$

    public static final String XML_ATTRIBUTE_FOR_REMOVAL = "forRemoval"; //$NON-NLS-1$

    public static final String PROPERTY_SINCE_VERSION_STRING = IVersionControlledElement.PROPERTY_SINCE_VERSION_STRING;

    public static final String PROPERTY_FOR_REMOVAL = "forRemoval"; //$NON-NLS-1$

    /**
     * Returns whether the part is marked for removal.
     */
    public boolean isForRemoval();

    /**
     * Marks the part for removal.
     */
    public void setForRemoval(boolean forRemoval);

    /**
     * Sets the version since which this part is deprecated using a version string representation.
     * 
     * @param version The version-string that should be set as since-version
     */
    public void setSinceVersionString(String version);

    /**
     * Returns the version since which this part is deprecated as a string. The version was set by
     * {@link #setSinceVersionString(String)}.
     * 
     * @return the version since which this element is deprecated
     * @see #getSinceVersion()
     */
    public String getSinceVersionString();

    /**
     * Returns the version since which this part is deprecated. The version was set by
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
