/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.fl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.faktorips.devtools.model.fl.IdentifierKind;
import org.junit.jupiter.api.Test;

public class IdentifierKindTest {

    @Test
    public void testIsDefaultIdentifier() throws Exception {
        assertTrue(IdentifierKind.DEFAULT_IDENTIFIER.isDefaultIdentifier());
        assertFalse(IdentifierKind.ATTRIBUTE.isDefaultIdentifier());
    }

    @Test
    public void testGetDefaultIdentifierOrAttribute() throws Exception {
        assertTrue(IdentifierKind.getDefaultIdentifierOrAttribute(true).equals(IdentifierKind.DEFAULT_IDENTIFIER));
        assertTrue(IdentifierKind.getDefaultIdentifierOrAttribute(false).equals(IdentifierKind.ATTRIBUTE));
    }

}
