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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IProductModel;
import org.faktorips.runtime.modeltype.IProductModelAssociation;

public class ProductModelAssociation extends ModelTypeAssociation implements IProductModelAssociation {

    private boolean changingOverTime;

    public ProductModelAssociation(ModelType modelType, Method getterMethod, boolean changingOverTime) {
        super(modelType, getterMethod);
        this.changingOverTime = changingOverTime;
    }

    @Override
    public ProductModelAssociation createOverwritingAssociationFor(ModelType subModelType) {
        return new ProductModelAssociation(subModelType, getGetterMethod(), changingOverTime);
    }

    @Override
    public ProductModel getModelType() {
        return (ProductModel)super.getModelType();
    }

    @Override
    public IProductModel getTarget() {
        return (IProductModel)super.getTarget();
    }

    @Override
    public List<IProductComponent> getTargetObjects(IProductComponent productComponentSource, Calendar effectiveDate) {
        List<IProductComponent> targets = new ArrayList<IProductComponent>();

        try {
            Object returnValue;
            if (isChangingOverTime()) {
                returnValue = getGetterMethod().invoke(productComponentSource.getGenerationBase(effectiveDate));
            } else {
                returnValue = getGetterMethod().invoke(productComponentSource);

            }
            if (returnValue instanceof Iterable<?>) {
                for (Object target : (Iterable<?>)returnValue) {
                    targets.add((IProductComponent)target);
                }
            } else if (returnValue instanceof IProductComponent) {
                targets.add((IProductComponent)returnValue);
            }

        } catch (IllegalAccessException e) {
            handleGetterError(productComponentSource, e);
        } catch (InvocationTargetException e) {
            handleGetterError(productComponentSource, e);
        }
        return targets;
    }

    private void handleGetterError(IProductComponent source, Exception e) {
        throw new IllegalArgumentException(String.format("Could not get target %s on source object %s.", getUsedName(),
                source), e);
    }

    @Override
    public IPolicyModel getMatchingAssociationSourceType() {
        return (IPolicyModel)super.getMatchingAssociationSourceType();
    }

    @Override
    public boolean isChangingOverTime() {
        return changingOverTime;
    }
}
