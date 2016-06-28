/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.enumtype;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import org.faktorips.runtime.model.annotation.IpsEnumDisplayName;
import org.faktorips.runtime.model.annotation.IpsEnumIdentifier;
import org.faktorips.runtime.model.annotation.IpsEnumUnique;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.modeltype.internal.AbstractModelElement;
import org.faktorips.runtime.modeltype.internal.DocumentationType;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Description of an attribute of an {@link EnumModel}.
 */
public class EnumAttributeModel extends AbstractModelElement {

    private final EnumModel enumModel;

    private final Class<?> datatype;

    private final Method getterMethod;

    public EnumAttributeModel(EnumModel enumModel, String name, Method getterMethod) {
        super(name, getterMethod.getAnnotation(IpsExtensionProperties.class));
        this.enumModel = enumModel;
        this.datatype = getterMethod.getReturnType();
        this.getterMethod = getterMethod;
    }

    /**
     * The class for this attribute's values.
     */
    public Class<?> getDatatype() {
        return datatype;
    }

    /**
     * Whether this attribute's value is unique over all the enum's values.
     */
    public boolean isUnique() {
        return getterMethod.isAnnotationPresent(IpsEnumUnique.class);
    }

    /**
     * Whether this attribute is used to identify an enum value.
     */
    public boolean isId() {
        return getterMethod.isAnnotationPresent(IpsEnumIdentifier.class);
    }

    /**
     * Whether this attribute is used to display an enum value for human readability.
     */
    public boolean isDisplayName() {
        return getterMethod.isAnnotationPresent(IpsEnumDisplayName.class);
    }

    /**
     * Returns the value for this attribute from the enum value. If the attribute
     * {@linkplain #isMultilingual() is multilingual}, the {@linkplain Locale#getDefault() default
     * Locale} is used.
     * 
     * @see EnumAttributeModel#getValue(Object, Locale) for getting a multilingual value for a
     *      specific locale
     */
    public Object getValue(Object enumInstance) {
        return getValue(enumInstance, Locale.getDefault());
    }

    /**
     * Whether the values of this attribute are dependent on {@link Locale}.
     */
    public boolean isMultilingual() {
        return getterMethod.getParameterTypes().length == 1;
    }

    /**
     * Returns the value for this attribute from the enum value. If the attribute
     * {@linkplain #isMultilingual() is multilingual}, the given locale is used, otherwise it is
     * ignored.
     * 
     * @see EnumAttributeModel#getValue(Object) for getting a locale independent value
     */
    public Object getValue(Object enumInstance, Locale locale) {
        try {
            if (isMultilingual()) {
                return getterMethod.invoke(enumInstance, locale);
            } else {
                return getterMethod.invoke(enumInstance);
            }
        } catch (IllegalAccessException e) {
            throw cantGetValueException(e, locale);
        } catch (IllegalArgumentException e) {
            throw cantGetValueException(e, locale);
        } catch (InvocationTargetException e) {
            throw cantGetValueException(e, locale);
        }
    }

    private RuntimeException cantGetValueException(Exception e, Locale locale) {
        return new RuntimeException("Can't get value for attribute \"" + getName() + "\""
                + (isMultilingual() ? " for locale " + locale : ""), e);
    }

    @Override
    protected String getMessageKey(DocumentationType messageType) {
        return messageType.getKey(enumModel.getName(), EnumModel.KIND_NAME, getName());
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return enumModel.getMessageHelper();
    }
}
