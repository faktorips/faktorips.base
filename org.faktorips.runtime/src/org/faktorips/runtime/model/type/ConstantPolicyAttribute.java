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

import java.lang.reflect.Field;
import java.util.Calendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.values.NullObject;
import org.faktorips.valueset.OrderedValueSet;
import org.faktorips.valueset.ValueSet;

/**
 * An {@linkplain PolicyCmptType PolicyCmptType's} {@link AttributeKind#CONSTANT CONSTANT}
 * attribute.
 */
public class ConstantPolicyAttribute extends PolicyAttribute {

    private final Field field;

    public ConstantPolicyAttribute(Type type, Field field, boolean changingOverTime) {
        super(type, field.getAnnotation(IpsAttribute.class), field.getAnnotation(IpsExtensionProperties.class),
                field.getType(), changingOverTime);
        this.field = field;
    }

    @Override
    public boolean isProductRelevant() {
        return false;
    }

    @Override
    public Object getValue(IModelObject modelObject) {
        return invokeField(field, modelObject);
    }

    @Override
    public void setValue(IModelObject modelObject, Object value) {
        throw new UnsupportedOperationException("Cannot modify a constant field.");
    }

    @Override
    public void removeValue(IModelObject modelObject) {
        throw new UnsupportedOperationException("Cannot modify a constant field.");
    }

    @Override
    public ConstantPolicyAttribute createOverwritingAttributeFor(Type subType) {
        return new ConstantPolicyAttribute(subType, field, isChangingOverTime());
    }

    @Override
    public Object getDefaultValue(IModelObject modelObject) {
        return getValue(modelObject);
    }

    @Override
    public Object getDefaultValue(IProductComponent source, Calendar effectiveDate) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

    @Override
    public void setDefaultValue(IConfigurableModelObject modelObject, Object defaultValue) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

    @Override
    public void setDefaultValue(IProductComponent source, Calendar effectiveDate, Object defaultValue) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

    @Override
    public ValueSet<?> getValueSet(IModelObject modelObject, IValidationContext context) {
        Object value = getValue(modelObject);
        if (value == null) {
            return new OrderedValueSet<>(true, null);
        } else if (value instanceof NullObject) {
            return new OrderedValueSet<>(true, value);
        } else {
            return new OrderedValueSet<>(false, null, value);
        }
    }

    @Override
    public ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate, IValidationContext context) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

    @Override
    public void setValueSet(IProductComponent source, Calendar effectiveDate, ValueSet<?> valueSet) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

    @Override
    public void setValueSet(IConfigurableModelObject modelObject, ValueSet<?> valueSet) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

}
