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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsAttribute;
import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.IPolicyModelAttribute;
import org.faktorips.runtime.modeltype.internal.ModelType.AnnotatedElementMatcher;
import org.faktorips.valueset.ValueSet;

public class PolicyModelAttribute extends AbstractModelAttribute implements IPolicyModelAttribute {

    private final Method getter;

    private final Method setter;

    private Method defaultValueMethod;
    private Map<ModelType, Method> valueSetMethods = new HashMap<ModelType, Method>(2);

    public PolicyModelAttribute(PolicyModel modelType, Method getter, Method setter, boolean changingOverTime) {
        super(modelType, getter.getAnnotation(IpsAttribute.class), getter.getAnnotation(IpsExtensionProperties.class),
                getter.getReturnType(), changingOverTime);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public boolean isProductRelevant() {
        return getter.isAnnotationPresent(IpsConfiguredAttribute.class);
    }

    @Override
    public Object getValue(IModelObject source) {
        return invokeMethod(getter, source);
    }

    @Override
    public void setValue(IModelObject source, Object value) {
        if (setter == null) {
            throw new IllegalArgumentException(String.format("There is no setter for attribute %s in type %s.",
                    getName(), getModelType().getName()));
        }
        invokeMethod(setter, source, value);
    }

    @Override
    public PolicyModel getModelType() {
        return (PolicyModel)super.getModelType();
    }

    @Override
    public Object getDefaultValue(IConfigurableModelObject modelObject) {
        return getDefaultValue(modelObject.getProductComponent(), modelObject.getEffectiveFromAsCalendar());
    }

    @Override
    public Object getDefaultValue(IProductComponent source, Calendar effectiveDate) {
        if (!isProductRelevant()) {
            throw new IllegalStateException("Trying to find default value method in product class, but policy class "
                    + getModelType().getJavaClass() + " is not configurable.");
        }
        return invokeMethod(getDefaultValueMethod(getModelType().getProductCmptType()),
                getRelevantProductObject(source, effectiveDate));
    }

    private Method getDefaultValueMethod(ModelType modelType) {
        if (defaultValueMethod == null) {
            defaultValueMethod = findDefaultValueMethod(modelType);
        }
        return defaultValueMethod;
    }

    private Method findDefaultValueMethod(ModelType modelType) {
        AnnotatedElementMatcher<IpsDefaultValue> filter = new AnnotatedElementMatcher<IpsDefaultValue>() {
            @Override
            public boolean matches(IpsDefaultValue ann) {
                return ann.value().equals(getName());
            }
        };
        return findMethod(IpsDefaultValue.class, filter, "default value", modelType);
    }

    @Override
    public ValueSet<?> getValueSet(IModelObject modelObject, IValidationContext context) {
        return (ValueSet<?>)invokeMethod(getValueSetMethod(getModelType()), modelObject, context);
    }

    @Override
    public ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate, IValidationContext context) {
        return (ValueSet<?>)invokeMethod(getValueSetMethod(getModelType().getProductCmptType()),
                getRelevantProductObject(source, effectiveDate), context);
    }

    private Method getValueSetMethod(ModelType model) {
        Method valueSetMethod = valueSetMethods.get(model);
        if (valueSetMethod == null) {
            valueSetMethod = findValueSetMethod(model);
            valueSetMethods.put(model, valueSetMethod);
        }
        return valueSetMethod;
    }

    @Override
    public PolicyModelAttribute createOverwritingAttributeFor(ModelType subModelType) {
        return new PolicyModelAttribute((PolicyModel)subModelType, getter, setter, isChangingOverTime());
    }

    private Method findValueSetMethod(ModelType modelType) {
        AnnotatedElementMatcher<IpsAllowedValues> filter = new AnnotatedElementMatcher<IpsAllowedValues>() {
            @Override
            public boolean matches(IpsAllowedValues ann) {
                return ann.value().equals(getName());
            }
        };
        return findMethod(IpsAllowedValues.class, filter, "allowed values", modelType);
    }

    private <T extends Annotation> Method findMethod(Class<T> annotationClass,
            AnnotatedElementMatcher<T> filter,
            String methodDescription,
            ModelType modelType) {
        Method method = modelType.searchDeclaredMethod(annotationClass, filter);
        if (method == null) {
            throw new IllegalStateException("No method found for retrieving the " + methodDescription
                    + " of attribute: " + getName());
        }
        return method;
    }
}
