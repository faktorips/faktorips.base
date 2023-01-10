/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Optional;

import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.type.Deprecation;
import org.faktorips.runtime.model.type.Documentation;
import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader;
import org.faktorips.runtime.util.MessagesHelper;

/**
 * Description of an attribute of an {@link EnumType}.
 */
public class EnumAttribute extends ModelElement {

    private final EnumType enumType;

    private final Class<?> datatype;

    private final Method getter;

    private final IpsEnumAttribute annotation;

    public EnumAttribute(EnumType enumType, String name, Method getter) {
        super(name, getter.getAnnotation(IpsExtensionProperties.class), Deprecation.of(getter));
        this.enumType = enumType;
        datatype = getter.getReturnType();
        this.getter = getter;
        annotation = getter.getAnnotation(IpsEnumAttribute.class);
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
        return annotation.unique();
    }

    /**
     * Whether this attribute is used to identify an enum value.
     */
    public boolean isIdentifier() {
        return annotation.identifier();
    }

    /**
     * Whether this attribute is used to display an enum value for human readability.
     */
    public boolean isDisplayName() {
        return annotation.displayName();
    }

    /**
     * Returns the value for this attribute from the enum value. If the attribute
     * {@linkplain #isMultilingual() is multilingual}, the {@linkplain Locale#getDefault() default
     * Locale} is used.
     * 
     * @see EnumAttribute#getValue(Object, Locale) for getting a multilingual value for a specific
     *          locale
     */
    public Object getValue(Object enumInstance) {
        return getValue(enumInstance, Locale.getDefault());
    }

    /**
     * Whether the values of this attribute are dependent on {@link Locale}.
     */
    public boolean isMultilingual() {
        return getter.getParameterTypes().length == 1;
    }

    /**
     * Returns the value for this attribute from the enum value. If the attribute
     * {@linkplain #isMultilingual() is multilingual}, the given locale is used, otherwise it is
     * ignored.
     * 
     * @see EnumAttribute#getValue(Object) for getting a locale independent value
     */
    public Object getValue(Object enumInstance, Locale locale) {
        try {
            if (isMultilingual()) {
                return getter.invoke(enumInstance, locale);
            } else {
                return getter.invoke(enumInstance);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw cantGetValueException(e, locale);
        }
    }

    private RuntimeException cantGetValueException(Exception e, Locale locale) {
        return new RuntimeException("Can't get value for attribute \"" + getName() + "\""
                + (isMultilingual() ? " for locale " + locale : ""), e);
    }

    @Override
    protected String getMessageKey(DocumentationKind messageType) {
        return messageType.getKey(enumType.getName(), EnumType.KIND_NAME, getName());
    }

    @Override
    protected MessagesHelper getMessageHelper() {
        return enumType.getMessageHelper();
    }

    protected static LinkedHashMap<String, EnumAttribute> createFrom(EnumType enumType, Class<?> enumClass) {
        return new SimpleTypePartsReader<>(
                IpsEnumType.class,
                IpsEnumType::attributeNames,
                IpsEnumAttribute.class,
                IpsEnumAttribute::name,
                (modelType, name, getterMethod) -> new EnumAttribute((EnumType)modelType, name, getterMethod))
                        .createParts(enumClass, enumType);
    }

    public Optional<EnumAttribute> findSuperEnumAttribute() {
        return enumType.findSuperEnumType().map(s -> s.getAttribute(getName()));
    }

    @Override
    protected String getDocumentation(Locale locale, DocumentationKind type, String fallback) {
        return Documentation.of(this, type, locale, fallback, this::findSuperEnumAttribute);
    }

}
