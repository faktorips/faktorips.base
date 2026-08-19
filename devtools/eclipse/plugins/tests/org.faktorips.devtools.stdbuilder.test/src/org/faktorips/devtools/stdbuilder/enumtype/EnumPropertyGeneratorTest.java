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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.values.LocalizedString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
public class EnumPropertyGeneratorTest {

    @Mock
    private IEnumType enumType;

    @Mock
    private IEnumAttribute enumAttribute;

    @Mock
    private IEnumAttribute enumAttribute2;

    @Mock
    private IEnumValue enumValue;

    @Mock
    private IEnumAttributeValue idAttributeValue;

    @Mock
    private IEnumAttributeValue enumAttributeValue;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private AProject aProject;

    @BeforeEach
    public void initEnumType() {
        List<IEnumAttribute> attributes = new ArrayList<>();
        attributes.add(enumAttribute);
        attributes.add(enumAttribute2);
        when(enumType.getEnumAttributesIncludeSupertypeCopies(false)).thenReturn(attributes);

        lenient().when(enumAttribute.getName()).thenReturn("column");

        List<IEnumValue> enumValues = new ArrayList<>();
        enumValues.add(enumValue);
        lenient().when(enumType.getEnumValues()).thenReturn(enumValues);

        lenient().when(enumValue.getEnumAttributeValue(enumAttribute)).thenReturn(enumAttributeValue);
        lenient().when(enumValue.getEnumAttributeValue(enumAttribute2)).thenReturn(idAttributeValue);

        lenient().when(enumType.findIdentiferAttribute(any(IIpsProject.class))).thenReturn(enumAttribute2);

        lenient().when(enumAttributeValue.getEnumValue()).thenReturn(enumValue);

        lenient().doReturn(ValueFactory.createStringValue("myId")).when(idAttributeValue).getValue();

        when(enumType.getIpsProject()).thenReturn(ipsProject);

        when(aProject.getDefaultLineSeparator()).thenReturn(System.lineSeparator());

        when(ipsProject.getProject()).thenReturn(aProject);
    }

    @Test
    public void testFindMultilingualAttributes_emptyList() throws Exception {
        EnumPropertyGenerator enumPropertyGenerator = new EnumPropertyGenerator(enumType, Locale.GERMAN);

        enumPropertyGenerator.findMultilingualAttributes();

        List<IEnumAttribute> multilingualAttributes = enumPropertyGenerator.getMultilingualAttributes();
        assertTrue(multilingualAttributes.isEmpty());
    }

    @Test
    public void testFindMultilingualAttributes_oneResult() throws Exception {
        EnumPropertyGenerator enumPropertyGenerator = new EnumPropertyGenerator(enumType, Locale.GERMAN);
        when(enumAttribute.isMultilingual()).thenReturn(true);

        enumPropertyGenerator.findMultilingualAttributes();

        List<IEnumAttribute> multilingualAttributes = enumPropertyGenerator.getMultilingualAttributes();
        assertThat(multilingualAttributes, hasItem(enumAttribute));
        assertEquals(1, multilingualAttributes.size());
    }

    @Test
    public void testFindMultilingualAttributes_twoResult() throws Exception {
        EnumPropertyGenerator enumPropertyGenerator = new EnumPropertyGenerator(enumType, Locale.GERMAN);
        when(enumAttribute.isMultilingual()).thenReturn(true);
        when(enumAttribute2.isMultilingual()).thenReturn(true);

        enumPropertyGenerator.findMultilingualAttributes();

        List<IEnumAttribute> multilingualAttributes = enumPropertyGenerator.getMultilingualAttributes();
        assertThat(multilingualAttributes, hasItem(enumAttribute));
        assertThat(multilingualAttributes, hasItem(enumAttribute2));
        assertEquals(2, multilingualAttributes.size());
    }

    @Test
    public void testGeneratePropertyFile_doNothingForNoMultilingualAttributes() throws Exception {
        EnumPropertyGenerator enumPropertyGenerator = new EnumPropertyGenerator(enumType, Locale.GERMAN);

        enumPropertyGenerator.generatePropertyFile();

        assertFalse(enumPropertyGenerator.getMessagesProperties().isModified());
    }

    @Test
    public void testGeneratePropertyFile_foundIdAttribute() throws Exception {
        EnumPropertyGenerator enumPropertyGenerator = new EnumPropertyGenerator(enumType, Locale.GERMAN);

        enumPropertyGenerator.generatePropertyFile();

        assertEquals(enumAttribute2, enumPropertyGenerator.getIdentifierAttribute());
    }

    @Test
    public void testGeneratePropertyFile() throws Exception {
        when(enumAttribute.isMultilingual()).thenReturn(true);
        @SuppressWarnings("unchecked")
        // we know it is safe
        IValue<IInternationalString> internationalStringValue = (IValue<IInternationalString>)ValueFactory.createValue(
                true, null);
        internationalStringValue.getContent().add(new LocalizedString(Locale.GERMAN, "myTestValue"));
        doReturn(internationalStringValue).when(enumAttributeValue).getValue();
        EnumPropertyGenerator enumPropertyGenerator = new EnumPropertyGenerator(enumType, Locale.GERMAN);

        enumPropertyGenerator.generatePropertyFile();

        assertTrue(enumPropertyGenerator.getMessagesProperties().isModified());
        assertEquals("myTestValue", enumPropertyGenerator.getMessagesProperties().getMessage("column_myId"));
    }

}
