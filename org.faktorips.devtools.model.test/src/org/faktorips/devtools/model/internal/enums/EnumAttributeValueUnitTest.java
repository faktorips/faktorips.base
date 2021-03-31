/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.AssertionFailedException;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.enums.EnumAttributeValue.IdentifierBoundaryValidator;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
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
    private IEnumAttribute identifierAttribute;
    @Mock
    private EnumType enumType;
    @Mock
    private EnumContent enumContent;

    @Before
    public void setUp() throws Exception {
        validator = new IdentifierBoundaryValidator(attribute, enumType, datatype, ipsProject);
        when(attribute.findEnumAttribute(ipsProject)).thenReturn(identifierAttribute);
        when(enumType.getIdentifierBoundary()).thenReturn("10");
        when(enumType.isValidateIdentifierBoundaryOnDatatypeNecessary(enumType.getIdentifierBoundary())).thenReturn(
                true);

        when(datatype.compare(anyString(), anyString())).thenCallRealMethod();
        when(datatype.supportsCompare()).thenCallRealMethod();
        when(datatype.getValue(anyString())).thenCallRealMethod();
        when(datatype.isParsable(anyString())).thenCallRealMethod();

        when(enumType.findIdentiferAttribute(ipsProject)).thenReturn(identifierAttribute);
        when(identifierAttribute.findDatatype(ipsProject)).thenReturn(datatype);

        when(enumType.isIdentifierNamespaceBelowBoundary()).thenCallRealMethod();
        when(enumContent.isIdentifierNamespaceBelowBoundary()).thenCallRealMethod();

        when(attribute.getEnumValue()).thenReturn(enumValue);
        when(enumValue.getEnumValueContainer()).thenReturn(enumType);
    }

    @Test(expected = AssertionFailedException.class)
    public void testConstructor() {
        validator = new IdentifierBoundaryValidator(attribute, enumType, datatype, null);
    }

    @Test
    public void testIdentifierBoundaryValidation_cannotValidate() {
        validator = new IdentifierBoundaryValidator(null, null, null, ipsProject);
        assertFalse(validator.canValidate());
    }

    @Test
    public void testIdentifierBoundaryValidation_canValidate() {
        assertTrue(validator.canValidate());
    }

    @Test
    public void testIdentifierBoundaryValidation_canValidate_nullDatatype() {
        validator = new IdentifierBoundaryValidator(attribute, enumType, null, ipsProject);
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

    @Test
    public void testIntegerDatatype() {
        assertEquals(-1, datatype.compare("5", "10"));
        assertEquals(1, datatype.compare("20", "10"));
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
        verify(validator).validateAndAppendMessages(any(MessageList.class));
    }

    @Test
    public void testValidateIfPossible_cannotValidate() {
        validator = new IdentifierBoundaryValidator(null, enumType, datatype, ipsProject);
        validator = spy(validator);
        assertFalse(validator.canValidate());

        validator.validateIfPossible();
        verify(validator, never()).validateAndAppendMessages(any(MessageList.class));
    }

    @Test
    public void testIDValueNotParsable() {
        when(attribute.getStringValue()).thenReturn("AAABBBB");
        assertFalse(validator.canValidate());
    }

    @Test
    public void testBoundaryValueNotParsable() {
        when(enumType.getIdentifierBoundary()).thenReturn("AAABBBB");
        assertFalse(validator.canValidate());
    }

}
