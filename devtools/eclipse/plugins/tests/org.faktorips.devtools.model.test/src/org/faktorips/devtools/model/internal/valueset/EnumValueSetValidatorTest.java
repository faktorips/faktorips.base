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
import static org.faktorips.testsupport.IpsMatchers.hasSeverity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsProjectPropertiesReadOnlyProxy;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IEnumValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class EnumValueSetValidatorTest {

    private static final String ANY_VALUE = "anyValue";

    @Mock
    private ValueDatatype datatype;

    @Mock
    private EnumValueSet enumValueSet;

    @Mock
    private IValueSetOwner owner;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IpsProjectPropertiesReadOnlyProxy settings;

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
    public void testValidate_MSGCODE_UNKNOWN_DATATYPE_default_setting() throws Exception {
        when(enumValueSet.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getReadOnlyProperties()).thenReturn(settings);
        when(settings.getMissingDatatypeSeverity()).thenReturn(Severity.WARNING);

        EnumValueSetValidator enumValueSetValidator = new EnumValueSetValidator(enumValueSet, owner, null);
        MessageList messageList = enumValueSetValidator.validate();

        assertThat(messageList, hasMessageCode(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE));
        Message message = messageList.getMessageByCode(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE);
        assertThat(message, hasInvalidObject(owner));
        assertThat(message, hasSeverity(Severity.WARNING));
    }

    @Test
    public void testValidate_MSGCODE_UNKNOWN_DATATYPE_error_setting() throws Exception {
        when(enumValueSet.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getReadOnlyProperties()).thenReturn(settings);
        when(settings.getMissingDatatypeSeverity()).thenReturn(Severity.ERROR);

        EnumValueSetValidator enumValueSetValidator = new EnumValueSetValidator(enumValueSet, owner, null);
        MessageList messageList = enumValueSetValidator.validate();

        assertThat(messageList, hasMessageCode(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE));
        Message message = messageList.getMessageByCode(IEnumValueSet.MSGCODE_UNKNOWN_DATATYPE);
        assertThat(message, hasInvalidObject(owner));
        assertThat(message, hasSeverity(Severity.ERROR));
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
