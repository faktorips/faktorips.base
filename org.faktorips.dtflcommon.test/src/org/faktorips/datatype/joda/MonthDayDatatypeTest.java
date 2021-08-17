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

public class MonthDayDatatypeTest {

    @Test
    public void testIsParsable() {
        MonthDayDatatype d = new MonthDayDatatype();

        assertTrue(d.isParsable(null));
        assertTrue(d.isParsable(""));

        assertFalse(d.isParsable("bla"));

        assertFalse(d.isParsable("15.04."));
        assertFalse(d.isParsable("04/15"));

        assertTrue(d.isParsable("--04-15"));
    }

}
