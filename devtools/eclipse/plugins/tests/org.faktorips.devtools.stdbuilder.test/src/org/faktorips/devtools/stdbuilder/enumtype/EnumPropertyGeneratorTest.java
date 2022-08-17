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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.values.LocalizedString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void initEnumType() {
        List<IEnumAttribute> attributes = new ArrayList<>();
        attributes.add(enumAttribute);
        attributes.add(enumAttribute2);
        when(enumType.getEnumAttributesIncludeSupertypeCopies(false)).thenReturn(attributes);

        when(enumAttribute.getName()).thenReturn("column");

        List<IEnumValue> enumValues = new ArrayList<>();
        enumValues.add(enumValue);
        when(enumType.getEnumValues()).thenReturn(enumValues);

        when(enumValue.getEnumAttributeValue(enumAttribute)).thenReturn(enumAttributeValue);
        when(enumValue.getEnumAttributeValue(enumAttribute2)).thenReturn(idAttributeValue);

        when(enumType.findIdentiferAttribute(any(IIpsProject.class))).thenReturn(enumAttribute2);

        when(enumAttributeValue.getEnumValue()).thenReturn(enumValue);

        doReturn(ValueFactory.createStringValue("myId")).when(idAttributeValue).getValue();
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
