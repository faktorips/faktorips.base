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
