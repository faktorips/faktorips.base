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

package org.faktorips.codegen.conversion;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class IntegerToBigDecimalCgTest extends AbstractSingleConversionCgTest {

    private IntegerToBigDecimalCg converter;

    @Before
    public void setUp() throws Exception {
        converter = new IntegerToBigDecimalCg();
    }

    @Test
    public void testGetConversionCode() throws Exception {
        assertEquals("BigDecimal.valueOf(integer)", getConversionCode(converter, "integer"));
    }

}
