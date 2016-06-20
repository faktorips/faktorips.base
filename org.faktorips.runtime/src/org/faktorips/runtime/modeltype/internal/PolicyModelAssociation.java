/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IPolicyModelAssociation;
import org.faktorips.runtime.modeltype.IProductModel;

public class PolicyModelAssociation extends ModelTypeAssociation implements IPolicyModelAssociation {

    public PolicyModelAssociation(ModelType modelType, Method getterMethod) {
        super(modelType, getterMethod);
    }

    @Override
    public PolicyModel getModelType() {
        return (PolicyModel)super.getModelType();
    }

    @Override
    public PolicyModelAssociation createOverwritingAssociationFor(ModelType subModelType) {
        return new PolicyModelAssociation(subModelType, getGetterMethod());
    }

    @Override
    public IProductModel getMatchingAssociationSourceType() {
        return (IProductModel)super.getMatchingAssociationSourceType();
    }

    @Override
    public IPolicyModel getTarget() {
        return (IPolicyModel)super.getTarget();
    }

    @Override
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
