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

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;

/**
 * Represents the result of a product finder operation, which contains the correct replacement of an
 * {@link IProductComponent} for the given {@link IConfigurableModelObject}.
 *
 * @since 23.6
 */
public class ProductFinderResult {

    private final IProductComponent productComponent;
    private final boolean error;
    private final String message;

    private ProductFinderResult(IProductComponent productComponent) {
        this.productComponent = productComponent;
        error = false;
        message = null;
    }

    private ProductFinderResult(String message, boolean error) {
        productComponent = null;
        this.error = error;
        this.message = message;
    }

    /**
     * Constructs a new {@link ProductFinderResult} with the specified product component.
     *
     * @param productComponent the correct replacement
     * @return a {@link ProductFinderResult} with the correct replacement
     */
    public static ProductFinderResult of(IProductComponent productComponent) {
        return new ProductFinderResult(productComponent);
    }

    /**
     * A new {@link ProductFinderResult} indicating an error while switching, for example when more
     * than one product component was found.
     * <p>
     * If more than one finder is chained with the
     * {@link MatchingProductFinder#or(MatchingProductFinder)} or
     * {@link AdvancedProductFinder#or(AdvancedProductFinder)} method, the error result will stop
     * the evaluation.
     *
     * @param message an error message explaining why the switch failed
     * @return a {@link ProductFinderResult} indicating an error
     */
    public static ProductFinderResult error(String message) {
        return new ProductFinderResult(message, true);
    }

    /**
     * A new {@link ProductFinderResult} that no replacement was found.
     * <p>
     * If more than one finder is chained with the
     * {@link MatchingProductFinder#or(MatchingProductFinder)} or
     * {@link AdvancedProductFinder#or(AdvancedProductFinder)} method, the empty result will
     * continue to evaluate the chained product finders.
     *
     * @param message a message explaining why no replacement was found
     *
     * @return a {@link ProductFinderResult} indicating that no replacement was found
     */
    public static ProductFinderResult empty(String message) {
        return new ProductFinderResult(message, true);
    }

    /**
     * Returns the {@link IProductComponent} for the replacement.
     *
     * @return the product component
     */
    public IProductComponent getProductComponent() {
        return productComponent;
    }

    /**
     * Returns {@code true} if the result is an {@link #error(String)}.
     *
     * @return {@code true} if the result is an error
     */
    public boolean isError() {
        return error;
    }

    /**
     * Returns {@code true} if the result is an empty result.
     *
     * @return {@code true} if the result is empty
     */
    public boolean isEmpty() {
        return productComponent == null;
    }

    /**
     * If a {@link IProductComponent} is present, returns {@code true}, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}
     */
    public boolean isPresent() {
        return !error && !isEmpty();
    }

    /**
     * @return the message explaining why this result {@link #isError()} or {@link #isEmpty()}
     */
    public String getMessage() {
        return message;
    }
}
