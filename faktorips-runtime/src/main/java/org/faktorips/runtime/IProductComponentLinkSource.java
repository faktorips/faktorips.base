/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.List;

/**
 * The source of an {@link IProductComponentLink}. This may be a {@link IProductComponent} or a
 * {@link IProductComponentGeneration}.
 * 
 */
public interface IProductComponentLinkSource {

    /**
     * Getting the runtime repository of this link source.
     * 
     * @return The {@link IRuntimeRepository} that was used to create this
     *         {@link IProductComponentLinkSource}
     */
    public IRuntimeRepository getRepository();

    /**
     * Returns a <code>List</code> of all the <code>IProductComponentLink</code>s from this product
     * component generation to other product components.
     * 
     * @since 3.8
     */
    public List<IProductComponentLink<? extends IProductComponent>> getLinks();

    /**
     * Returns the <code>IProductComponentLink</code> for the association with the given role name
     * to the given product component or <code>null</code> if no such association exists.
     */
    public IProductComponentLink<? extends IProductComponent> getLink(String linkName, IProductComponent target);

}
