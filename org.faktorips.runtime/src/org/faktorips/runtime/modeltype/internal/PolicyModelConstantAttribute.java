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
import java.util.Calendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IPolicyModelAttribute;
import org.faktorips.valueset.ValueSet;

/**
 * A {@link PolicyModelConstantAttribute} represent an attribute from the PolicyCmptType or the
 * ProductCmptType.
 */
public class PolicyModelConstantAttribute extends AbstractModelAttribute implements IPolicyModelAttribute {

    private final Field field;

    public PolicyModelConstantAttribute(ModelType modelType, Field field, boolean changingOverTime) {
        super(modelType, field.getAnnotation(IpsAttribute.class), field.getAnnotation(IpsExtensionProperties.class),
                field.getType(), changingOverTime);
        this.field = field;
    }

    @Override
    public boolean isProductRelevant() {
        return false;
    }

    @Override
    public Object getValue(IModelObject source) {
        return invokeField(field, source);
    }

    @Override
    public void setValue(IModelObject source, Object value) {
        throw new UnsupportedOperationException("Cannot modify a constant field.");
    }

    @Override
    public PolicyModelConstantAttribute createOverwritingAttributeFor(ModelType subModelType) {
        return new PolicyModelConstantAttribute(subModelType, field, isChangingOverTime());
    }

    @Override
    public Object getDefaultValue(IConfigurableModelObject modelObject) {
        return getValue(modelObject);
    }

    @Override
    public Object getDefaultValue(IProductComponent source, Calendar effectiveDate) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

    @Override
    public ValueSet<?> getValueSet(IConfigurableModelObject modelObject, IValidationContext context) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

    @Override
    public ValueSet<?> getValueSet(IModelObject modelObject, IValidationContext context) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

    @Override
    public ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate, IValidationContext context) {
        throw new UnsupportedOperationException(getName() + " is a constant field and has no product configuration");
    }

}
