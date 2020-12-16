/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.GregorianCalendar;

import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptGenerationNotFoundExceptionTest {

    @Test
    public void testToString() {
        ProductCmptGenerationNotFoundException e = new ProductCmptGenerationNotFoundException("MotorProducts",
                "MotorProduct", new GregorianCalendar(2007, 0, 1), false);
        e.toString();
    }

}
