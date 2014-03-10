/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsobject;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IVersion;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * This interface is implemented by every {@link IIpsElement} that supports the since-version
 * mechanism.
 * <p>
 * The since-version is a documentation property that tells the user since which version a element
 * is available in the model.
 * 
 */
public interface IVersionControlledElement extends IIpsObjectPartContainer {

    public static final String PROPERTY_SINCE_VERSION = "sinceVersion"; //$NON-NLS-1$

    /**
     * Sets the Version since which this part is available in the model.
     * 
     * @param version The version that should be set as since-version
     */
    public void setSinceVersion(IVersion<?> version);

    /**
     * Returns the version since which this part is available. The version was set by
     * {@link #setSinceVersion(IVersion)}.
     * 
     * @return the version since which this element is available
     */
    public IVersion<?> getSinceVersion();

}
