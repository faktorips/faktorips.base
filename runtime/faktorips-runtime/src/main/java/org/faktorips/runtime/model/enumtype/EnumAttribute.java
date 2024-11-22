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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.IpsEnumAttribute;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.type.Deprecation;
import org.faktorips.runtime.model.type.Documentation;
import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.read.SimpleTypePartsReader;
import org.faktorips.runtime.util.MessagesHelper;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.LocalizedString;
import org.faktorips.values.ObjectUtil;

/**
 * Description of an attribute of an {@link EnumType}.
 */
public class EnumAttribute extends ModelElement {

    public static final String MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY = "ENUM_ATTRIBUTE-MANDATORY_ATTRIBUTE_IS_EMPTY";
    public static final String MSGCODE_NOT_UNIQUE = "ENUM_ATTRIBUTE-NOT_UNIQUE";
    protected static final String MSGKEY_MANDATORY_ATTRIBUTE_IS_EMPTY = "Validation.MandatoryAttributeIsEmpty";
    protected static final String MSGKEY_MANDATORY_MULTILINGUAL_ATTRIBUTE_IS_EMPTY = "Validation.MandatoryMultilingualAttributeIsEmpty";
    protected static final String MSGKEY_NOT_UNIQUE = "Validation.AttributeNotUnique";

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
     * Whether this attribute's values are mandatory (no empty/{@code null} values allowed).
     */
    public boolean isMandatory() {
        return annotation.mandatory();
    }

    /**
     * Returns the value for this attribute from the enum value. If the attribute
     * {@linkplain #isMultilingual() is multilingual}, the {@linkplain Locale#getDefault() default
     * Locale} is used.
     *
     * @see EnumAttribute#getValue(Object, Locale) for getting a multilingual value for a specific
     *          locale
     */
    public Object getValue(Object enumValue) {
        return getValue(enumValue, Locale.getDefault());
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
    public Object getValue(Object enumValue, Locale locale) {
        try {
            if (isMultilingual()) {
                return getter.invoke(enumValue, locale);
            } else {
                return getter.invoke(enumValue);
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

    /**
     * Validates this enum attribute's configuration in the given enum value against the model. Only
     * properties inherent to an enum value (like {@link #isMandatory() mandatory}) are validated
     * here, properties that can only be validated in context with other enum values like
     * {@link #isUnique() unique} are validated in
     * {@link EnumType#validate(MessageList, IValidationContext, List)} (which also calls this
     * validation for every enum value).
     *
     * @see EnumType#validate(MessageList, IValidationContext, List)
     *
     * @param list a {@link MessageList}, to which validation messages may be added
     * @param context the {@link IValidationContext}, needed to determine the {@link Locale} in
     *            which to create {@link Message Messages}
     * @param enumValue the enum value instance to validate
     * @since 25.1
     */
    public void validate(MessageList list,
            IValidationContext context,
            Object enumValue) {
        requireNonNull(list, "list must not be null");
        requireNonNull(context, "context must not be null");
        requireNonNull(enumValue, "enumValue must not be null");
        if (enumType.isExtensible() && isFromExtension(enumValue)) {
            validateMandatory(list, context, enumValue);
        }
    }

    private void validateMandatory(MessageList list, IValidationContext context, Object enumValue) {
        if (isMandatory()) {
            if (isMultilingual()) {
                Collection<LocalizedString> localizedStrings = getLocalizedStrings(enumValue);
                localizedStrings.forEach(l -> {
                    String value = l.getValue();
                    if (ObjectUtil.isNull(value) || value.isEmpty()) {
                        addErrorMessage(list, context, MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY,
                                MSGKEY_MANDATORY_MULTILINGUAL_ATTRIBUTE_IS_EMPTY, enumValue,
                                getLabel(context.getLocale()), l.getLocale().getDisplayName(context.getLocale()),
                                enumValue);
                    }
                });
                if (localizedStrings.isEmpty()) {
                    addErrorMessage(list, context, MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY,
                            MSGKEY_MANDATORY_ATTRIBUTE_IS_EMPTY, enumValue, getLabel(context.getLocale()),
                            enumValue);
                }
            } else {
                Object value = getValue(enumValue);
                if (ObjectUtil.isNull(value) || value instanceof String s && s.isEmpty()) {
                    addErrorMessage(list, context, MSGCODE_MANDATORY_ATTRIBUTE_IS_EMPTY,
                            MSGKEY_MANDATORY_ATTRIBUTE_IS_EMPTY, enumValue, getLabel(context.getLocale()), enumValue);
                }
            }
        }
    }

    void addErrorMessage(MessageList list,
            IValidationContext context,
            String msgCode,
            String msgKey,
            Object enumValue,
            Object... replacementValues) {
        Locale locale = context.getLocale();
        ResourceBundle messages = getResourceBundle(locale);
        String text = String.format(messages.getString(msgKey), replacementValues);
        Message message = Message.error(text).code(msgCode).invalidObjectWithProperties(enumValue, getName()).create();
        list.add(message);
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle(EnumAttribute.class.getName(), locale);
    }

    private Collection<LocalizedString> getLocalizedStrings(Object enumValue) {
        try {
            Field field = enumType.getEnumClass().getDeclaredField(IpsStringUtils.toLowerFirstChar(getName()));
            field.setAccessible(true);
            if (field.get(enumValue) instanceof DefaultInternationalString defaultInternationalString) {
                return defaultInternationalString.getLocalizedStrings();
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(
                    "Can't validate multilingual attribute " + enumType + "." + this
                            + " because accessing the internal field failed",
                    e);
        }
        return List.of();
    }

    private boolean isFromExtension(Object enumValue) {
        // TODO FIPS-12259 Use EnumType#getValuesFromType instead?
        try {
            Field field = enumType.getEnumClass().getDeclaredField("productRepository");
            field.setAccessible(true);
            return field.get(enumValue) != null;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(
                    "Can't validate multilingual attribute " + enumType + "." + this
                            + " because accessing the internal field failed",
                    e);
        }
    }

}
