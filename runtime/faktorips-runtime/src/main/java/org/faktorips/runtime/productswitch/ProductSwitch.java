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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.type.AssociationKind;
import org.faktorips.runtime.model.type.PolicyAssociation;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ProductAssociation;
import org.faktorips.runtime.model.type.ProductCmptType;
import org.faktorips.runtime.productswitch.ProductSwitchResults.FailedProductSwitch;
import org.faktorips.runtime.productswitch.ProductSwitchResults.ProductSwitchResult;
import org.faktorips.runtime.productswitch.ProductSwitchResults.SuccessfulProductSwitch;

/**
 * The {@link ProductSwitch} class is used to generically and recursively switch the
 * {@link IProductComponent} of an {@link IConfigurableModelObject} and its children.
 * <p>
 * By default, it uses the only replacement that is configured in the product configuration. If more
 * than one replacement is found, it uses the {@link IProductComponent#getKindId()} to determine the
 * correct replacement.
 * <p>
 * The default behavior can be changed by providing a {@link BiPredicate} that provides a more
 * specific way than the kindId to find a replacement, or by providing a different implementation of
 * the {@link MatchingProductFinder} or the {@link AdvancedProductFinder} interface.
 *
 * @since 23.6
 */
public class ProductSwitch {

    /**
     * This {@link BiPredicate} uses the {@link IProductComponent#getKindId()} to find a suitable
     * replacement for a product switch. It can be used as a fallback for the
     * {@link ProductSwitch#from(IConfigurableModelObject)} method.
     * <p>
     * Example usage:
     *
     * <pre>
     * <code>
     *     // fallback after a BiPredicate for a special case
     *     ProductSwitch.from(modelObject).with(mySpecialBiPredicate.or(ProductSwitch.BY_KIND_ID)).to(otherProductComponent);
     *
     *     // using only
     *     ProductSwitch.from(modelObject).with(ProductSwitch.BY_KIND_ID).to(otherProductComponent);
     *
     *     // is the same as the default behavior of
     *     ProductSwitch.from(modelObject).to(otherProductComponent);
     * </code>
     * </pre>
     */
    public static final BiPredicate<IProductComponent, IProductComponent> BY_KIND_ID = (oldP, newP) -> oldP.getKindId()
            .equals(newP.getKindId());

    private ProductSwitch() {
        // builder
    }

    /**
     * Switches the specified model object and all its configurable children to corresponding
     * objects from the new product component.
     * <p>
     * Example usage:
     *
     * <pre>
     * <code>
     *      ProductSwitch.from(modelObject).to(newProductComponent);
     *      // or
     *      ProductSwitch.from(modelObject)
     *          .matchingBy(attributeName)
     *          .to(newProductComponent);
     * </code>
     * </pre>
     *
     * @param modelObject the model object that should be switched to a different product
     *
     * @return the {@link ProductSwitchCondition ProductSwitchConditionBuilder}
     */
    public static ProductSwitchCondition from(IConfigurableModelObject modelObject) {
        return new ProductSwitchCondition(modelObject);
    }

    /**
     * Creates a {@link ProductFinderResult} object with an error message indicating that the switch
     * failed because the specified object has multiple replacements.
     *
     * @param modelObject the object where the switch failed
     * @param matchingProductComponents the results from the product finder
     *
     * @return a {@link ProductFinderResult#error(String)} object with a message
     */
    public static ProductFinderResult createErrorResult(IConfigurableModelObject modelObject,
            List<IProductComponent> matchingProductComponents) {
        String message = MessageFormat.format("Target {0} has multiple replacements {1}", modelObject.toString(),
                matchingProductComponents.stream().map(IProductComponent::toString)
                        .collect(Collectors.joining(" ", ", ", ".")));
        return ProductFinderResult.error(message);
    }

    /**
     * Creates a {@link ProductFinderResult} object with an error message indicating that the switch
     * failed because the specified object has no suitable replacements.
     *
     * @param modelObject the object where the switch failed
     *
     * @return a {@link ProductFinderResult#error(String)} object with a message
     */
    public static ProductFinderResult createEmptyResult(IConfigurableModelObject modelObject) {
        String message = MessageFormat.format("Target {0} has no suitable replacements.", modelObject.toString());
        return ProductFinderResult.empty(message);
    }

    private static boolean isProductConfiguredComposition(PolicyAssociation policyAssociation) {
        return policyAssociation.getAssociationKind() == AssociationKind.Composition
                && policyAssociation.getType().isConfiguredByProductCmptType();
    }

    /**
     * This builder class configures the conditions used for finding the correct replacement
     * {@link IProductComponent}.
     *
     */
    public static class ProductSwitchCondition {

        private final IConfigurableModelObject rootModelObject;

        private ProductSwitchCondition(IConfigurableModelObject modelObject) {
            rootModelObject = modelObject;
        }

        /**
         * This method will return a {@link MatchingProductFinderSwitch} that uses the given
         * predicate if more than one suitable replacement is found in the product configuration.
         *
         * @param howToMatch the predicate how the {@link IProductComponent IProductComponents}
         *            determine if they are suitable for a switch e.g. a configured enum that is
         *            used on all switchable {@link IProductComponent IProductComponents}
         * @return the {@link MatchingProductFinderSwitch MatchingProductFinderSwitchBuilder}
         */
        public MatchingProductFinderSwitch with(BiPredicate<IProductComponent, IProductComponent> howToMatch) {
            return new MatchingProductFinderSwitch(rootModelObject, howToMatch);
        }

        /**
         * This method will return a {@link MatchingProductFinderSwitch} that uses the given
         * attribute name of a {@link ProductComponent} if more than one suitable replacement is
         * found in the product configuration.
         *
         * @param attributeName the attribute with the given name declared in a
         *            {@link ProductCmptType}
         * @return the {@link MatchingProductFinderSwitch MatchingProductFinderSwitchBuilder}
         */
        public MatchingProductFinderSwitch matchingBy(String attributeName) {
            BiPredicate<IProductComponent, IProductComponent> howToMatch = (oldP, newP) -> {
                Object oldValue = IpsModel.getProductCmptType(oldP).getAttribute(attributeName).getValue(oldP,
                        rootModelObject.getEffectiveFromAsCalendar());
                Object newValue = IpsModel.getProductCmptType(newP).getAttribute(attributeName).getValue(newP,
                        rootModelObject.getEffectiveFromAsCalendar());
                return oldValue != null && oldValue.equals(newValue);
            };
            return new MatchingProductFinderSwitch(rootModelObject, howToMatch);
        }

        /**
         * This method will return a {@link MatchingProductFinderSwitch} that uses the given
         * {@link MatchingProductFinder}.
         *
         * @param productFinder the {@link MatchingProductFinder} that returns an {@link Optional}
         *            with an {@link IProductComponent} that should be used for the switch, or
         *            {@link Optional#empty()} if no suitable replacement is found in the product
         *            configuration
         * @return the {@link MatchingProductFinderSwitch MatchingProductFinderSwitchBuilder}
         */
        public MatchingProductFinderSwitch with(MatchingProductFinder productFinder) {
            return new MatchingProductFinderSwitch(rootModelObject, productFinder);
        }

        /**
         * This method will return a {@link MatchingProductFinderSwitch} that uses a
         * {@link ProductComponent} and an attribute name of the given {@link ProductCmptType} if
         * more than one suitable replacement is found in the product configuration.
         *
         * @param productCmptType the {@link ProductCmptType} used to switch
         * @param attributeName the attribute with the given name declared in the
         *            {@link ProductCmptType}
         * @return the {@link MatchingProductFinderSwitch MatchingProductFinderSwitchBuilder}
         */
        public MatchingProductFinderSwitch matchingBy(ProductCmptType productCmptType, String attributeName) {

            MatchingProductFinder productFinder = (childModel, oldChildProducts, newChildProducts) -> {
                if (oldChildProducts.size() == 1 && newChildProducts.size() == 1) {
                    return ProductFinderResult.of(newChildProducts.get(0));
                }
                ProductCmptType oldProductCmptType = IpsModel.getProductCmptType(childModel.getProductComponent());
                if (!oldProductCmptType.isSameOrSub(productCmptType)) {
                    return ProductFinderResult
                            .empty(MessageFormat.format("ProductCmptType {0} of Target {1} does not match {2}",
                                    oldProductCmptType, childModel, productCmptType));
                }

                Object oldValue = oldProductCmptType.getAttribute(attributeName).getValue(
                        childModel.getProductComponent(),
                        childModel.getEffectiveFromAsCalendar());

                List<IProductComponent> matching = new ArrayList<>();
                for (IProductComponent newChildProduct : newChildProducts) {
                    ProductCmptType newProductCmptType = IpsModel.getProductCmptType(newChildProduct);

                    if (newProductCmptType.isSameOrSub(productCmptType)) {
                        Object newValue = newProductCmptType.getAttribute(attributeName).getValue(newChildProduct,
                                childModel.getEffectiveFromAsCalendar());
                        if (oldValue != null && oldValue.equals(newValue)) {
                            matching.add(newChildProduct);
                        }
                    }
                }
                if (matching.size() == 1) {
                    return ProductFinderResult.of(matching.get(0));
                }
                if (matching.isEmpty()) {
                    return createEmptyResult(childModel);
                }
                return createErrorResult(childModel, matching);
            };
            return new MatchingProductFinderSwitch(rootModelObject, productFinder);
        }

        /**
         * The new {@link IProductComponent} that should be used for the switch.
         *
         * @param newProduct the new {@link IProductComponent}
         * @return an {@link Map} with all switched {@link IConfigurableModelObject
         *             IConfigurableModelObjects} and their status determined by either
         *             {@link FailedProductSwitch} or {@link SuccessfulProductSwitch}.
         */
        public ProductSwitchResults to(IProductComponent newProduct) {
            return new MatchingProductFinderSwitch(rootModelObject, BY_KIND_ID).to(newProduct);
        }

        /**
         * This method allows to register an {@link AdvancedProductFinder} with a specific
         * {@link PolicyAssociation}.
         *
         * @param association the association for which the product finder should be used
         * @param finder the product finder that should be used
         * @return the {@link MultipleProductSwitchCondition MultipleProductSwitchConditionBuilder}
         */
        public MultipleProductSwitchCondition switchAt(PolicyAssociation association, AdvancedProductFinder finder) {
            return new MultipleProductSwitchCondition(rootModelObject, association, finder);
        }

        /**
         * This method uses the given {@link AdvancedProductFinder} for all {@link PolicyAssociation
         * PolicyAssociations}.
         *
         * @param finder the {@link AdvancedProductFinder} to use
         * @return the {@link AdvancedProductFinderSwitch AdvancedProductFinderSwitchBuilder}
         */
        public AdvancedProductFinderSwitch with(AdvancedProductFinder finder) {
            return new AdvancedProductFinderSwitch(rootModelObject, finder);
        }
    }

    /**
     * This builder class configures the conditions used for finding the correct replacement
     * {@link IProductComponent} for multiple {@link PolicyAssociation PolicyAssociations}.
     *
     */
    public static class MultipleProductSwitchCondition {

        private final IConfigurableModelObject rootModelObject;
        private final Map<PolicyAssociation, AdvancedProductFinder> finders = new HashMap<>();

        private MultipleProductSwitchCondition(IConfigurableModelObject modelObject,
                PolicyAssociation association,
                AdvancedProductFinder finder) {
            rootModelObject = modelObject;
            finders.put(association, finder);
        }

        /**
         * This method allows to register an {@link AdvancedProductFinder} for a specific
         * {@link PolicyAssociation}.
         *
         * @param association the association for which the product finder that should be used
         * @param finder the product finder that should be used
         * @return the {@link MultipleProductSwitchCondition MultipleProductSwitchConditionBuilder}
         */
        public MultipleProductSwitchCondition switchAt(PolicyAssociation association, AdvancedProductFinder finder) {
            finders.put(association, finder);
            return this;
        }

        /**
         * This method registers the specified {@link AdvancedProductFinder} for all unregistered
         * {@link PolicyAssociation PolicyAssociations}.
         *
         * @param finder the {@link AdvancedProductFinder} to use for unregistered
         *            {@link PolicyAssociation PolicyAssociations}.
         * @return the {@link AdvancedProductFinderSwitch AdvancedProductFinderSwitchBuilder}.
         */
        public AdvancedProductFinderSwitch switchOthers(AdvancedProductFinder finder) {
            finders.put(null, finder);
            return new AdvancedProductFinderSwitch(rootModelObject, finders);
        }

        /**
         * This method registers {@link AdvancedProductFinder#BY_KIND_ID BY_KIND_ID} for all
         * unregistered {@link PolicyAssociation PolicyAssociations}.
         *
         * @return the {@link AdvancedProductFinderSwitch AdvancedProductFinderSwitchBuilder}
         */
        public AdvancedProductFinderSwitch elseUseDefault() {
            finders.put(null, AdvancedProductFinder.BY_KIND_ID);
            return new AdvancedProductFinderSwitch(rootModelObject, finders);
        }
    }

    /**
     * This builder class uses the {@link AdvancedProductFinder} to switch to a new
     * {@link IProductComponent}
     *
     */
    public static class AdvancedProductFinderSwitch extends AbstractProductFinderSwitch {

        private final Map<PolicyAssociation, AdvancedProductFinder> finders = new HashMap<>();

        private AdvancedProductFinderSwitch(IConfigurableModelObject modelObject,
                Map<PolicyAssociation, AdvancedProductFinder> finders) {
            super(modelObject);
            this.finders.putAll(finders);
        }

        private AdvancedProductFinderSwitch(IConfigurableModelObject modelObject, AdvancedProductFinder finder) {
            super(modelObject);
            finders.put(null, finder);
        }

        @Override
        ProductFinderResult findMatchingProduct(IConfigurableModelObject parent,
                IConfigurableModelObject child,
                List<IProductComponent> oldChildProducts,
                List<IProductComponent> newChildProducts,
                IProductComponent oldParentProduct,
                PolicyAssociation policyAssociation) {
            AdvancedProductFinder productFinder = finders.get(policyAssociation);
            if (productFinder == null) {
                productFinder = finders.get(null);
            }
            return productFinder.findMatchingProduct(parent, oldParentProduct, child, policyAssociation);
        }
    }

    /**
     * This builder class uses the {@link MatchingProductFinder} to switch to a new
     * {@link IProductComponent}
     *
     */
    public static class MatchingProductFinderSwitch extends AbstractProductFinderSwitch {

        private final MatchingProductFinder productFinder;

        private MatchingProductFinderSwitch(IConfigurableModelObject modelObject,
                BiPredicate<IProductComponent, IProductComponent> howToMatch) {
            this(modelObject, (childModel, oldChildProducts, newChildProducts) -> {
                if (oldChildProducts.size() == 1 && newChildProducts.size() == 1) {
                    return ProductFinderResult.of(newChildProducts.get(0));
                }

                List<IProductComponent> matching = newChildProducts.stream()
                        .filter(newChildProduct -> howToMatch.test(childModel.getProductComponent(), newChildProduct))
                        .collect(Collectors.toList());

                if (matching.isEmpty()) {
                    return ProductSwitch.createEmptyResult(childModel);
                } else if (matching.size() == 1) {
                    return ProductFinderResult.of(matching.get(0));
                }
                return ProductSwitch.createErrorResult(childModel, matching);
            });
        }

        private MatchingProductFinderSwitch(IConfigurableModelObject modelObject, MatchingProductFinder productFinder) {
            super(modelObject);
            this.productFinder = productFinder;
        }

        @Override
        ProductFinderResult findMatchingProduct(IConfigurableModelObject parent,
                IConfigurableModelObject child,
                List<IProductComponent> oldChildProducts,
                List<IProductComponent> newChildProducts,
                IProductComponent oldParentProduct,
                PolicyAssociation policyAssociation) {
            return productFinder.findMatchingProduct(child, oldChildProducts, newChildProducts);
        }
    }

    public abstract static class AbstractProductFinderSwitch {

        private final IConfigurableModelObject rootModelObject;

        private AbstractProductFinderSwitch(IConfigurableModelObject modelObject) {
            rootModelObject = modelObject;
        }

        /**
         * The new {@link IProductComponent} that should be used for the switch.
         *
         * @param newProduct the new {@link IProductComponent}
         * @return an {@link Map} with all switched {@link IConfigurableModelObject
         *             IConfigurableModelObjects} and their status determined by either
         *             {@link FailedProductSwitch} or {@link SuccessfulProductSwitch}.
         */
        public ProductSwitchResults to(IProductComponent newProduct) {
            Map<IConfigurableModelObject, ProductSwitchResult> result = new LinkedHashMap<>();
            switchProduct(rootModelObject, newProduct, result);
            return new ProductSwitchResults(result);
        }

        private void switchProduct(IConfigurableModelObject modelObject,
                IProductComponent newProduct,
                Map<IConfigurableModelObject, ProductSwitchResult> result) {
            IProductComponent oldProduct = modelObject.getProductComponent();
            modelObject.setProductComponent(newProduct);
            result.put(modelObject, new SuccessfulProductSwitch(oldProduct, newProduct));
            PolicyCmptType policyCmptType = IpsModel.getPolicyCmptType(modelObject);
            Calendar effectiveDate = modelObject.getEffectiveFromAsCalendar();
            policyCmptType.getAssociations().forEach(policyAssociation -> {
                ProductAssociation productAssociation = policyAssociation.getMatchingAssociation();
                if (isProductConfiguredComposition(policyAssociation) && !policyAssociation.isDerivedUnion()
                        && productAssociation != null) {
                    List<IProductComponent> oldChildProducts = productAssociation.getTargetObjects(oldProduct,
                            effectiveDate);
                    List<IProductComponent> newChildProducts = productAssociation.getTargetObjects(newProduct,
                            effectiveDate);
                    policyAssociation.getTargetObjects(modelObject).stream()
                            .filter(IConfigurableModelObject.class::isInstance)
                            .map(IConfigurableModelObject.class::cast)
                            .forEach(child -> {
                                ProductFinderResult match = findMatchingProduct(modelObject, child, oldChildProducts,
                                        newChildProducts, oldProduct, policyAssociation);
                                if (match.isError() || match.isEmpty()) {
                                    result.put(child, new FailedProductSwitch(modelObject, policyAssociation,
                                            match.getMessage()));
                                } else {
                                    switchProduct(child, match.getProductComponent(), result);
                                }
                            });
                }
            });
        }

        abstract ProductFinderResult findMatchingProduct(IConfigurableModelObject parent,
                IConfigurableModelObject child,
                List<IProductComponent> oldChildProducts,
                List<IProductComponent> newChildProducts,
                IProductComponent oldParentProduct,
                PolicyAssociation policyAssociation);
    }
}
