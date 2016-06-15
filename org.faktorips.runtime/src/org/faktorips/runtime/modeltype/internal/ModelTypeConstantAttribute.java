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

import java.lang.reflect.Field;

import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;

/**
 * A {@link ModelTypeConstantAttribute} represent an attribute from the PolicyCmptType or the
 * ProductCmptType.
 */
public class ModelTypeConstantAttribute extends AbstractModelTypeAttribute {

    private final Field field;

    public ModelTypeConstantAttribute(ModelType modelType, Field field) {
        super(modelType, field.getAnnotation(IpsAttribute.class), field.getAnnotation(IpsExtensionProperties.class),
                field.getType());
        this.field = field;
    }

    @Override
    public boolean isProductRelevant() {
        return false;
    }

    @Override
    public Object getValue(IModelObject source) {
        try {
            return field.get(source);
        } catch (IllegalAccessException e) {
            handleGetterError(source, e);
        } catch (SecurityException e) {
            handleGetterError(source, e);
        }
        return null;
    }

    @Override
    public void setValue(IModelObject source, Object value) {
        throw new IllegalArgumentException("Cannot modify a constant field.");
    }

    @Override
    ModelTypeConstantAttribute createOverwritingAttributeFor(ModelType subModelType) {
        return new ModelTypeConstantAttribute(subModelType, field);
    }

}
