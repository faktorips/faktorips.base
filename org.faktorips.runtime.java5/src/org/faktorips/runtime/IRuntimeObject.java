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

package org.faktorips.runtime;

import java.util.Set;

/**
 * Base class for runtime objects offering access to extension properties.
 * 
 * @author Daniel Hohenberger
 */
public interface IRuntimeObject {

    /**
     * @return a set of the extension property ids defined for this element.
     */
    public abstract Set<String> getExtensionPropertyIds();

    /**
     * @param propertyId the id of the desired extension property.
     * 
     * @return the value of the extension property defined by the given <code>propertyId</code> or
     *         <code>null</code> if the extension property's <code>isNull</code> attribute is
     *         <code>true</code>.
     * @throws IllegalArgumentException if no such property exists.
     */
    public abstract Object getExtensionPropertyValue(String propertyId);

}
