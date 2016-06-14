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

import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.model.annotation.IpsDefaultValue;
import org.faktorips.runtime.modeltype.internal.ModelType.AnnotatedElementMatcher;

public class PolicyModelAttribute extends ModelTypeAttribute {

    private Method defaultValueMethod;

    public PolicyModelAttribute(PolicyModel modelType, Method getterMethod, Method setterMethod) {
        super(modelType, getterMethod, setterMethod);
    }

    /**
     * Returns the product configured default value of the attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getDefaultValue() method. This also occurs if the corresponding policy class is not
     * configured by a product class.
     * 
     * @param source the product component to read the attribute default value from.
     * @param effectiveDate the date to determine the product component generation. If
     *            <code>null</code> the latest generation is used. Is ignored if the attribute's
     *            configuration is not changing over time.
     */
    public Object getDefaultValue(IProductComponent source, Calendar effectiveDate) {
        return invokeDefaultValueMethod(getRelevantProductObject(source, effectiveDate));
    }

    /**
     * Returns the product configured default value of the attribute identified by this model type
     * attribute. Throws an {@link IllegalArgumentException} if the model object has no
     * getDefaultValue() method. This also occurs if the corresponding policy class is not
     * configured by a product class.
     * 
     * @param modelObject the configurable model object from which product component and (if
     *            necessary) effective date can be retrieved
     * @see #getDefaultValue(IProductComponent, Calendar)
     */
    public Object getDefaultValue(IConfigurableModelObject modelObject) {
        return invokeDefaultValueMethod(getRelevantProductObject(modelObject.getProductComponent(),
                modelObject.getEffectiveFromAsCalendar()));
    }

    private Object invokeDefaultValueMethod(Object source) {
        try {
            return getDefaultValueMethod().invoke(source);
        } catch (IllegalAccessException e) {
            handleDefaultValueError(source, e);
        } catch (InvocationTargetException e) {
            handleDefaultValueError(source, e);
        } catch (SecurityException e) {
            handleDefaultValueError(source, e);
        }
        return null;
    }

    private Method getDefaultValueMethod() {
        if (defaultValueMethod == null) {
            defaultValueMethod = findDefaultValueMethod();
        }
        return defaultValueMethod;
    }

    @Override
    public PolicyModel getModelType() {
        return (PolicyModel)super.getModelType();
    }

    private Method findDefaultValueMethod() {
        if (!getModelType().isConfiguredByPolicyCmptType()) {
            throw new IllegalStateException("Trying to find default value method in product class, but policy class "
                    + getModelType().getJavaClass() + " is not configurable.");
        }
        ProductModel productModel = (ProductModel)getModelType().getProductCmptType();
        AnnotatedElementMatcher<IpsDefaultValue> filter = new AnnotatedElementMatcher<IpsDefaultValue>() {
            @Override
            public boolean matches(IpsDefaultValue ann) {
                return ann.value().equals(getName());
            }
        };
        Method method = productModel.searchDeclaredMethod(IpsDefaultValue.class, filter);
        if (method == null) {
            throw new IllegalStateException("No method found for retrieving the default value of attribute: "
                    + getName());
        }
        return method;
    }

    private void handleDefaultValueError(Object source, Exception e) {
        handleGetterError(source, e, "default value");
    }

    // /**
    // *
    // * Returns the value set of the given model object's attribute identified by this model type
    // * attribute.
    // *
    // * @param source the model object to read an attribute value set from. Must correspond to the
    // * {@link IModelType} this attribute belongs to.
    // */
    // public ValueSet<?> getValueSet(IProductComponent source, Calendar effectiveDate,
    // IValidationContext context) {
    // // try {
    // // return (ValueSet<?>)valueSetMethod.invoke(source, context);
    // // } catch (IllegalAccessException e) {
    // // handleValueSetError(source, e);
    // // } catch (InvocationTargetException e) {
    // // handleValueSetError(source, e);
    // // } catch (SecurityException e) {
    // // handleValueSetError(source, e);
    // // }
    // return null;
    // }
    //
    // private void handleValueSetError(Object source, Exception e) {
    // handleGetterError(source, e, "value set");
    // }

}
