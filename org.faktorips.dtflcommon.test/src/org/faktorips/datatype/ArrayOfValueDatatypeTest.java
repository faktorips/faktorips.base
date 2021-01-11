/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ArrayOfValueDatatypeTest {
    @Test
    public void testGetDimenion() {
        assertEquals(0, ArrayOfValueDatatype.getDimension(null));
        assertEquals(0, ArrayOfValueDatatype.getDimension("Money")); //$NON-NLS-1$
        assertEquals(1, ArrayOfValueDatatype.getDimension("Money[]")); //$NON-NLS-1$
        assertEquals(2, ArrayOfValueDatatype.getDimension("Money[][]")); //$NON-NLS-1$
    }

    @Test
    public void testGetBasicDatatypeName() {
        assertNull(ArrayOfValueDatatype.getBasicDatatypeName(null));
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money[]")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("Money", ArrayOfValueDatatype.getBasicDatatypeName("Money[][]")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testGetName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        assertEquals(datatype.getBasicDatatype().getName() + "[][]", datatype.getName()); //$NON-NLS-1$
    }

    @Test
    public void testGetQualifiedName() {
        ArrayOfValueDatatype datatype = new ArrayOfValueDatatype(Datatype.MONEY, 2);
        assertEquals(datatype.getBasicDatatype().getQualifiedName() + "[][]", datatype.getQualifiedName()); //$NON-NLS-1$
    }

}
