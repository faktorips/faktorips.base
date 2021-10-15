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

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IStringLengthValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.runtime.MessageList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringLengthValueSetValidatorTest {

    @Mock
    private IValueSetOwner valueSetOwner;
    private ValueDatatype datatype = new StringDatatype();

    @Test
    public void testValidate_MSGCODE_UNKNOWN_DATATYPE() {
        StringLengthValueSet vs = new StringLengthValueSet(valueSetOwner, "partId", "A", false);

        MessageList list = vs.createValidator(valueSetOwner, null).validate();

        assertThat(list, hasMessageCode(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE));
        assertThat(list.getMessageByCode(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE), hasInvalidObject(vs));
    }

    @Test
    public void testValidate_MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE() {
        StringLengthValueSet vs = new StringLengthValueSet(valueSetOwner, "partId", "abc");

        MessageList list = vs.createValidator(valueSetOwner, datatype).validate();

        assertThat(list,
                hasMessageCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
    }

    @Test
    public void testValidate_MSGCODE_ILLEGAL_INPUT() {
        StringLengthValueSet vs = new StringLengthValueSet(valueSetOwner, "partId", "-10");

        MessageList list = vs.createValidator(valueSetOwner, datatype).validate();

        assertThat(list, hasMessageCode(IStringLengthValueSet.MSGCODE_NEGATIVE_VALUE));
    }
}
