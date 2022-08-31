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
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IRangeValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class RangeValueSetValidatorTest {

    private static final String ANY_VALUE = "anyValue";

    @Mock
    private ValueDatatype nonNumericDatatype;

    @Mock
    private NumericDatatype datatype;

    @Mock
    private IValueSetOwner owner;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private RangeValueSet range;

    @Mock
    private IIpsProject ipsProject;

    @InjectMocks
    private RangeValueSetValidator rangeValueSetValidator;

    @Test
    public void testValidate_useCorrectDatatype() throws Exception {
        when(datatype.isParsable(ANY_VALUE)).thenReturn(true);
        doReturn(ANY_VALUE).when(range).getLowerBound();
        doReturn(ANY_VALUE).when(range).getUpperBound();
        doReturn(ANY_VALUE).when(range).getStep();

        MessageList validatotList = new RangeValueSetValidator(range, owner, datatype).validate();

        assertThat(validatotList, lacksMessageCode(IValueSet.MSGCODE_VALUE_NOT_PARSABLE));
    }

    @Test
    public void testValidate_onlyNumerictDatatype() throws Exception {
        when(nonNumericDatatype.isParsable(ANY_VALUE)).thenReturn(true);
        doReturn(ANY_VALUE).when(range).getLowerBound();
        doReturn(ANY_VALUE).when(range).getUpperBound();
        doReturn(ANY_VALUE).when(range).getStep();

        MessageList validatotList = new RangeValueSetValidator(range, owner, nonNumericDatatype).validate();

        assertThat(validatotList, hasMessageCode(IRangeValueSet.MSGCODE_NOT_NUMERIC_DATATYPE));
    }

    @Test
    public void testValidate_useCorrectOwner() throws Exception {

        MessageList validatotList = new RangeValueSetValidator(range, owner, null).validate();
        Message messageByCode = validatotList.getMessageByCode(IRangeValueSet.MSGCODE_UNKNOWN_DATATYPE);

        assertThat(messageByCode, hasInvalidObject(owner));
    }

}
