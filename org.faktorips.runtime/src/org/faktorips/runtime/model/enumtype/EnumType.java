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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsEnumType;
import org.faktorips.runtime.model.annotation.IpsExtensibleEnum;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.type.DocumentationKind;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.util.MessagesHelper;
import org.faktorips.runtime.util.StringBuilderJoiner;

/**
 * Description of an enum's attributes and extensibility.
 */
public class EnumType extends ModelElement {

    public static final String KIND_NAME = "EnumType";

    private final MessagesHelper messagesHelper;

    private final List<String> attributeNames;

    private final LinkedHashMap<String, EnumAttribute> attributeModels;

    private final IpsExtensibleEnum ipsExtensibleEnum;

    private final Class<?> enumTypeClass;

    public EnumType(Class<?> enumTypeClass) {
        super(enumTypeClass.getAnnotation(IpsEnumType.class).name(), enumTypeClass
                .getAnnotation(IpsExtensionProperties.class));
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
    protected MessagesHelper getMessageHelper() {
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

    @FunctionalInterface
    private static interface AttributeMatcher {
        boolean matches(EnumAttribute attributeModel);
    }

}
