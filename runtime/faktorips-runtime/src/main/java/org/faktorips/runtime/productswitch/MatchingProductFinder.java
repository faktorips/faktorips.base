/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.productswitch;

import java.util.List;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;

/**
 * This functional interface represents an operation to find an {@link IProductComponent} for a
 * given {@link IConfigurableModelObject}.
 * <p>
 * The functional method of this interface is
 * {@link #findMatchingProduct(IConfigurableModelObject, List, List)}.
 *
 * @since 23.6
 */
@FunctionalInterface
public interface MatchingProductFinder {

    /**
     * This method returns a {@link ProductFinderResult} object which contains the correct
     * replacement of an {@link IProductComponent} for the given {@link IConfigurableModelObject}.
     * Possible return values are:
     * <ul>
     * <li>the correct replacement of a {@link IProductComponent} for the given
     * {@link IConfigurableModelObject}</li>
     * <li>{@link ProductFinderResult#empty(String)} if no suitable replacement was found</li>
     * <li>{@link ProductFinderResult#error(String)} if no suitable replacement <b>can</b> be found,
     * for example because the new parent product component has no children for the given
     * association or there are multiple possible new targets. Details will be given in the
     * {@link ProductFinderResult#getMessage()}</li>
     * </ul>
     * If no replacement was found, the result contains a message explaining why.
     * <p>
     * If more than one finder is chained with the {@link #or(MatchingProductFinder)} method, the
     * error result will stop the chain, while the empty result will delegate finding a replacement
     * to the next finder.
     *
     * @param modelObject the model object that should be switched to a new
     *            {@link IProductComponent}
     * @param oldProducts a list of {@link IProductComponent} objects found in the product
     *            configuration of the model object
     * @param newProducts a list of {@link IProductComponent} objects found in the product
     *            configuration of the new {@link IProductComponent}
     * @return a suitable replacement for the given model object from the {@code newProducts} list
     *             or an explanation why none was found
     */
    ProductFinderResult findMatchingProduct(IConfigurableModelObject modelObject,
            List<IProductComponent> oldProducts,
            List<IProductComponent> newProducts);

    /**
     * Returns a composed product finder that represents a short-circuiting logical OR of this
     * product finder and another. When evaluating the composed product finder, if {@code this}
     * product finder is present or has an error, then the {@code other} product finder is not
     * evaluated.
     *
     * @param other a product finder that will be logically-ORed with {@code this} product finder
     * @return the result of {@code this} product finder, or if empty, the result of the
     *             {@code other} product finder
     */
    default MatchingProductFinder or(MatchingProductFinder other) {
        return (mo, oldProducts, newProducts) -> {
            ProductFinderResult result = findMatchingProduct(mo, oldProducts, newProducts);
            if (!result.isEmpty() || result.isError()) {
                return result;
            } else {
                return other.findMatchingProduct(mo, oldProducts, newProducts);
            }
        };
    }
}
