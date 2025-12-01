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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PrimitiveBooleanDatatypeTest {

    @Test
    public void testGetValue() {
        PrimitiveBooleanDatatype primBooleanDatatype = new PrimitiveBooleanDatatype();
        assertFalse(primBooleanDatatype.isParsable("K"));
        assertFalse(primBooleanDatatype.isParsable("TruE"));
        assertFalse(primBooleanDatatype.isParsable("False"));
        assertTrue(primBooleanDatatype.isParsable("false"));
        assertTrue(primBooleanDatatype.isParsable("true"));
    }

}
