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
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.modeltype.IPolicyModel;
import org.faktorips.runtime.modeltype.IProductModel;

public class PolicyModel extends ModelType implements IPolicyModel {

    public PolicyModel(String name, AnnotatedType annotatedModelType) {
        super(name, annotatedModelType);
    }

    @Override
    public boolean isConfiguredByPolicyCmptType() {
        return getAnnotatedModelType().is(IpsConfiguredBy.class);
    }

    @Override
    public IProductModel getProductCmptType() {
        return Models.getProductModel(getAnnotatedModelType().get(IpsConfiguredBy.class).value()
                .asSubclass(IProductComponent.class));
    }

    @Override
    public IPolicyModel getSuperType() {
        Class<?> superclass = getJavaClass().getSuperclass();
        return Models.isPolicyModel(superclass) ? Models.getPolicyModel(superclass.asSubclass(IModelObject.class))
                : null;
    }

}
