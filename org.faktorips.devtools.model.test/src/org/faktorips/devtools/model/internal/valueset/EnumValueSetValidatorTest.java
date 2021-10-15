/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.valueset;

import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.runtime.MessageList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumValueSetValidatorTest {

    private static final String ANY_VALUE = "anyValue";

    @Mock
    private ValueDatatype datatype;

    @Mock
    private EnumValueSet enumValueSet;

    @Mock
    private IValueSetOwner owner;

    @Test
    public void testValidate_MSGCODE_NULL_NOT_SUPPORTED() throws Exception {
        when(datatype.isPrimitive()).thenReturn(true);
        when(enumValueSet.isContainsNull()).thenReturn(true);

        EnumValueSetValidator enumValueSetValidator = new EnumValueSetValidator(enumValueSet, owner, datatype);
        MessageList messageList = enumValueSetValidator.validate();

        assertThat(messageList, hasMessageCode(IEnumValueSet.MSGCODE_NULL_NOT_SUPPORTED));
        assertThat(messageList.getMessageByCode(IEnumValueSet.MSGCODE_NULL_NOT_SUPPORTED),
                hasInvalidObject(enumValueSet));
    }

    @Test
    public void testValidate_MSGCODE_UNKNOWN_DATATYPE() throws Exception {
        when(enumValueSet.findValueDatatype(any(IIpsProject.class))).thenReturn(datatype);
        when(datatype.isPrimitive()).thenReturn(true);
        when(enumValueSet.isContainsNull()).thenReturn(true);

        EnumValueSetValidator enumValueSetValidator = new EnumValueSetValidator(enumValueSet, owner, null);
        MessageList messageList = enumValueSetValidator.validate();

        assertThat(messageList, hasMessageCode(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE));
        assertThat(messageList.getMessageByCode(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE),
                hasInvalidObject(owner));
    }

    @Test
    public void testValidate_MSGCODE_VALUE_NOT_PARSABLE() throws Exception {
        when(enumValueSet.size()).thenReturn(1);
        when(enumValueSet.getValue(0)).thenReturn(ANY_VALUE);

        EnumValueSetValidator enumValueSetValidator = new EnumValueSetValidator(enumValueSet, owner, datatype);
        MessageList messageList = enumValueSetValidator.validate();

        assertThat(messageList,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
        assertThat(
                messageList.getMessageByCode(
                        IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE),
                hasInvalidObject(enumValueSet));
    }
}
