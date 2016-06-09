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

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.Models;
import org.faktorips.runtime.model.annotation.AnnotatedType;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsConfigures;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IProductModel;

public class ProductModel extends ModelType implements IProductModel {

    public ProductModel(String name, AnnotatedType annotatedModelType) {
        super(name, annotatedModelType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChangingOverTime() {
        return getAnnotatedModelType().is(IpsChangingOverTime.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigurationForPolicyCmptType() {
        return getAnnotatedModelType().is(IpsConfigures.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPolicyModel getPolicyCmptType() {
        return Models.getPolicyModel(getAnnotatedModelType().get(IpsConfigures.class).value()
                .asSubclass(IModelObject.class));
    }

    @Override
    public IProductModel getSuperType() {
        Class<?> superclass = getJavaClass().getSuperclass();
        return Models.isProductModel(superclass) ? Models.getProductModel(superclass
                .asSubclass(IProductComponent.class)) : null;
    }

}
