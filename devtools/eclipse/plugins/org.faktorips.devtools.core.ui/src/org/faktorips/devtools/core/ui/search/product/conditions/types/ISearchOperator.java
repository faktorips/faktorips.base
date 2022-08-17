/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import org.faktorips.devtools.model.productcmpt.IProductPartsContainer;

/**
 * A ISearchOperator provides the core for the search
 * <p>
 * The method {@link #check(IProductPartsContainer)} returns whether the given
 * {@link IProductPartsContainer} is a hit of of the search.
 * 
 * @author dicker
 */
public interface ISearchOperator {

    /**
     * returns true, if the given {@link IProductPartsContainer} is a hit
     */
    boolean check(IProductPartsContainer productPartsContainer);

}
