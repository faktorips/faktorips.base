/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.dthelpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
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

    @Mock
    private DatatypeHelper elementDatatypeHelper;

    @Mock
    private PrimitiveDatatypeHelper primitiveElementDatatypeHelper;

    private ListOfValueDatatypeHelper listOfValueDatatypeHelper;

    @Before
    public void createListOfValueDatatypeHelper() throws Exception {
        when(elementDatatypeHelper.getDatatype()).thenReturn(elementDatatype);
        listOfValueDatatypeHelper = new ListOfValueDatatypeHelper(elementDatatypeHelper);
    }

    @Test
    public void testNewInstance() {
        when(elementDatatypeHelper.getJavaClassName()).thenReturn("Integer");
        assertEquals("new ArrayList<>(xxx)", listOfValueDatatypeHelper.newInstance("xxx").getSourcecode());
    }

    @Test
    public void testValueOfExpression() {
        when(elementDatatypeHelper.getJavaClassName()).thenReturn("Integer");

        assertEquals("new ArrayList<>(test)", listOfValueDatatypeHelper.valueOfExpression("test")
                .getSourcecode());
    }

    @Test
    public void testValueOfExpressionPrimitiveElementDatatype() {
        when(elementDatatype.isPrimitive()).thenReturn(true);
        when(primitiveElementDatatypeHelper.getDatatype()).thenReturn(elementDatatype);
        when(primitiveElementDatatypeHelper.getJavaClassName()).thenReturn("int");
        when(primitiveElementDatatypeHelper.getWrapperTypeHelper()).thenReturn(DatatypeHelper.INTEGER);
        listOfValueDatatypeHelper = new ListOfValueDatatypeHelper(primitiveElementDatatypeHelper);

        assertEquals("new ArrayList<>(test)", listOfValueDatatypeHelper.valueOfExpression("test")
                .getSourcecode());
    }
}
