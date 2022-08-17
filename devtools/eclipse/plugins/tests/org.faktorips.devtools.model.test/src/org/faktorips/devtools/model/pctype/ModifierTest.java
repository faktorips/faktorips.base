/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype;

import static org.junit.Assert.assertEquals;

import org.faktorips.devtools.model.ipsobject.Modifier;
import org.junit.Test;

public class ModifierTest {

    /**
     * Tests if all values can be created.
     */
    @Test
    public void testGetEnumType() {
        assertEquals(Modifier.PUBLIC.toString(), "public");
        assertEquals(Modifier.PUBLISHED.toString(), "published");
    }
}
