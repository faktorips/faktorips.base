/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import static org.junit.Assert.assertEquals;

import org.faktorips.devtools.core.model.ipsobject.Modifier;
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
