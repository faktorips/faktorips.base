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

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.type.PolicyAssociation;

/**
 * This class represents the results of a product switch operation. It provides methods to retrieve
 * the switch result for a specific {@link IConfigurableModelObject} as well as views of successful
 * and failed switch results.
 *
 * @since 23.6
 */
public class ProductSwitchResults {

    private final Map<IConfigurableModelObject, ProductSwitchResult> result;

    /**
     * Constructs a new instance of {@code ProductSwitchResults} with the specified map of
     * {@link ProductSwitchResult product switch results}.
     *
     * @param result the map of switch results
     */
    public ProductSwitchResults(Map<IConfigurableModelObject, ProductSwitchResult> result) {
        this.result = result;
    }

    /**
     * Returns an unmodifiable view of all switch results.
     *
     * @return the map of switch results
     */
    public Map<IConfigurableModelObject, ProductSwitchResult> asMap() {
        return Collections.unmodifiableMap(result);
    }

    /**
     * Returns the switch result for the given model object or {@code null} if the model object was
     * not switched, for example if it isn't part of the original object tree or no switch target
     * was found for a parent model object.
     *
     * @param modelObject the model object
     * @return the switch result for the model object
     */
    public ProductSwitchResult getResultFor(IConfigurableModelObject modelObject) {
        return result.get(modelObject);
    }

    /**
     * Returns an unmodifiable view of all successful switch results.
     *
     * @return the map of successful switch results
     */
    public Map<IConfigurableModelObject, SuccessfulProductSwitch> successfulAsMap() {
        return Collections.unmodifiableMap(result.entrySet().stream()
                .filter(e -> e.getValue() instanceof SuccessfulProductSwitch)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (SuccessfulProductSwitch)e.getValue())));
    }

    /**
     * Returns an unmodifiable view of all failed switch results.
     *
     * @return the map of failed switch results
     */
    public Map<IConfigurableModelObject, FailedProductSwitch> failureAsMap() {
        return Collections.unmodifiableMap(result.entrySet().stream()
                .filter(e -> e.getValue() instanceof FailedProductSwitch)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (FailedProductSwitch)e.getValue())));
    }

    /**
     * @return {@code true} if at least one failed switch result is contained in the result;
     *             otherwise {@code false}
     */
    public boolean containsFailures() {
        return result.values().stream()
                .anyMatch(FailedProductSwitch.class::isInstance);
    }

    /**
     * @return the number of all results
     */
    public int size() {
        return result.size();
    }

    /**
     * This interface represents the result of a product switch operation for a single model object.
     */
    public interface ProductSwitchResult {

        /**
         * @return {@code true} if the switch operation was successful; otherwise returns
         *             {@code false}.
         */
        boolean isSuccessful();
    }

    /**
     * Represents the result of a successful product switch operation.
     */
    public static class SuccessfulProductSwitch implements ProductSwitchResult {
        private final IProductComponent oldProduct;
        private final IProductComponent newProduct;

        /**
         * Constructs a new {@code SuccessfulProductSwitch} instance with the given old and new
         * product components.
         *
         * @param oldProduct the old product component
         * @param newProduct the new product component
         */
        public SuccessfulProductSwitch(IProductComponent oldProduct, IProductComponent newProduct) {
            this.oldProduct = oldProduct;
            this.newProduct = newProduct;
        }

        /**
         * @return the old product component
         */
        public IProductComponent getOldProduct() {
            return oldProduct;
        }

        /**
         * @return the new product component
         */
        public IProductComponent getNewProduct() {
            return newProduct;
        }

        @Override
        public boolean isSuccessful() {
            return true;
        }
    }

    /**
     * Represents the result of a failed product switch operation.
     */
    public static class FailedProductSwitch implements ProductSwitchResult {

        private final IConfigurableModelObject parent;
        private final PolicyAssociation association;
        private final String message;

        /**
         * Constructs a new {@code FailedProductSwitch} instance with the given parent object,
         * policy association and error message.
         *
         * @param parent the parent object
         * @param association the policy association
         * @param message the error message
         */
        public FailedProductSwitch(IConfigurableModelObject parent, PolicyAssociation association, String message) {
            this.parent = parent;
            this.association = association;
            this.message = message;
        }

        /**
         * @return the parent object
         */
        public IConfigurableModelObject getParent() {
            return parent;
        }

        /**
         * @return the policy association
         */
        public PolicyAssociation getAssociation() {
            return association;
        }

        /**
         * @return the error message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return {@code false}
         */
        @Override
        public boolean isSuccessful() {
            return false;
        }
    }
}
