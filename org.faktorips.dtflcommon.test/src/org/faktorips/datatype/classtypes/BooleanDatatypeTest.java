/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.datatype.classtypes;

import junit.framework.TestCase;

import org.junit.Test;

public class BooleanDatatypeTest extends TestCase {

    @Test
    public void testCompare() {
        BooleanDatatype booleanDatatype = new BooleanDatatype();

        assertEquals(Boolean.TRUE.compareTo(Boolean.FALSE), booleanDatatype.compare("true", "false"));
        assertEquals(Boolean.TRUE.compareTo(Boolean.TRUE), booleanDatatype.compare("true", "true"));
        assertEquals(Boolean.FALSE.compareTo(Boolean.FALSE), booleanDatatype.compare("false", "false"));
        assertEquals(Boolean.FALSE.compareTo(Boolean.TRUE), booleanDatatype.compare("false", "true"));
    }

}
