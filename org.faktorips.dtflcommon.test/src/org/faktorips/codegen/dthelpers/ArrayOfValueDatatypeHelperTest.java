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
