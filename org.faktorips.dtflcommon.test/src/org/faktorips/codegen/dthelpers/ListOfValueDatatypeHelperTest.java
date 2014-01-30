/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.ValueDatatype;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListOfValueDatatypeHelperTest {

    @Mock
    private ValueDatatype elementDatatype;

    private ListOfValueDatatypeHelper listOfValueDatatypeHelper;

    @Before
    public void createListOfValueDatatypeHelper() throws Exception {
        listOfValueDatatypeHelper = new ListOfValueDatatypeHelper(elementDatatype);
    }

    @Test
    public void testNewInstance() {
        when(elementDatatype.getJavaClassName()).thenReturn("Integer");
        assertEquals("new ArrayList<Integer>(xxx)", listOfValueDatatypeHelper.newInstance("xxx").getSourcecode());
    }

    @Test
    public void testValueOfExpression() {
        when(elementDatatype.getJavaClassName()).thenReturn("Integer");

        assertEquals("new ArrayList<Integer>(test)", listOfValueDatatypeHelper.valueOfExpression("test")
                .getSourcecode());
    }

    @Test
    public void testValueOfExpressionPrimitiveElementDatatype() {
        ValueDatatype wrapperDatatype = mock(ValueDatatype.class);
        when(elementDatatype.getJavaClassName()).thenReturn("int");
        when(elementDatatype.isPrimitive()).thenReturn(true);
        when(elementDatatype.getWrapperType()).thenReturn(wrapperDatatype);
        when(wrapperDatatype.getJavaClassName()).thenReturn("Integer");
        // need to recreate the helper because the datatype isPrimitive has changed
        listOfValueDatatypeHelper = new ListOfValueDatatypeHelper(elementDatatype);

        assertEquals("new ArrayList<Integer>(test)", listOfValueDatatypeHelper.valueOfExpression("test")
                .getSourcecode());
    }
}
