/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.faktorips.runtime.util.ValidationMessageUtil.generateValidationMessage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.faktorips.runtime.CardinalityRange;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.IProductComponentLink;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.faktorips.runtime.model.annotation.IpsAssociation;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationLinks;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;
import org.faktorips.valueset.Range;

public class ProductAssociation extends Association {

    public static final String MSGCODE_MAX_CARDINALITY_NOT_VALID = "ASSOCIATION-MAX_CARDINALITY_NOT_VALID";

    public static final String MSGKEY_MAX_CARDINALITY_NOT_VALID = "Validation.MaxCardinalityNotValid";

    public static final String MSGCODE_MIN_CARDINALITY_NOT_VALID = "ASSOCIATION-MIN_CARDINALITY_NOT_VALID";

    public static final String MSGKEY_MIN_CARDINALITY_NOT_VALID = "Validation.MinCardinalityNotValid";

    public static final String MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX = "ASSOCIATION-MAX_CARDINALITY_EXCEEDS_MODEL_MAX";

    public static final String MSGKEY_MAX_CARDINALITY_EXCEEDS_MODEL_MAX = "Validation.MaxCardinalityExceedsModelMax";

    public static final String MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN = "ASSOCIATION-MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN";

    public static final String MSGKEY_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN = "Validation.MinCardinalityFallsBelowModelMin";

    public static final String MSGCODE_DATE_FROM_NOT_VALID = "ASSOCIATION-VALID_FROM_NOT_VALID";

    public static final String MSGKEY_DATE_FROM_NOT_VALID = "Validation.DateFromNotValid";

    public static final String MSGCODE_DATE_TO_NOT_VALID = "ASSOCIATION-VALID_TO_NOT_VALID";

    public static final String MSGKEY_DATE_TO_NOT_VALID = "Validation.DateToNotValid";

    private final boolean changingOverTime;
    private final Method getLinksMethod;
    private final Method addMethodWithCardinality;
    private final Method addMethod;
    private final Method removeMethod;

    public ProductAssociation(Type type, Method getterMethod, Method addMethod, Method addMethodWithCardinality,
            Method removeMethod,
            boolean changingOverTime,
            Method getLinksMethod) {
        super(type, getterMethod);
        this.addMethod = addMethod;
        this.addMethodWithCardinality = addMethodWithCardinality;
        this.removeMethod = removeMethod;
        this.changingOverTime = changingOverTime;
        this.getLinksMethod = getLinksMethod;
    }

    @Override
    public ProductAssociation createOverwritingAssociationFor(Type subType) {
        return new ProductAssociation(subType, getGetterMethod(), addMethod, addMethodWithCardinality, removeMethod,
                changingOverTime,
                getLinksMethod);
    }

    /**
     * Returns the {@link ProductCmptType} this association belongs to.
     */
    @Override
    public ProductCmptType getType() {
        return (ProductCmptType)super.getType();
    }

    /**
     * Returns the model type this association belongs to.
     *
     * @deprecated Use {@link #getType()}
     */
    @Deprecated
    @Override
    public ProductCmptType getModelType() {
        return getType();
    }

    /**
     * Returns the target type of this association.
     *
     */
    @Override
    public ProductCmptType getTarget() {
        return (ProductCmptType)super.getTarget();
    }

    /**
     * Returns a list of the target(s) of the given product component's association identified by
     * this model type association. If this association is changing over time (resides in the
     * generation) the date is used to retrieve the correct generation. If the date is
     * <code>null</code> the latest generation is used. If the association is not changing over time
     * the date will be ignored.
     *
     * @param productComponentSource a product object corresponding to the {@link ProductCmptType}
     *            this association belongs to
     * @param effectiveDate The date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * @return a list of the target(s) of the given model object's association identified by this
     *             model type association
     * @throws IllegalArgumentException if the model object does not have an association fitting
     *             this model type association or that association is not accessible for any reason
     */
    public List<IProductComponent> getTargetObjects(IProductComponent productComponentSource, Calendar effectiveDate) {
        List<IProductComponent> targets = new ArrayList<>();
        Object source = getRelevantProductObject(productComponentSource, effectiveDate, isChangingOverTime());
        Object returnValue = invokeMethod(getGetterMethod(), source);
        if (returnValue instanceof Iterable<?> it) {
            for (Object target : it) {
                targets.add((IProductComponent)target);
            }
        } else if (returnValue instanceof IProductComponent productCmpt) {
            targets.add(productCmpt);
        }
        return targets;
    }

    /**
     * Adds the target objects to this association in the source object. If this is a ..1 ("to one")
     * association, the target object is set (and thus the potentially existing object is
     * overwritten).
     * <p>
     * The return value is the updated source object. It could be used to directly create a tree of
     * objects. For example with a model like:
     *
     * <pre>
     * {@code
     * SalesProduct <>---- Product <>---- CoverageType
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      products.addTargetObjects(salesProduct,
     *          coveragetypes.addTargetObjects(product,
     *              coverageType1, coverageType2));
     * </code>
     * </pre>
     *
     * @param source the object to add a target object to
     * @param effectiveDate The date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * @param targets the objects to add to source
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationAdder @IpsAssociationAdder}. This is the case if the
     *             association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple target objects provided for a ..1 ("to
     *             one") association
     *
     * @since 20.6
     */
    public <S extends IProductComponent> S addTargetObjects(S source,
            Calendar effectiveDate,
            Collection<IProductComponent> targets) {
        if (isToOneAssociation() && targets.size() > 1) {
            throw new IllegalArgumentException(String.format(
                    "The association %s on source object %s allows a maxmimum of one target object but %s were provided.",
                    getName(), source, targets.size()));
        } else {
            if (addMethod == null) {
                if (isOverriding()) {
                    return getSuperAssociation().addTargetObjects(source, effectiveDate, targets);
                } else {
                    throw new IllegalArgumentException(
                            String.format("The association %s on source object %s does not allow %s target objects%s.",
                                    getName(), source, isToOneAssociation() ? "setting" : "adding",
                                    isDerivedUnion() ? " because it is a derived union"
                                            : ("; make sure a method annotated with @"
                                                    + IpsAssociationAdder.class.getSimpleName() + " exists")));
                }
            }
        }

        Object relevantSource = getRelevantProductObject(source, effectiveDate, isChangingOverTime());
        for (IProductComponent target : targets) {
            invokeMethod(addMethod, relevantSource, target);
        }
        return source;
    }

    /**
     * Adds the target objects to this association in the source object. If this is a ..1 ("to one")
     * association, the target object is set (and thus the potentially existing object is
     * overwritten).
     * <p>
     * The return value is the updated source object. It could be used to directly create a tree of
     * objects. For example with a model like:
     *
     * <pre>
     * {@code
     * SalesProduct <>---- Product <>---- CoverageType
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      products.addTargetObjects(salesProduct,
     *          coveragetypes.addTargetObjects(product,
     *              coverageType1, coverageType2));
     * </code>
     * </pre>
     *
     * @param source the object to add a target object to
     * @param effectiveDate the date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * @param targets the objects to add to source
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationAdder @IpsAssociationAdder}. This is the case if the
     *             association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple target objects provided for a ..1 ("to
     *             one") association
     *
     * @since 20.6
     */
    public <S extends IProductComponent> S addTargetObjects(S source,
            Calendar effectiveDate,
            IProductComponent... targets) {

        return addTargetObjects(source, effectiveDate, Arrays.asList(targets));
    }

    /**
     * Adds the target object with the given cardinality to this association in the source object.
     * If this is a ..1 ("to one") association, the target object is set (and thus the potentially
     * existing object is overwritten).
     * <p>
     * The return value is the updated source object. It could be used to directly create a tree of
     * objects. For example with a model like:
     *
     * <pre>
     * {@code
     * SalesProduct <>---- Product <>---- CoverageType
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      products.addTargetObject(salesProduct,
     *          coveragetypes.addTargetObject(product,
     *              coverageType));
     * </code>
     * </pre>
     *
     * @param source the object to add a target object to
     * @param effectiveDate the date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * @param target the object to add to source
     * @param cardinality the cardinality range that will be set for the target
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationAdder @IpsAssociationAdder}. This is the case if the
     *             association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple target objects provided for a ..1 ("to
     *             one") association
     *
     * @since 20.6
     */
    public <S extends IProductComponent> S addTargetObject(S source,
            Calendar effectiveDate,
            IProductComponent target,
            CardinalityRange cardinality) {
        if (addMethodWithCardinality == null) {
            if (isOverriding()) {
                return getSuperAssociation().addTargetObject(source, effectiveDate, target, cardinality);
            } else {
                throw new IllegalArgumentException(
                        String.format(
                                "The association %s on source object %s does not allow %s target objects with cardinality%s.",
                                getName(), source, isToOneAssociation() ? "setting" : "adding",
                                isDerivedUnion() ? " because it is a derived union"
                                        : isMatchingAssociationPresent() ? " because it has no matching association"
                                                : ("; make sure a method annotated with @"
                                                        + IpsAssociationAdder.class.getSimpleName()
                                                        + "(withCardinality = true) exists")));
            }
        }

        Object relevantSource = getRelevantProductObject(source, effectiveDate, isChangingOverTime());
        invokeMethod(addMethodWithCardinality, relevantSource, target, cardinality);
        return source;
    }

    /**
     * Removes the target object from this association in the source object. Does nothing if the
     * target object is not currently referenced (in this association). Sets to <code>null</code> if
     * this is a ..1 ("to one") association.
     * <p>
     * The return value is the updated source object. It could be used to directly remove objects in
     * a tree of objects. For example with a model like:
     *
     * <pre>
     * {@code
     * SalesProduct <>---- Product <>---- CoverageType
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      products.removeTargetObjects(SalesProduct,
     *          coverageTypes.removeTargetObjects(product,
     *              coverageType1, coverageType2));
     * </code>
     * </pre>
     *
     * @param source the object to remove a target object from
     * @param effectiveDate the date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * @param targetsToRemove the objects to remove from this association in source
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationRemover @IpsAssociationRemover} (or
     *             {@link IpsAssociationAdder @IpsAssociationAdder} for a ..1 association). This is
     *             the case if the association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple or no target objects provided for a
     *             ..1 ("to one") association
     *
     * @since 20.6
     */
    public <S extends IProductComponent> S removeTargetObjects(S source,
            Calendar effectiveDate,
            List<IProductComponent> targetsToRemove) {
        if (isToOneAssociation()) {
            if (targetsToRemove.size() > 1) {
                throw new IllegalArgumentException(String.format(
                        "The association %s on source object %s allows a maxmimum of one target object but %s were tried to remove.",
                        getName(), source, targetsToRemove.size()));
            }
            if (targetsToRemove.size() == 1) {
                resetTargetObject(source, effectiveDate, targetsToRemove.get(0));
            }
        } else {
            if (removeMethod == null) {
                if (isOverriding()) {
                    return getSuperAssociation().removeTargetObjects(source, effectiveDate, targetsToRemove);
                } else {
                    throw new IllegalArgumentException(String.format(
                            "The association %s on source object %s does not allow removing target objects%s.",
                            getName(), source,
                            isDerivedUnion() ? " because it is a derived union"
                                    : ("; make sure a method annotated with @"
                                            + IpsAssociationRemover.class.getSimpleName() + " exists")));
                }
            }

            Object relevantSource = getRelevantProductObject(source, effectiveDate, isChangingOverTime());
            for (IProductComponent targetToRemove : targetsToRemove) {
                invokeMethod(removeMethod, relevantSource, targetToRemove);
            }
        }
        return source;
    }

    /**
     * Removes the target object from this association in the source object. Does nothing if the
     * target object is not currently referenced (in this association). Sets to <code>null</code> if
     * this is a ..1 ("to one") association.
     * <p>
     * The return value is the updated source object. It could be used to directly remove objects in
     * a tree of objects. For example with a model like:
     *
     * <pre>
     * {@code
     * SalesProduct <>---- Product <>---- CoverageType
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      products.removeTargetObjects(SalesProduct,
     *          coverageTypes.removeTargetObjects(product,
     *              coverageType1, coverageType2));
     * </code>
     * </pre>
     *
     * @param source the object to remove a target object from
     * @param effectiveDate the date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * @param targetsToRemove the objects to remove from this association in source
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationRemover @IpsAssociationRemover} (or
     *             {@link IpsAssociationAdder @IpsAssociationAdder} for a ..1 association). This is
     *             the case if the association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple or no target objects provided for a
     *             ..1 ("to one") association
     *
     * @since 20.6
     */
    public <S extends IProductComponent> S removeTargetObjects(S source,
            Calendar effectiveDate,
            IProductComponent... targetsToRemove) {
        return removeTargetObjects(source, effectiveDate, Arrays.asList(targetsToRemove));
    }

    /**
     * Returns the {@link PolicyCmptType} identified by {@link #getMatchingAssociationSource()}
     *
     * @see #getMatchingAssociationSource()
     *
     * @return The policy component type of the matching association source
     */
    @Override
    public PolicyCmptType getMatchingAssociationSourceType() {
        return (PolicyCmptType)super.getMatchingAssociationSourceType();
    }

    /**
     * Returns the matching policy component type association or <code>null</code> if no matching
     * association is defined for this association.
     *
     * @return The matching association
     */
    @Override
    public PolicyAssociation getMatchingAssociation() {
        return (PolicyAssociation)super.getMatchingAssociation();
    }

    /**
     * Returns the matching policy component type association or an {@link Optional#empty() empty
     * Optional} if no matching association is defined for this association.
     *
     * @return The matching association
     */
    @Override
    public Optional<PolicyAssociation> findMatchingAssociation() {
        return Optional.ofNullable(getMatchingAssociation());
    }

    /**
     * Checks whether this association is changing over time (resides in the generation) or not
     * (resides in the product component).
     *
     * @return <code>true</code> if this association is changing over time, else <code>false</code>
     */
    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }

    /**
     * {@return <code>true</code> if the association is marked as hidden}
     *
     * @since 25.7
     */
    public boolean isHidden() {
        return getGetterMethod().getAnnotation(IpsAssociation.class).hide();
    }

    /**
     * Retrieves all {@link IProductComponentLink links} for this association from a product
     * component.
     *
     * @param prodCmpt the source product component to retrieve the links from
     * @param effectiveDate the effective-date of the adjustment (a.k.a. product component
     *            generation). Ignored if this is a static association
     *            ({@link #isChangingOverTime()}==false).
     * @return the list of all link instances defined in the product component for this association.
     *             Returns a list with a single link instance for ..1 associations.
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationLinks @IpsAssociationLinks}. This is the case if the
     *             association {@link #isDerivedUnion() is a derived union}.
     *
     * @since 3.22
     */
    public <T extends IProductComponent> Collection<IProductComponentLink<T>> getLinks(IProductComponent prodCmpt,
            Calendar effectiveDate) {
        if (isChangingOverTime()) {
            Object generation = getRelevantProductObject(prodCmpt, effectiveDate, true);
            return getLinksFromObject(generation);
        } else {
            return getLinksFromObject(prodCmpt);
        }
    }

    /**
     * Retrieves the {@link IProductComponentLink link} - based on this association - from the
     * source product component to the target product component, if one such link exists.
     *
     * @param source the source product component to retrieve the links from
     * @param target the target product component of the link
     * @param effectiveDate the effective-date of the adjustment (a.k.a. product component
     *            generation). Ignored if this is a static association
     *            ({@link #isChangingOverTime()}==false).
     * @return the {@link IProductComponentLink link} - based on this association - from the source
     *             product component to the target product component, if one such link exists
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationLinks @IpsAssociationLinks}. This is the case if the
     *             association {@link #isDerivedUnion() is a derived union}.
     *
     * @since 24.1
     */
    @SuppressWarnings("unchecked")
    public <T extends IProductComponent> Optional<IProductComponentLink<T>> getLink(IProductComponent source,
            IProductComponent target,
            Calendar effectiveDate) {
        return getLinks(source, effectiveDate).stream()
                .filter(l -> Objects.equals(l.getTarget(), target))
                .map(l -> ((IProductComponentLink<T>)l))
                .findFirst();
    }

    @Override
    public ProductAssociation getSuperAssociation() {
        return (ProductAssociation)super.getSuperAssociation();
    }

    /**
     * Validates this association's configuration in the given product against the model.
     *
     * @param list a {@link MessageList}, to which validation messages may be added
     * @param context the {@link IValidationContext}, needed to determine the {@link Locale} in
     *            which to create {@link Message Messages}
     * @param source the {@link IProductComponent} to validate
     * @param effectiveDate the date that determines which {@link IProductComponentGeneration} is to
     *            be validated, if the {@link IProductComponent} has any
     */
    @Override
    public void validate(MessageList list,
            IValidationContext context,
            IProductComponent source,
            Calendar effectiveDate) {
        List<IProductComponent> targetObjects = getTargetObjects(source, effectiveDate);
        validateMinCardinality(list, context, source, targetObjects, effectiveDate);
        validateMaxCardinality(list, context, source, targetObjects, effectiveDate);
        validateValidFrom(list, context, source, targetObjects, effectiveDate);
        validateValidTo(list, context, source, targetObjects);
    }

    private void validateMinCardinality(MessageList list,
            IValidationContext context,
            IProductComponent source,
            List<IProductComponent> targetObjects,
            Calendar effectiveDate) {

        validate(list, context,
                () -> targetObjects.size(),
                this::getMinCardinality,
                (links, minCardinality) -> links.compareTo(minCardinality) < 0,
                MSGCODE_MIN_CARDINALITY_NOT_VALID,
                MSGKEY_MIN_CARDINALITY_NOT_VALID,
                PROPERTY_MIN_CARDINALITY,
                new ObjectProperty(source, getName()));

        findMatchingAssociation().ifPresent(
                policyAssociation -> validateTotalMin(list, policyAssociation, getLinks(source, effectiveDate),
                        context));

    }

    private void validateMaxCardinality(MessageList list,
            IValidationContext context,
            IProductComponent source,
            List<IProductComponent> targetObjects,
            Calendar effectiveDate) {
        validate(list, context,
                () -> targetObjects.size(),
                this::getMaxCardinality,
                (links, maxCardinality) -> links.compareTo(maxCardinality) > 0,
                MSGCODE_MAX_CARDINALITY_NOT_VALID,
                MSGKEY_MAX_CARDINALITY_NOT_VALID,
                PROPERTY_MAX_CARDINALITY,
                new ObjectProperty(source, getName()));

        findMatchingAssociation().ifPresent(
                policyAssociation -> validateTotalMax(list, policyAssociation, getLinks(source, effectiveDate),
                        context));

    }

    private void validateTotalMax(MessageList list,
            PolicyAssociation policyAssociation,
            Collection<IProductComponentLink<IProductComponent>> links,
            IValidationContext context) {
        int maxType = policyAssociation.getMaxCardinality();
        if (maxType != Integer.MAX_VALUE) {
            int sumMinCardinality = links.stream()
                    .mapToInt(relation -> relation.getCardinality().getLowerBound())
                    .sum();

            for (IProductComponentLink<IProductComponent> link : links) {
                int sumCardinality;
                if (link.getCardinality().getUpperBound() < Integer.MAX_VALUE) {
                    sumCardinality = sumMinCardinality;
                    sumCardinality += link.getCardinality().getUpperBound();
                    sumCardinality -= link.getCardinality().getLowerBound();
                } else {
                    sumCardinality = Integer.MAX_VALUE;
                }
                if (sumCardinality > maxType) {
                    list.newError(MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX,
                            generateValidationMessage(context.getLocale(),
                                    getResourceBundleName(), MSGKEY_MAX_CARDINALITY_EXCEEDS_MODEL_MAX,
                                    sumCardinality, link.getTargetId(), maxType, getUsedName()),
                            new ObjectProperty(link.getCardinality(), Range.PROPERTY_UPPER_BOUND),
                            new ObjectProperty(link.getSource(), getName()));

                }
            }
        }

    }

    private void validateTotalMin(MessageList list,
            PolicyAssociation policyAssociation,
            Collection<IProductComponentLink<IProductComponent>> links,
            IValidationContext context) {
        int minType = policyAssociation.getMinCardinality();
        for (IProductComponentLink<IProductComponent> link : links) {
            int sumMaxCardinality = link.getCardinality().getLowerBound();
            for (IProductComponentLink<IProductComponent> otherLink : links) {
                if (!link.equals(otherLink)) {
                    if (otherLink.getCardinality().getUpperBound() == Integer.MAX_VALUE) {
                        sumMaxCardinality = Integer.MAX_VALUE;

                        break;
                    }
                    sumMaxCardinality += otherLink.getCardinality().getUpperBound();
                }
            }
            if (sumMaxCardinality < minType) {
                list.newError(MSGCODE_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN,
                        generateValidationMessage(context.getLocale(),
                                getResourceBundleName(), MSGKEY_MIN_CARDINALITY_FALLS_BELOW_MODEL_MIN,
                                sumMaxCardinality, link.getTargetId(), minType, getUsedName()),
                        new ObjectProperty(link.getCardinality(), Range.PROPERTY_LOWER_BOUND),
                        new ObjectProperty(link.getSource(), getName()));

            }

        }

    }

    private void validateValidTo(MessageList list,
            IValidationContext context,
            IProductComponent source,
            List<IProductComponent> targetObjects) {
        for (IProductComponent target : targetObjects) {
            validate(list,
                    () -> target.getValidTo(),
                    () -> source.getValidTo(),
                    (targetDate, sourceDate) -> targetDate.compareTo(sourceDate) < 0,
                    (targetDate, sourceDate) -> generateValidationMessage(context.getLocale(),
                            getResourceBundleName(), MSGKEY_DATE_TO_NOT_VALID,
                            targetDate, target, sourceDate, source),
                    MSGCODE_DATE_TO_NOT_VALID,
                    new ObjectProperty(target, IProductComponent.PROPERTY_VALID_TO),
                    new ObjectProperty(source, getName()));
        }

    }

    private void validateValidFrom(MessageList list,
            IValidationContext context,
            IProductComponent source,
            List<IProductComponent> targetObjects,
            Calendar effectiveDate) {
        for (IProductComponent target : targetObjects) {

            validate(list,
                    () -> target.getValidFrom(),
                    () -> isChangingOverTime() ? source.getGenerationBase(effectiveDate).getValidFrom()
                            : source.getValidFrom(),
                    (targetDate, sourceDate) -> targetDate.compareTo(sourceDate) > 0,
                    (targetDate, sourceDate) -> generateValidationMessage(context.getLocale(),
                            getResourceBundleName(), MSGKEY_DATE_FROM_NOT_VALID,
                            targetDate, target, sourceDate, source),
                    MSGCODE_DATE_FROM_NOT_VALID,
                    new ObjectProperty(target, IProductComponent.PROPERTY_VALID_FROM),
                    new ObjectProperty(source, getName()));
        }

    }

    /**
     * ..1 associations have no remove method. Call set(null) instead. Does nothing if the currently
     * associated object is not equal to targetToReset.
     *
     * Uses getTargetObjects().contains() to check whether the single instance is the one that
     * should be removed.
     */
    private <S extends IProductComponent> void resetTargetObject(S source,
            Calendar effectiveDate,
            IProductComponent targetToReset) {
        if (getTargetObjects(source, effectiveDate).contains(targetToReset)) {
            addTargetObjects(source, effectiveDate, new IProductComponent[] { null });
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends IProductComponent> Collection<IProductComponentLink<T>> getLinksFromObject(
            Object prodCmptOrGeneration) {
        if (getLinksMethod == null) {
            if (isOverriding()) {
                return getSuperAssociation().getLinksFromObject(prodCmptOrGeneration);
            } else {
                throw new IllegalArgumentException(String.format(
                        "The association %s on %s does not allow retrieving links%s.", getName(), prodCmptOrGeneration,
                        isDerivedUnion() ? " because it is a derived union"
                                : ("; make sure a method annotated with @" + IpsAssociationLinks.class + " exists")));
            }
        }
        if (isToOneAssociation()) {
            IProductComponentLink<T> link = (IProductComponentLink<T>)invokeMethod(getLinksMethod,
                    prodCmptOrGeneration);
            if (link == null) {
                return List.of();
            } else {
                return Collections.singletonList(link);
            }
        } else {
            return (Collection<IProductComponentLink<T>>)invokeMethod(getLinksMethod, prodCmptOrGeneration);
        }
    }

}
