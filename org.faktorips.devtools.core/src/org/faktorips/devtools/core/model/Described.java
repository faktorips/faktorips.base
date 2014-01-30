/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import java.util.Locale;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;

/**
 * An interface that marks an object as having a description.
 * 
 * @author Jan Ortmann
 * 
 * @deprecated In version 3.1 another interface called {@link IDescribedElement} was introduced.
 */
// Deprecated since 3.1
@Deprecated
public interface Described {

    /**
     * Indicates if the implementation of this interface supports changing of the description by
     * means of the setter method.
     * 
     * @deprecated In version 3.1 another interface called {@link IDescribedElement} was introduced.
     *             To check whether an element supports descriptions, use the <tt>instanceof</tt>
     *             operator.
     */
    // Deprecated since 3.1
    @Deprecated
    public boolean isDescriptionChangable();

    /**
     * Sets the description.
     * 
     * @throws IllegalArgumentException if newDescription is null.
     * 
     * @deprecated In version 3.1 another interface called {@link IDescribedElement} was introduced.
     *             This method now sets the text of the first {@link IDescription} that can be
     *             found. If there is no {@link IDescription} at all, then nothing is done at all.
     *             In addition, an {@link UnsupportedOperationException} is thrown, should
     *             {@link #isDescriptionChangable()} return <tt>false</tt> at the moment this
     *             operation is called.
     */
    // Deprecated since 3.1
    @Deprecated
    public abstract void setDescription(String newDescription);

    /**
     * Returns the element's description.
     * 
     * @deprecated In version 3.1 another interface called {@link IDescribedElement} was introduced.
     *             Use {@link IDescribedElement#getDescription(Locale)} or the methods provided by
     *             {@link IpsPlugin#getMultiLanguageSupport()} instead.
     */
    // Deprecated since 3.1
    @Deprecated
    public abstract String getDescription();

}
