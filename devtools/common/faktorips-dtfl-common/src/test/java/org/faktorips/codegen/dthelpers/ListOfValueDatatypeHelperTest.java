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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.PrimitiveDatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
        assertEquals("new ArrayList<>(xxx)", listOfValueDatatypeHelper.newInstance("xxx").getSourcecode()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testNewValueInstance() {
        when(elementDatatypeHelper.newInstance("foo")).thenReturn(new JavaCodeFragment("Foo.BAR"));
        assertEquals("Foo.BAR", listOfValueDatatypeHelper.newValueInstance("foo").getSourcecode()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testValueOfExpression() {
        assertEquals("new ArrayList<>(test)", listOfValueDatatypeHelper.valueOfExpression("test") //$NON-NLS-1$ //$NON-NLS-2$
                .getSourcecode());
    }

    @Test
    public void testValueOfExpressionPrimitiveElementDatatype() {
        when(elementDatatype.isPrimitive()).thenReturn(true);
        when(primitiveElementDatatypeHelper.getDatatype()).thenReturn(elementDatatype);
        when(primitiveElementDatatypeHelper.getWrapperTypeHelper()).thenReturn(DatatypeHelper.INTEGER);
        listOfValueDatatypeHelper = new ListOfValueDatatypeHelper(primitiveElementDatatypeHelper);

        assertEquals("new ArrayList<>(test)", listOfValueDatatypeHelper.valueOfExpression("test") //$NON-NLS-1$ //$NON-NLS-2$
                .getSourcecode());
    }

    @Test
    public void testCreateCastExpression_WithValue() {
        when(elementDatatypeHelper.newInstance("foo")).thenReturn(new JavaCodeFragment("Foo.BAR"));
        assertEquals("Foo.BAR", listOfValueDatatypeHelper.createCastExpression("foo").getSourcecode()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testCreateCastExpression_WithNullValue() {
        when(elementDatatype.hasNullObject()).thenReturn(true);
        when(elementDatatypeHelper.newInstance("")).thenReturn(new JavaCodeFragment("Foo.NULL"));
        assertEquals("Foo.NULL", listOfValueDatatypeHelper.createCastExpression("").getSourcecode()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testCreateCastExpression_WithNull() {
        when(elementDatatype.hasNullObject()).thenReturn(false);
        when(elementDatatypeHelper.newInstance("")).thenReturn(new JavaCodeFragment("null"));
        when(elementDatatypeHelper.getJavaClassName()).thenReturn("test.Foo");
        assertEquals("(Foo)null", listOfValueDatatypeHelper.createCastExpression("").getSourcecode()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetValueSetJavaClassName() {
        listOfValueDatatypeHelper.getValueSetJavaClassName();
        verify(elementDatatypeHelper).getValueSetJavaClassName();
    }

    @Test
    public void testGetRangeJavaClassName() {
        listOfValueDatatypeHelper.getRangeJavaClassName(true);
        verify(elementDatatypeHelper).getRangeJavaClassName(true);
    }

    @Test
    public void testNewRangeInstance() {
        JavaCodeFragment lowerBoundExp = mock(JavaCodeFragment.class);
        JavaCodeFragment upperBoundExp = mock(JavaCodeFragment.class);
        JavaCodeFragment stepExp = mock(JavaCodeFragment.class);
        JavaCodeFragment containsNullExp = mock(JavaCodeFragment.class);
        listOfValueDatatypeHelper.newRangeInstance(lowerBoundExp, upperBoundExp, stepExp, containsNullExp, true);
        verify(elementDatatypeHelper).newRangeInstance(lowerBoundExp, upperBoundExp, stepExp, containsNullExp, true);
    }
}
