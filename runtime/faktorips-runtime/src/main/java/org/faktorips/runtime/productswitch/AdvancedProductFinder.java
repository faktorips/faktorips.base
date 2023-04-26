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
import java.util.stream.Collectors;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.type.PolicyAssociation;

/**
 * This functional interface represents an operation to find an {@link IProductComponent} for a
 * given {@link IConfigurableModelObject}. Unlike the {@link MatchingProductFinder} interface, this
 * interface provides more options to find a replacement component.
 * <p>
 * The functional method of this interface is
 * {@link #findMatchingProduct(IConfigurableModelObject, IProductComponent, IConfigurableModelObject, PolicyAssociation)}.
 *
 * @since 23.6
 */
@FunctionalInterface
public interface AdvancedProductFinder {

    /**
     * This {@link AdvancedProductFinder} uses the {@link IProductComponent#getKindId()} to find a
     * suitable replacement for a product switch.
     */
    AdvancedProductFinder BY_KIND_ID = (parent, oldParentProdCmpt, child, parentToChild) -> {
        List<IProductComponent> matches = parentToChild
                .getMatchingAssociation()
                .getTargetObjects(parent.getProductComponent(), parent.getEffectiveFromAsCalendar()).stream()
                .filter(c -> child.getProductComponent().getKindId().equals(c.getKindId()))
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            return ProductSwitch.createEmptyResult(child);
        } else if (matches.size() == 1) {
            return ProductFinderResult.of(matches.get(0));
        } else {
            return ProductSwitch.createErrorResult(child, matches);
        }
    };

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
     * If more than one finder is chained with the {@link #or(AdvancedProductFinder)} method, the
     * error result will stop the chain, while the empty result will delegate finding a replacement
     * to the next finder.
     *
     * @param parent the parent model object
     * @param oldParentProdCmpt the old {@link IProductComponent}, because the {@code parent}
     *            parameter already is set to the new product component
     * @param child the child object for which a new {@link IProductComponent} must be found
     * @param parentToChild the {@link PolicyAssociation} between parent and child
     * @return a {@link ProductFinderResult} that represents a suitable replacement for the given
     *             model object or explains why none was found
     */
    ProductFinderResult findMatchingProduct(IConfigurableModelObject parent,
            IProductComponent oldParentProdCmpt,
            IConfigurableModelObject child,
            PolicyAssociation parentToChild);

    /**
     * Returns a composed product finder that represents a short-circuiting logical OR of this
     * product finder and other product finders. When evaluating the composed product finder, if
     * {@code this} product finder is present or has an error, then the {@code other} product finder
     * is not evaluated.
     *
     * @param other a product finder that will be logically-ORed with {@code this} product finder
     * @return the result of {@code this} product finder or if empty the result of the {@code other}
     *             product finder
     */
    default AdvancedProductFinder or(AdvancedProductFinder other) {
        return (parent, oldParentProdCmpt, child, parentToChild) -> {
            ProductFinderResult result = findMatchingProduct(parent, oldParentProdCmpt, child, parentToChild);
            if (!result.isEmpty() || result.isError()) {
                return result;
            } else {
                return other.findMatchingProduct(parent, oldParentProdCmpt, child, parentToChild);
            }
        };
    }
}
