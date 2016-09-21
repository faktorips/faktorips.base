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

    public PolicyAssociation(Type type, Method getterMethod) {
        super(type, getterMethod);
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
        return new PolicyAssociation(subType, getGetterMethod());
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

}
