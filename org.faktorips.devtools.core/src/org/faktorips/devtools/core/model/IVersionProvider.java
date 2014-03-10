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

import org.faktorips.devtools.core.model.ipsproject.IVersionFormat;

/**
 * The {@link IVersionProvider} is able to provides versions for different uses. Its main use is to
 * provide the version of the project its belongs to. It also is used to create a {@link IVersion}
 * instance for a specified String. To verify the format of a version string it extends the
 * {@link IVersionFormat} interface.
 * <p>
 * The generic type K specifies the type of version that is handled by this {@link IVersionProvider}
 * . This is important to verify that the versions are not mixed within one version provider.
 * 
 */
public interface IVersionProvider<K extends IVersion<K>> extends IVersionFormat {

    /**
     * Creates a new Version by using an parameter string.
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
     * Returns the version of the project this {@link IVersionProvider} belongs to.
     * 
     * @return IVersion of the according project
     */
    public IVersion<K> getProjectlVersion();

    /**
     * Sets the correct Version in the project this {@link IVersionProvider} belongs to.
     * 
     * @param version of the according model
     */
    public void setProjectVersion(IVersion<K> version);
}
