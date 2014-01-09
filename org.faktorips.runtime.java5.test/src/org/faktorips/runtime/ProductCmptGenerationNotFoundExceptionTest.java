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
