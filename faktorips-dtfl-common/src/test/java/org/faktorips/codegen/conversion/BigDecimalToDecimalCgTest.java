/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class BigDecimalToDecimalCgTest extends AbstractSingleConversionCgTest {

    private BigDecimalToDecimalCg converter;

    @Before
    public void setUp() {
        converter = new BigDecimalToDecimalCg();
    }

    @Test
    public void testGetConversionCode() throws Exception {
        assertEquals("Decimal.valueOf(bigDecimal)", getConversionCode(converter, "bigDecimal")); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
