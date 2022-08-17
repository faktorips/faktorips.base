/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.joda;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LocalTimeDatatypeTest {

    private LocalTimeDatatype datatype;

    @Test
    public void testIsParsable() {
        datatype = new LocalTimeDatatype();
        assertTrue(datatype.isParsable(null));
        assertTrue(datatype.isParsable("")); //$NON-NLS-1$
        assertTrue(datatype.isParsable("10:44:00")); //$NON-NLS-1$
        assertTrue(datatype.isParsable("23:59:59")); //$NON-NLS-1$
        assertFalse(datatype.isParsable("24:61:61")); //$NON-NLS-1$
    }
}
