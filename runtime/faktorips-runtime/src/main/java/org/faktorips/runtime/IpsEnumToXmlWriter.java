/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import org.faktorips.runtime.internal.AbstractCachingRuntimeRepository;
import org.faktorips.runtime.internal.DescriptionXmlHelper;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.enumtype.EnumType;
import org.faktorips.runtime.xml.IToXmlSupport;
import org.faktorips.values.InternationalString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used in generated code for enums to write their values to XML when the generator
 * setting "Generate toXml Support" is activated.
 */
public class IpsEnumToXmlWriter {

    public static final String XML_ELEMENT_ENUMATTRIBUTEVALUE = "EnumAttributeValue";

    private static final String XML_ELEMENT_ENUMVALUE = "EnumValue";
    private static final String XML_ELEMENT_ENUMCONTENT = "EnumContent";
    private static final String XML_ELEMENT_ENUMATTRIBUTEREFERENCE = "EnumAttributeReference";
    private static final String XML_ATTRIBUTE_ENUM_TYPE = "enumType";
    private static final String XML_ATTRIBUTE_NAME = "name";

    private final IRuntimeRepository repository;
    private final Class<?> enumClass;
    private List<?> enumValues;

    /**
     * To save an extensible enum to XML, all instances need to be loaded from the
     * {@link IRuntimeRepository} therefore we require <strong>non</strong>-{@code null} parameters.
     *
     * @param repository the repository
     * @param enumClass the enum class
     */
    public IpsEnumToXmlWriter(IRuntimeRepository repository, Class<?> enumClass) {
        this(repository, enumClass, repository.getEnumValues(enumClass));
    }

    /**
     * To save an extensible enum to XML, all instances need to be loaded from the
     * {@link IRuntimeRepository} therefore we require <strong>non</strong>-{@code null} parameters.
     *
     * @param repository the repository
     * @param enumClass the enum class
     * @param enumValues the value of the enum type as list
     */
    public IpsEnumToXmlWriter(IRuntimeRepository repository, Class<?> enumClass, List<?> enumValues) {
        this.repository = Objects.requireNonNull(repository);
        this.enumClass = Objects.requireNonNull(enumClass);
        this.enumValues = Objects.requireNonNull(enumValues);
    }

    /**
     * Creates an XML {@link Element} that represents this enums data.
     *
     * @param document a document, that can be used to create XML elements.
     * @throws UnsupportedOperationException if the support for toXml ("Generate toXml Support") is
     *             not activated in the Faktor-IPS standard builder.
     */
    public Element toXml(Document document) {
        IToXmlSupport.check(enumClass);
        Element enumContentElement = document.createElement(XML_ELEMENT_ENUMCONTENT);
        writeDescriptionToXml(enumContentElement, false);
        writeValuesToXml(enumContentElement);
        writeHeaderToXml(enumContentElement);
        return enumContentElement;
    }

    private void writeValuesToXml(Element element) {
        for (Object enumValue : enumValues) {
            if (isModelValue(enumValue)) {
                continue;
            }
            Element enumValueElement = element.getOwnerDocument().createElement(XML_ELEMENT_ENUMVALUE);
            // this is only to match the runtime to the design time, a description can not be added
            // in the UI at this point
            writeDescriptionToXml(enumValueElement, true);
            ((IToXmlSupport)enumValue).writePropertiesToXml(enumValueElement);
            element.appendChild(enumValueElement);
        }
    }

    private boolean isModelValue(Object enumValue) {
        try {
            Field valuesField;
            try {
                valuesField = enumValue.getClass().getField("VALUES");
            } catch (NoSuchFieldException e) {
                return false;
            }
            List<?> values = (List<?>)valuesField.get(enumValue);
            return values.contains(enumValue);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(
                    "Can't determine whether " + enumValue + " is an extension value of " + enumValue.getClass(), e);
        }
    }

    private void writeHeaderToXml(Element element) {
        EnumType enumType = IpsModel.getEnumType(enumClass);
        element.setAttribute(XML_ATTRIBUTE_ENUM_TYPE, enumType.getEnumContentQualifiedName());
        for (String name : enumType.getAttributenames()) {
            Element refElement = element.getOwnerDocument().createElement(XML_ELEMENT_ENUMATTRIBUTEREFERENCE);
            refElement.setAttribute(XML_ATTRIBUTE_NAME, name);
            element.appendChild(refElement);
        }
    }

    /**
     * Writes the {@link InternationalString} to XML. With the boolean parameter
     * {@code skipTextContent} {@code true} no text content will be written. This is done to match
     * the design time XML to the runtime XML.
     *
     * @param element the element
     * @param skipTextContent whether to skip the text content {@code true}, or to write it
     *            {@code false}
     */
    private void writeDescriptionToXml(Element element, boolean skipTextContent) {
        if (repository instanceof AbstractCachingRuntimeRepository) {
            AbstractCachingRuntimeRepository runtimeRepository = (AbstractCachingRuntimeRepository)repository;
            InternationalString enumDescription = runtimeRepository.getEnumDescription(enumClass);
            DescriptionXmlHelper.write(enumDescription, element, skipTextContent);
        }
    }
}
