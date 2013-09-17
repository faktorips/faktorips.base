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

package org.faktorips.devtools.core.internal.fl;

import java.util.List;

import org.faktorips.devtools.core.fl.IFlIdentifierFilterExtension;
import org.faktorips.devtools.core.fl.IdentifierKind;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * An {@link IdentifierFilter} is used to check whether an {@link IIpsObjectPartContainer} is
 * allowed to be used as a identifier in an formula expression. The List holds the
 * {@link IFlIdentifierFilterExtension}s who can be extends by an extension point.
 * 
 * @author frank
 * @since 3.10.0
 */
public class IdentifierFilter {

    private final List<IFlIdentifierFilterExtension> flIdentifierFilters;

    public IdentifierFilter(List<IFlIdentifierFilterExtension> flIdentifierFilters) {
        this.flIdentifierFilters = flIdentifierFilters;
    }

    /**
     * Call the check of the {@link IIpsObjectPartContainer} in all the
     * {@link IFlIdentifierFilterExtension}s and returns <code>true</code> if it is allowed or
     * <code>false</code> when not.
     * 
     * @param ipsObjectPartContainer the {@link IIpsObjectPartContainer} to check
     * @param identifierKind the kind of Identifier {@link IdentifierKind}
     */
    public boolean isIdentifierAllowed(IIpsObjectPartContainer ipsObjectPartContainer, IdentifierKind identifierKind) {
        for (IFlIdentifierFilterExtension flIdentifierFilterExtension : flIdentifierFilters) {
            if (!flIdentifierFilterExtension.isIdentifierAllowed(ipsObjectPartContainer, identifierKind)) {
                return false;
            }
        }
        return true;
    }
}
