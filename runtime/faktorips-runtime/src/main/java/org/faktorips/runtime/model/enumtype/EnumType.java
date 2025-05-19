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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.type.Deprecation;
import org.faktorips.runtime.model.type.Documentation;
import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.util.MessagesHelper;
import org.faktorips.runtime.util.ReflectionHelper;
import org.faktorips.runtime.util.StringBuilderJoiner;
import org.faktorips.values.ListUtil;

/**
 * Description of an enum's attributes and extensibility.
 */
public class EnumType extends ModelElement {

    public static final String KIND_NAME = "EnumType";

    private static final ConcurrentHashMap<Class<?>, List<?>> ENUMVALUECACHE = new ConcurrentHashMap<>();

    private final MessagesHelper messagesHelper;

    private final List<String> attributeNames;

    private final LinkedHashMap<String, EnumAttribute> attributeModels;

    private final IpsExtensibleEnum ipsExtensibleEnum;

    private final Class<?> enumTypeClass;

    public EnumType(Class<?> enumTypeClass) {
        super(enumTypeClass.getAnnotation(IpsEnumType.class).name(), enumTypeClass
                .getAnnotation(IpsExtensionProperties.class), Deprecation.of(AnnotatedDeclaration.from(enumTypeClass)));
        IpsEnumType annotation = enumTypeClass.getAnnotation(IpsEnumType.class);
        attributeNames = Arrays.asList(annotation.attributeNames());
        attributeModels = EnumAttribute.createFrom(this, enumTypeClass);
        ipsExtensibleEnum = enumTypeClass.getAnnotation(IpsExtensibleEnum.class);
        messagesHelper = createMessageHelper(enumTypeClass.getAnnotation(IpsDocumented.class),
                enumTypeClass.getClassLoader());
        this.enumTypeClass = enumTypeClass;
    }

    /**
     * Whether the enum's values can be extended in an enum content provided in a
     * {@link IRuntimeRepository}.
     */
    public boolean isExtensible() {
        return ipsExtensibleEnum != null;
    }

    /**
     * The qualified name an enum content extending this enum must have.
     *
     * @see #isExtensible()
     */
    public String getEnumContentQualifiedName() {
        return isExtensible() ? ipsExtensibleEnum.enumContentName() : null;
    }

    /**
     * Returns models for all this enum's attributes
     */
    public List<EnumAttribute> getAttributes() {
        return new ArrayList<>(attributeModels.values());
    }

    /**
     * Returns the model for the attribute with the given name or {@code null} if no such attribute
     * exists.
     */
    public EnumAttribute getAttribute(String name) {
        return attributeModels.get(IpsStringUtils.toLowerFirstChar(name));
    }

    /**
     * Returns the names of all this enum's attributes.
     */
    public List<String> getAttributenames() {
        return attributeNames;
    }

    /**
     * The model for the attribute used to uniquely identify an instance of this enum.
     */
    public EnumAttribute getIdAttribute() {
        return findMarkedAttribute("Identifier", EnumAttribute::isIdentifier);
    }

    /**
     * The model for the attribute used to display an instance of this enum in human readable form.
     */
    public EnumAttribute getDisplayNameAttribute() {
        return findMarkedAttribute("DisplayName", EnumAttribute::isDisplayName);
    }

    /**
     * Returns the values that are defined in the type, if any. Values defined in an enum content
     * are not returned here but must be retrieved via
     * {@link IRuntimeRepository#getEnumValues(Class)}.
     *
     * @param enumClass The class of which you want to get the enumeration values
     * @return A list of instances of {@code enumClass} that are defined as enumeration values of
     *             the specified type.
     *
     * @since 25.1
     */

    @SuppressWarnings("unchecked")
    public static <T> List<T> getValuesFromType(Class<T> enumClass) {
        return Collections.unmodifiableList(
                (List<T>)ENUMVALUECACHE.computeIfAbsent(enumClass, EnumType::findEnumValuesDefinedInType));
    }

    private static <T> List<T> findEnumValuesDefinedInType(Class<T> enumClass) {
        if (enumClass.isEnum()) {
            return Arrays.asList(enumClass.getEnumConstants());
        }

        return ReflectionHelper.<List<T>> findStaticFieldValue(enumClass, "VALUES")
                .map(Collections::unmodifiableList)
                .orElseGet(List::of);
    }

    /**
     * Finds the first attribute matched by the given {@link AttributeMatcher}
     */
    private EnumAttribute findMarkedAttribute(String marker, AttributeMatcher matcher) {
        for (EnumAttribute attributeModel : attributeModels.values()) {
            if (matcher.matches(attributeModel)) {
                return attributeModel;
            }
        }
        throw new IllegalStateException("No attribute of the enum \"" + getName() + "\" is marked as " + marker);
    }

    @Override
    public MessagesHelper getMessageHelper() {
        return messagesHelper;
    }

    @Override
    protected String getMessageKey(DocumentationKind messageType) {
        return messageType.getKey(getName(), KIND_NAME, IpsStringUtils.EMPTY);
    }

    /**
     * Returns the class this enum type represents.
     */
    public Class<?> getEnumClass() {
        return enumTypeClass;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        if (isExtensible()) {
            sb.append('[');
            sb.append(getEnumContentQualifiedName());
            sb.append(']');
        }
        sb.append('(');
        StringBuilderJoiner.join(sb, attributeNames);
        sb.append(')');
        return sb.toString();
    }

    public Optional<EnumType> findSuperEnumType() {
        return Arrays.stream(getEnumClass().getInterfaces())
                .filter(IpsModel::isEnumType)
                .findFirst()
                .map(IpsModel::getEnumType);
    }

    @Override
    protected String getDocumentation(Locale locale, DocumentationKind type, String fallback) {
        return Documentation.of(this, type, locale, fallback, this::findSuperEnumType);
    }

    /**
     * Validates this enum's configuration in the given enum values against the model.
     *
     * @param list a {@link MessageList}, to which validation messages may be added
     * @param context the {@link IValidationContext}, needed to determine the {@link Locale} in
     *            which to create {@link Message Messages}
     * @param enumValues the enum value instances to validate
     *
     * @throws IllegalArgumentException if any of the given enum values does not match this enum
     *             type
     *
     * @since 25.1
     */
    public void validate(MessageList list,
            IValidationContext context,
            List<?> enumValues) {
        requireNonNull(list, "list must not be null");
        requireNonNull(context, "context must not be null");
        requireNonNull(enumValues, "enumValues must not be null");
        enumValues.forEach(enumValue -> validate(list, context, enumValue));
        getAttributes().stream()
                .filter(EnumAttribute::isUnique)
                .forEach(a -> validateUniqueValues(list, context, enumValues, a));
    }

    private void validateUniqueValues(MessageList list,
            IValidationContext context,
            List<?> enumValues,
            EnumAttribute a) {
        mapAttributeValuesToEnumValues(enumValues, a)
                .entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .forEach(e -> {
                    String allDuplicates = e.getValue().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));
                    e.getValue()
                            .forEach(v -> a.addErrorMessage(list, context, EnumAttribute.MSGCODE_NOT_UNIQUE,
                                    EnumAttribute.MSGKEY_NOT_UNIQUE, v, a.getLabel(context.getLocale()),
                                    allDuplicates));
                });
    }

    private <T> Map<Object, List<T>> mapAttributeValuesToEnumValues(List<T> enumValues, EnumAttribute enumAttribute) {
        return enumValues.stream()
                .collect(Collectors.toMap(enumAttribute::getValue, List::of, ListUtil::join));
    }

    /**
     * Validates this enum's configuration in the given enum value against the model. Only
     * properties inherent to an enum value (like {@link EnumAttribute#isMandatory() mandatory}) are
     * validated here, properties that can only be validated in context with other enum values like
     * {@link EnumAttribute#isUnique() unique} are only validated in
     * {@link EnumType#validate(MessageList, IValidationContext, List)} (which also calls this
     * validation for every enum value).
     *
     * @see #validate(MessageList, IValidationContext, List)
     * @see EnumAttribute#validate(MessageList, IValidationContext, Object)
     *
     * @param list a {@link MessageList}, to which validation messages may be added
     * @param context the {@link IValidationContext}, needed to determine the {@link Locale} in
     *            which to create {@link Message Messages}
     * @param enumValue the enum value instances to validate
     *
     * @throws IllegalArgumentException if the given enum value does not match this enum type
     *
     * @since 25.1
     */
    public void validate(MessageList list,
            IValidationContext context,
            Object enumValue) {
        requireNonNull(list, "list must not be null");
        requireNonNull(context, "context must not be null");
        requireNonNull(enumValue, "enumValues must not be null");

        if (!getEnumClass().isInstance(enumValue)) {
            throw new IllegalArgumentException(enumValue + " is not a " + this);
        }
        getAttributes().forEach(a -> a.validate(list, context, enumValue));
    }

    @FunctionalInterface
    private interface AttributeMatcher {
        boolean matches(EnumAttribute attributeModel);
    }

}
