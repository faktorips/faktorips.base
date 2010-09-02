/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import java.util.Locale;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

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
     *             This method now delegates to
     *             {@link IIpsObjectPartContainer#hasDescriptionSupport()}.
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
     *             This method now sets the text of the first {@link IDescription} retrieved via
     *             {@link IIpsObjectPartContainer#getDescriptions()}. If there is no
     *             {@link IDescription} at all, then nothing is done at all. In addition, an
     *             {@link UnsupportedOperationException} is thrown, should
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
     *             Use {@link IDescribedElement#getDescription(Locale)} or
     *             {@link IpsPlugin#getLocalizedDescription(IDescribedElement)} instead. This method
     *             now delegates to {@link IpsPlugin#getLocalizedDescription(IDescribedElement)}.
     */
    // Deprecated since 3.1
    @Deprecated
    public abstract String getDescription();

}
