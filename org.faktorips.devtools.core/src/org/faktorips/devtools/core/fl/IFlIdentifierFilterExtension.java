/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.fl;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.fl.IdentifierFilter;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;

/**
 * Implementations of this interface are registered by the extension point
 * "flIdentifierFilterExtension". They are instantiated by the {@link IpsPlugin} and used in
 * {@link IdentifierFilter}. The {@link IdentifierFilter} determines for a given
 * {@link IIpsObjectPartContainer part container} if it may be used as an identifier in functions.
 * 
 * @see IdentifierFilter
 * @author frank
 * @since 3.10.0
 */
public interface IFlIdentifierFilterExtension {

    /**
     * Checks whether the given {@link IIpsObjectPartContainer} is allowed by this filter (
     * <code>true</code>) or disallowed (<code>false</code>) respectively. This method is called
     * frequently so performance is crucial.
     * 
     * @param ipsObjectPartContainer the {@link IIpsObjectPartContainer} to check
     * @param identifierKind the kind of Identifier ({@link IdentifierKind})
     * @return <code>true</code> if the part is allowed, <code>false</code> otherwise
     */
    public boolean isIdentifierAllowed(IIpsObjectPartContainer ipsObjectPartContainer, IdentifierKind identifierKind);

}
