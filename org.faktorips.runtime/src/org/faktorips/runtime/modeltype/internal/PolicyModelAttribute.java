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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.model.annotation.IpsAllowedValues;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.modeltype.IModelType;
import org.faktorips.runtime.modeltype.internal.ModelType.AnnotatedElementMatcher;
import org.faktorips.valueset.ValueSet;

public class PolicyModelAttribute extends ModelTypeAttribute {

    private Method defaultValueMethod;
    private Method valueSetMethod;

    public PolicyModelAttribute(PolicyModel modelType, Method getterMethod, Method setterMethod) {
        super(modelType, getterMethod, setterMethod);
    }

    @Override
    public PolicyModel getModelType() {
        return (PolicyModel)super.getModelType();
    }

    /**
     * Returns the product configured default value of the attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getDefaultValue() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     * 
     * @param modelObject the configurable model object from which product component and (if
     *            necessary) effective date can be retrieved
     * @see #getDefaultValue(IProductComponent, Calendar)
     */
    public Object getDefaultValue(IConfigurableModelObject modelObject) {
        return getDefaultValue(modelObject.getProductComponent(), modelObject.getEffectiveFromAsCalendar());
    }

    /**
     * Returns the product configured default value of the attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getDefaultValue() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     * 
     * @param source the product component to read the attribute default value from.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     */
    public Object getDefaultValue(IProductComponent source, Calendar effectiveDate) {
        return invokeGetterMethod(getDefaultValueMethod(), getRelevantProductObject(source, effectiveDate),
                "default value");
    }

    private Method getDefaultValueMethod() {
        if (defaultValueMethod == null) {
            defaultValueMethod = findDefaultValueMethod();
        }
        return defaultValueMethod;
    }

    private Method findDefaultValueMethod() {
        AnnotatedElementMatcher<IpsDefaultValue> filter = new AnnotatedElementMatcher<IpsDefaultValue>() {
            @Override
            public boolean matches(IpsDefaultValue ann) {
                return ann.value().equals(getName());
            }
        };
        return findMethod(IpsDefaultValue.class, filter, "default value", false);
    }

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getAllowedValues() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     *
     * @param modelObject the configurable model object from which product component and (if
     *            necessary) effective date can be retrieved
     * @see #getValueSet(IProductComponent, Calendar, IValidationContext)
     */
    public ValueSet<?> getValueSet(IConfigurableModelObject modelObject, IValidationContext context) {
        return getValueSet((IModelObject)modelObject, context);
    }

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getAllowedValues() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     *
     * @param modelObject a model object
     */
    public ValueSet<?> getValueSet(IModelObject modelObject, IValidationContext context) {
        if (isProductRelevant() && modelObject instanceof IConfigurableModelObject) {
            IConfigurableModelObject configurableModelObject = (IConfigurableModelObject)modelObject;
            return getValueSet(configurableModelObject.getProductComponent(),
                    configurableModelObject.getEffectiveFromAsCalendar(), context);
        }
        return (ValueSet<?>)invokeGetterMethod(getValueSetMethod(), modelObject, "value set", context);
    }

    /**
     * Returns the value set of the given model object's attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getAllowedValues() method for this attribute. This also occurs if the corresponding policy
     * class is not configured by a product class.
     *
     * @param source the product component to read an attribute value set from. Must correspond to
     *            the {@link IModelType} this attribute belongs to.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     */
    public ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate, IValidationContext context) {
        return (ValueSet<?>)invokeGetterMethod(getValueSetMethod(), getRelevantProductObject(source, effectiveDate),
                "value set", context);
    }

    private Method getValueSetMethod() {
        if (valueSetMethod == null) {
            valueSetMethod = findValueSetMethod();
        }
        return valueSetMethod;
    }

    private Method findValueSetMethod() {
        AnnotatedElementMatcher<IpsAllowedValues> filter = new AnnotatedElementMatcher<IpsAllowedValues>() {
            @Override
            public boolean matches(IpsAllowedValues ann) {
                return ann.value().equals(getName());
            }
        };
        return findMethod(IpsAllowedValues.class, filter, "allowed values", true);
    }

    private Object invokeGetterMethod(Method method, Object source, String kindOfGetter, Object... params) {
        try {
            return method.invoke(source, params);
        } catch (IllegalArgumentException e) {
            handleGetterError(source, e, kindOfGetter);
        } catch (IllegalAccessException e) {
            handleGetterError(source, e, kindOfGetter);
        } catch (InvocationTargetException e) {
            handleGetterError(source, e, kindOfGetter);
        } catch (SecurityException e) {
            handleGetterError(source, e, kindOfGetter);
        }
        return null;
    }

    private <T extends Annotation> Method findMethod(Class<T> annotationClass,
            AnnotatedElementMatcher<T> filter,
            String methodDescription,
            boolean fallbackToPolicy) {
        if (!getModelType().isConfiguredByPolicyCmptType() || !isProductRelevant()) {
            if (fallbackToPolicy) {
                return findMethod(annotationClass, filter, methodDescription, getModelType());
            } else {
                throw new IllegalStateException("Trying to find " + methodDescription
                        + " method in product class, but policy class " + getModelType().getJavaClass()
                        + " is not configurable.");
            }
        }
        return findMethod(annotationClass, filter, methodDescription, (ProductModel)getModelType().getProductCmptType());
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
