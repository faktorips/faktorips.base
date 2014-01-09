/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueSetTypeTest {

    @Test
    public void testGetValueSetType() {
        assertNull(ValueSetType.getValueSetType("unknown"));
        assertEquals(ValueSetType.UNRESTRICTED, ValueSetType.getValueSetType(ValueSetType.UNRESTRICTED.getId()));
    }

    @Test
    public void testGetValueSetTypeByName() {
        assertNull(ValueSetType.getValueSetTypeByName("unknown"));
        assertEquals(ValueSetType.UNRESTRICTED, ValueSetType.getValueSetTypeByName(ValueSetType.UNRESTRICTED.getName()));
    }

    @Test
    public void testGetValueSetTypes() {
        assertEquals(3, ValueSetType.getValueSetTypes().length);
    }

}
