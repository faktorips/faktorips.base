/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.internal.model.enums.EnumAttributeValue.IdentifierBoundaryValidator;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumAttributeValueUnitTest {

    private IdentifierBoundaryValidator validator;

    @Mock
    private IEnumAttributeValue attribute;
    @Mock
    private IEnumValue enumValue;
    @Mock
    private IIpsProject ipsProject;
    @Mock
    private IntegerDatatype datatype;
    @Mock
    private MessageList messageList;
    @Mock
    private IEnumAttribute identifierAttribute;
    @Mock
    private EnumType enumType;
    @Mock
    private EnumContent enumContent;

    @Before
    public void setUp() throws Exception {
        validator = new IdentifierBoundaryValidator(attribute);
        validator.withIpsProject(ipsProject);
        validator.withMessageList(messageList);
        validator.withEnumType(enumType);
        validator.withDatatype(datatype);
        when(attribute.findEnumAttribute(ipsProject)).thenReturn(identifierAttribute);
        when(enumType.getIdentifierBoundary()).thenReturn("10");

        when(datatype.compare(anyString(), anyString())).thenCallRealMethod();
        when(datatype.supportsCompare()).thenCallRealMethod();
        when(datatype.getValue(anyString())).thenCallRealMethod();

        when(enumType.findIdentiferAttribute(ipsProject)).thenReturn(identifierAttribute);
        when(identifierAttribute.findDatatype(ipsProject)).thenReturn(datatype);

        when(enumType.isIdentifierNamespaceBelowBoundary()).thenCallRealMethod();
        when(enumContent.isIdentifierNamespaceBelowBoundary()).thenCallRealMethod();

        when(attribute.getEnumValue()).thenReturn(enumValue);
        when(enumValue.getEnumValueContainer()).thenReturn(enumType);
    }

    @Test
    public void testIdentifierBoundaryValidation_cannotValidate() {
        validator.withIpsProject(null);
        validator.withMessageList(null);
        validator.withEnumType(null);
        validator.withDatatype(null);
        assertFalse(validator.canValidate());
    }

    @Test
    public void testIdentifierBoundaryValidation_canValidate() {
        assertTrue(validator.canValidate());
    }

    @Test
    public void testIdentifierBoundaryValidation_canValidate_nullDatatype() {
        validator.withDatatype(null);
        assertFalse(validator.canValidate());
    }

    @Test
    public void testIdentifierBoundaryValidation_canValidate_nullProject() {
        validator.withIpsProject(null);
        assertFalse(validator.canValidate());
    }

    @Test
    public void testIdentifierBoundaryValidation_canValidate_nullMessageList() {
        validator.withMessageList(null);
        assertFalse(validator.canValidate());
    }

    @Test
    public void testIdentifierBoundaryValidation_canValidate_valueIsNotId() {
        when(attribute.findEnumAttribute(ipsProject)).thenReturn(null);
        assertFalse(validator.canValidate());
    }

    @Test
    public void testIdentifierBoundaryValidation_canValidate_noBoundary() {
        when(enumType.getIdentifierBoundary()).thenReturn(null);
        assertFalse(validator.canValidate());
    }

    public void testIntegerDatatype() {
        assertEquals(-5, datatype.compare("5", "10"));
        assertEquals(10, datatype.compare("20", "10"));
        assertEquals(0, datatype.compare("10", "10"));
    }

    @Test
    public void testIdentifierValidForContent() {
        when(enumValue.getEnumValueContainer()).thenReturn(enumContent);
        when(attribute.getStringValue()).thenReturn("20");
        assertTrue(validator.isIdentitifierValid());
    }

    @Test
    public void testIdentifierInvalidForContent() {
        when(enumValue.getEnumValueContainer()).thenReturn(enumContent);
        when(attribute.getStringValue()).thenReturn("5");
        assertFalse(validator.isIdentitifierValid());
    }

    @Test
    public void testIdentifierValidForContent_equalToBoundary() {
        when(enumValue.getEnumValueContainer()).thenReturn(enumContent);
        when(attribute.getStringValue()).thenReturn("10");
        assertTrue(validator.isIdentitifierValid());
    }

    @Test
    public void testIdentifierValidForEnumType() {
        when(enumValue.getEnumValueContainer()).thenReturn(enumType);
        when(attribute.getStringValue()).thenReturn("5");
        assertTrue(validator.isIdentitifierValid());
    }

    @Test
    public void testIdentifierInvalidForEnumType_equalToBoundary() {
        when(enumValue.getEnumValueContainer()).thenReturn(enumType);
        when(attribute.getStringValue()).thenReturn("10");
        assertFalse(validator.isIdentitifierValid());
    }

    @Test
    public void testIdentifierInvalidForEnumType() {
        when(enumValue.getEnumValueContainer()).thenReturn(enumType);
        when(attribute.getStringValue()).thenReturn("20");
        assertFalse(validator.isIdentitifierValid());
    }

    @Test
    public void testValidateIfPossible_canValidate() {
        validator = spy(validator);
        assertTrue(validator.canValidate());

        validator.validateIfPossible();
        verify(validator).validate();
    }

    @Test
    public void testValidateIfPossible_cannotValidate() {
        validator.withEnumType(null);
        validator = spy(validator);
        assertFalse(validator.canValidate());

        validator.validateIfPossible();
        verify(validator, never()).validate();
    }
}
