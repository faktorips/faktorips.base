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

import org.faktorips.devtools.model.ipsproject.IVersionFormat;

/**
 * Provides {@link IVersion versions} for different uses. Its main use is to provide the version of
 * the project it belongs to. It can also be used to create a {@link IVersion} instance from a
 * specified String. To verify the format of a version string it extends the {@link IVersionFormat}
 * interface.
 * <p>
 * The generic type K specifies the type of version that is handled by this {@link IVersionProvider}
 * . This ensures that versions of different kinds cannot be mixed within one version provider.
 * 
 */
public interface IVersionProvider<K extends IVersion<K>> extends IVersionFormat {

    /**
     * Creates a new version by using the parameter string.
     * 
     * @param versionAsString The String that should be converted into a version instance.
     * @return the new Version that reflects the specified versionString
     * 
     * @throws IllegalArgumentException if the version string could not be parsed by this
     *             {@link IVersionProvider}. Use {@link #isCorrectVersionFormat(String)} to make
     *             sure to only get valid results.
     */
    public IVersion<K> getVersion(String versionAsString);

    /**
     * Returns the version of the project this {@link IVersionProvider} belongs to. If there is no
     * valid version this method returns an empty version.
     * 
     * @return IVersion of the according project
     */
    public IVersion<K> getProjectVersion();

    /**
     * Sets the given Version in the project this {@link IVersionProvider} belongs to.
     * 
     * @param version of the according model
     */
    public void setProjectVersion(IVersion<K> version);
}
