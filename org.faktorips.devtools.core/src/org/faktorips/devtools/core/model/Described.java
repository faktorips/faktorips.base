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

import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;

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
     *             This method doesn't work any longer.
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
     *             This method doesn't work any longer.
     */
    // Deprecated since 3.1
    @Deprecated
    public abstract void setDescription(String newDescription);

    /**
     * Returns the object's description. This method never returns null.
     * 
     * @deprecated In version 3.1 another interface called {@link IDescribedElement} was introduced.
     *             This method doesn't work any longer.
     */
    // Deprecated since 3.1
    @Deprecated
    public abstract String getDescription();

}
