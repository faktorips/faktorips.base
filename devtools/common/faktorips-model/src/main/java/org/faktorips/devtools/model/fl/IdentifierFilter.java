/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.fl;

import java.util.List;

import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;

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
