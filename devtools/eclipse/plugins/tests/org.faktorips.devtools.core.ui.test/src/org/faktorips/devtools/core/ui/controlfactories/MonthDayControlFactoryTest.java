/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.MonthDayDatatype;
import org.junit.Test;

public class MonthDayControlFactoryTest {

    @Test
    public void testIsFactoryFor() {
        MonthDayControlFactory f = new MonthDayControlFactory();
        assertFalse(f.isFactoryFor(null));
        assertFalse(f.isFactoryFor(Datatype.INTEGER));
        assertTrue(f.isFactoryFor(new MonthDayDatatype()));
    }

}
