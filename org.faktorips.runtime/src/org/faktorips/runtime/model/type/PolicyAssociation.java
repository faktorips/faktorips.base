/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import java.util.List;

import org.faktorips.runtime.IModelObject;

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
     *         model type association
     * @throws IllegalArgumentException if the model object does not have an association fitting
     *             this model type association or that association is not accessible for any reason
     */
    public List<IModelObject> getTargetObjects(IModelObject source) {
        List<IModelObject> targets = new ArrayList<IModelObject>();
        Object object = invokeMethod(getGetterMethod(), source);
        if (object instanceof Iterable<?>) {
            for (Object target : (Iterable<?>)object) {
                targets.add((IModelObject)target);
            }
        } else if (object instanceof IModelObject) {
            targets.add((IModelObject)object);
        }
        return targets;
    }

    /**
     * Adds the target object to this association in the source object. If this is a ..1 ("to one")
     * association, the target object is set (and thus the potentially existing object is
     * overwritten).
     * 
     * @param source the object to add a target object to
     * @param target the object to add to source
     * @return the changed source object
     */
    public <S extends IModelObject> S addTargetObject(S source, IModelObject target) {
        invokeMethod(addMethod, source, target);
        return source;
    }

    /**
     * Removes the target object from this association in the source object. Does nothing if the
     * target object is not currently referenced (in this association). Sets to <code>null</code> if
     * this is a ..1 ("to one") association.
     * 
     * @param source the object to remove a target object from
     * @param targetToRemove the object to remove from this association in source
     * @return the changed source object
     */
    public <S extends IModelObject> S removeTargetObject(S source, IModelObject targetToRemove) {
        if (isToOneAssociation()) {
            resetTargetObject(source, targetToRemove);
        } else {
            invokeMethod(removeMethod, source, targetToRemove);
        }
        return source;
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
            addTargetObject(source, null);
        }
    }
}
