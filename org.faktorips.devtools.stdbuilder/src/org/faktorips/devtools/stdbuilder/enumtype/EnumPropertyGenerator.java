/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.enumtype;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.stdbuilder.MessagesProperties;

/**
 * This property generator helps the {@link EnumPropertyBuilder} to generate a concrete property
 * file. There will be one {@link EnumPropertyGenerator} for every property file that is generated
 * by the {@link EnumPropertyBuilder}. You could tell the generator to read an existing property
 * file so the generator could recognize any changes to the exiting properties.
 * 
 * @author dirmeier
 */
public class EnumPropertyGenerator {

    private final MessagesProperties messagesProperties;

    private final IEnumType enumType;

    private final Locale locale;

    private List<IEnumAttribute> multilingualAttributes;

    private IEnumAttribute identiferAttribute;

    public EnumPropertyGenerator(IEnumType enumType, Locale locale) {
        this.enumType = enumType;
        this.locale = locale;
        messagesProperties = new MessagesProperties();
    }

    public MessagesProperties getMessagesProperties() {
        return messagesProperties;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean generatePropertyFile() {
        findIdentifierAttribute();
        findMultilingualAttributes();
        if (multilingualAttributes.isEmpty()) {
            return false;
        }
        generatePropertiesForValues();
        return messagesProperties.isModified();
    }

    private void findIdentifierAttribute() {
        identiferAttribute = enumType.findIdentiferAttribute(enumType.getIpsProject());
    }

    IEnumAttribute getIdentifierAttribute() {
        return identiferAttribute;
    }

    void findMultilingualAttributes() {
        multilingualAttributes = new ArrayList<IEnumAttribute>();
        List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(false);
        for (IEnumAttribute enumAttribute : enumAttributes) {
            if (enumAttribute.isMultilingual()) {
                multilingualAttributes.add(enumAttribute);
            }
        }
    }

    List<IEnumAttribute> getMultilingualAttributes() {
        return multilingualAttributes;
    }

    void generatePropertiesForValues() {
        List<IEnumValue> enumValues = enumType.getEnumValues();
        for (IEnumValue enumValue : enumValues) {
            generatePropertiesForValue(enumValue);
        }
    }

    private void generatePropertiesForValue(IEnumValue enumValue) {
        for (IEnumAttribute enumAttribute : multilingualAttributes) {
            IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValue(enumAttribute);
            generatePropertiesForAttributeValue(enumAttributeValue, enumAttribute.getName());
        }
    }

    private void generatePropertiesForAttributeValue(IEnumAttributeValue enumAttributeValue, String attributeName) {
        IInternationalString content = (IInternationalString)enumAttributeValue.getValue().getContent();
        messagesProperties.put(attributeName + "_" + getId(enumAttributeValue), content.get(locale).getValue());
    }

    private String getId(IEnumAttributeValue enumAttributeValue) {
        IEnumAttributeValue idValue = enumAttributeValue.getEnumValue().getEnumAttributeValue(identiferAttribute);
        return idValue.getValue().getContentAsString();
    }

    public void readFromStream(InputStream stream) {
        messagesProperties.load(stream);
    }

    public InputStream getStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        messagesProperties.store(outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return inputStream;
    }
}