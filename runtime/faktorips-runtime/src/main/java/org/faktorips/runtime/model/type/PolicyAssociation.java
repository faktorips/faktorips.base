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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;

/**
 * An association between two {@link PolicyCmptType PolicyCmptTypes}.
 */
public class PolicyAssociation extends Association {

    private final Method addMethod;
    private final Method removeMethod;

    /**
     *
     * @param type the type the association belongs to
     * @param getterMethod the getter method for retrieving all associated instances
     * @param addMethod the method for associating new instances (add-method for ..N associations,
     *            set-method for ..1 associations)
     * @param removeMethod the method for removing instances from the association (<code>null</code>
     *            in case of a ..1 association, as no method is generated)
     */
    public PolicyAssociation(Type type, Method getterMethod, Method addMethod, Method removeMethod) {
        super(type, getterMethod);
        this.addMethod = addMethod;
        this.removeMethod = removeMethod;
    }

    /**
     * Returns the {@link PolicyCmptType} this association belongs to.
     */
    @Override
    public PolicyCmptType getType() {
        return (PolicyCmptType)super.getType();
    }

    /**
     * Returns the model type this association belongs to.
     *
     * @deprecated Use {@link #getType()}
     */
    @Deprecated
    @Override
    public PolicyCmptType getModelType() {
        return getType();
    }

    @Override
    public PolicyAssociation createOverwritingAssociationFor(Type subType) {
        return new PolicyAssociation(subType, getGetterMethod(), addMethod, removeMethod);
    }

    /**
     * Returns the {@link ProductCmptType} identified by {@link #getMatchingAssociationSource()}
     *
     * @see #getMatchingAssociationSource()
     *
     * @return The model type object of the matching association source
     */
    @Override
    public ProductCmptType getMatchingAssociationSourceType() {
        return (ProductCmptType)super.getMatchingAssociationSourceType();
    }

    /**
     * Returns the matching product component type association or <code>null</code> if no matching
     * association is defined for this association.
     *
     * @return The matching association
     */
    @Override
    public ProductAssociation getMatchingAssociation() {
        return (ProductAssociation)super.getMatchingAssociation();
    }

    /**
     * Returns the matching product component type association or an {@link Optional#empty() empty
     * Optional} if no matching association is defined for this association.
     *
     * @return The matching association
     */
    @Override
    public Optional<ProductAssociation> findMatchingAssociation() {
        return Optional.ofNullable(getMatchingAssociation());
    }

    @Override
    public PolicyCmptType getTarget() {
        return (PolicyCmptType)super.getTarget();
    }

    /**
     * Returns a list of the target(s) of the given model object's association identified by this
     * model type association.
     *
     * @param source a model object corresponding to the {@link PolicyCmptType} this association
     *            belongs to
     * @return a list of the target(s) of the given model object's association identified by this
     *             model type association
     * @throws IllegalArgumentException if the model object does not have an association fitting
     *             this model type association or that association is not accessible for any reason
     */
    public List<IModelObject> getTargetObjects(IModelObject source) {
        List<IModelObject> targets = new ArrayList<>();
        Object object = invokeMethod(getGetterMethod(), source);
        if (object instanceof Iterable<?> it) {
            for (Object target : it) {
                targets.add((IModelObject)target);
            }
        } else if (object instanceof IModelObject modelObject) {
            targets.add(modelObject);
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
     * Policy <>---- Contract <>---- Coverage
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      contracts.addTargetObjects(policy,
     *          coverages.addTargetObjects(contract,
     *              coverage1, coverage2));
     * </code>
     * </pre>
     *
     * @param source the object to add a target object to
     * @param targets the objects to add to source
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationAdder @IpsAssociationAdder}. This is the case if the
     *             association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple target objects provided for a ..1 ("to
     *             one") association
     *
     * @since 3.22
     */
    public <S extends IModelObject> S addTargetObjects(S source, Collection<IModelObject> targets) {
        if (isToOneAssociation() && targets.size() > 1) {
            throw new IllegalArgumentException(String.format(
                    "The association %s on source object %s allows a maxmimum of one target object but %s were provided.",
                    getName(), source, targets.size()));
        } else {
            if (addMethod == null) {
                if (isOverriding()) {
                    return getSuperAssociation().addTargetObjects(source, targets);
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

        for (IModelObject target : targets) {
            invokeMethod(addMethod, source, target);
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
     * Policy <>---- Contract <>---- Coverage
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      contracts.addTargetObjects(policy,
     *          coverages.addTargetObjects(contract,
     *              coverage1, coverage2));
     * </code>
     * </pre>
     *
     * @param source the object to add a target object to
     * @param targets the objects to add to source
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationAdder @IpsAssociationAdder}. This is the case if the
     *             association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple target objects provided for a ..1 ("to
     *             one") association
     *
     * @since 3.22
     */
    public <S extends IModelObject> S addTargetObjects(S source, IModelObject... targets) {
        return addTargetObjects(source, Arrays.asList(targets));
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
     * Policy <>---- Contract <>---- Coverage
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      contracts.removeTargetObjects(policy,
     *          coverages.removeTargetObjects(contract,
     *              coverage1, coverage2));
     * </code>
     * </pre>
     *
     * @param source the object to remove a target object from
     * @param targetsToRemove the objects to remove from this association in source
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationRemover @IpsAssociationRemover} (or
     *             {@link IpsAssociationAdder @IpsAssociationAdder} for a ..1 association). This is
     *             the case if the association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple or no target objects provided for a
     *             ..1 ("to one") association
     *
     * @since 3.22
     */
    public <S extends IModelObject> S removeTargetObjects(S source, List<IModelObject> targetsToRemove) {
        if (isToOneAssociation()) {
            if (targetsToRemove.size() > 1) {
                throw new IllegalArgumentException(String.format(
                        "The association %s on source object %s allows a maxmimum of one target object but %s were tried to remove.",
                        getName(), source, targetsToRemove.size()));
            }
            if (targetsToRemove.size() == 1) {
                resetTargetObject(source, targetsToRemove.get(0));
            }
        } else {
            if (removeMethod == null) {
                if (isOverriding()) {
                    return getSuperAssociation().removeTargetObjects(source, targetsToRemove);
                } else {
                    throw new IllegalArgumentException(String.format(
                            "The association %s on source object %s does not allow removing target objects%s.",
                            getName(), source,
                            isDerivedUnion() ? " because it is a derived union"
                                    : ("; make sure a method annotated with @"
                                            + IpsAssociationRemover.class.getSimpleName() + " exists")));
                }
            }
            for (IModelObject targetToRemove : targetsToRemove) {
                invokeMethod(removeMethod, source, targetToRemove);
            }
        }
        return source;
    }

    @Override
    public PolicyAssociation getSuperAssociation() {
        return (PolicyAssociation)super.getSuperAssociation();
    }

    /**
     * Removes the target objects from this association in the source object. Does nothing if the
     * target object is not currently referenced (in this association). Sets to <code>null</code> if
     * this is a ..1 ("to one") association.
     * <p>
     * The return value is the updated source object. It could be used to directly remove objects in
     * a tree of objects. For example with a model like:
     *
     * <pre>
     * {@code
     * Policy <>---- Contract <>---- Coverage
     * }
     * </pre>
     *
     * You could write such code:
     *
     * <pre>
     * <code>
     *      contracts.removeTargetObjects(policy,
     *          coverages.removeTargetObjects(contract,
     *              coverage1, coverage2));
     * </code>
     * </pre>
     *
     * @param source the object to remove a target object from
     * @param targetsToRemove the objects to remove from this association in source
     * @return the changed source object
     * @throws IllegalArgumentException if there is no method annotated with
     *             {@link IpsAssociationRemover @IpsAssociationRemover} (or
     *             {@link IpsAssociationAdder @IpsAssociationAdder} for a ..1 association). This is
     *             the case if the association {@link #isDerivedUnion() is a derived union}.
     * @throws IllegalArgumentException if there are multiple target objects provided for a ..1 ("to
     *             one") association
     *
     * @since 3.22
     */
    public <S extends IModelObject> S removeTargetObjects(S source, IModelObject... targetsToRemove) {
        return removeTargetObjects(source, Arrays.asList(targetsToRemove));
    }

    /**
     * ..1 associations have no remove method. Call set(null) instead. Does nothing if the currently
     * associated object is not equal to targetToReset.
     *
     * Uses getTargetObjects().contains() to check whether the single instance is the one that
     * should be removed.
     */
    private <S extends IModelObject> void resetTargetObject(S source, IModelObject targetToReset) {
        if (getTargetObjects(source).contains(targetToReset)) {
            addTargetObjects(source, new IModelObject[] { null });
        }
    }

    /**
     * Returns whether this association is "qualified". A "qualified" {@link PolicyAssociation}'s
     * cardinalities are not defined for all targets but per target of the matching
     * {@link ProductAssociation}.
     *
     * @since 22.6
     */
    public boolean isQualified() {
        return getAnnotation().qualified();
    }
}
