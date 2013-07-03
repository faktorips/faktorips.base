/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.fl;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * Implementations of this interface are registered by the extension point
 * "flIdentifierFilterExtension". They are instantiated by the {@link IpsPlugin} and used in
 * {@link IdentifierFilter}. The {@link IdentifierFilter} asked for every
 * {@link IIpsObjectPartContainer part container} if it is allowed to use as identifier or not.
 * 
 * @see IdentifierFilter
 * @author frank
 * @since 3.10.0
 */
public interface IFlIdentifierFilterExtension {

    /**
     * Check if this {@link IIpsObjectPartContainer} is allowed. This method is called very often so
     * it is important that the decision is found very fast!
     * 
     * @param ipsObjectPartContainer the {@link IIpsObjectPartContainer} to check
     * @return <code>true</code> if use is allowed and <code>false</code>
     */
    public boolean isIdentifierAllowed(IIpsObjectPartContainer ipsObjectPartContainer);
}
