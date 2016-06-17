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
import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IProductModelAttribute;

public class ProductModelAttribute extends AbstractModelAttribute implements IProductModelAttribute {

    private final Method getter;

    private final Method setter;

    public ProductModelAttribute(ModelType modelType, boolean changingOverTime, Method getter, Method setter) {
        super(modelType, getter.getAnnotation(IpsAttribute.class), getter.getAnnotation(IpsExtensionProperties.class),
                getter.getReturnType(), changingOverTime);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public boolean isProductRelevant() {
        return true;
    }

    @Override
    public AbstractModelAttribute createOverwritingAttributeFor(ModelType subModelType) {
        return new ProductModelAttribute(subModelType, isChangingOverTime(), getter, setter);
    }

    @Override
    public Object getValue(IProductComponent productComponent, Calendar effectiveDate) {
        Object source = getRelevantProductObject(productComponent, effectiveDate);
        try {
            return getter.invoke(source);
        } catch (IllegalAccessException e) {
            handleGetterError(source, e);
        } catch (InvocationTargetException e) {
            handleGetterError(source, e);
        } catch (SecurityException e) {
            handleGetterError(source, e);
        }
        return null;
    }

}
