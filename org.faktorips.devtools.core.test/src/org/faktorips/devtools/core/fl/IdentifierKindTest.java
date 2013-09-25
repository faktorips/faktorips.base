/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.fl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
