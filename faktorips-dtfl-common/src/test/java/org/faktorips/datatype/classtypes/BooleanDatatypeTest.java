/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BooleanDatatypeTest {

    @Test
    public void testCompare() {
        BooleanDatatype booleanDatatype = new BooleanDatatype();

        assertEquals(Boolean.TRUE.compareTo(Boolean.FALSE), booleanDatatype.compare("true", "false")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(Boolean.TRUE.compareTo(Boolean.TRUE), booleanDatatype.compare("true", "true")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(Boolean.FALSE.compareTo(Boolean.FALSE), booleanDatatype.compare("false", "false")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(Boolean.FALSE.compareTo(Boolean.TRUE), booleanDatatype.compare("false", "true")); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
