package org.faktorips.codegen.dthelpers;

import static org.junit.Assert.assertEquals;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.ArrayOfValueDatatype;
import org.faktorips.datatype.Datatype;
import org.junit.Test;

public class ArrayOfValueDatatypeHelperTest {

    @Test
    public void testGetJavaClassName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        ArrayOfValueDatatypeHelper helper = new ArrayOfValueDatatypeHelper(datatype, DatatypeHelper.MONEY);
        assertEquals(DatatypeHelper.MONEY.getJavaClassName() + "[][]", helper.getJavaClassName()); //$NON-NLS-1$
    }

}
