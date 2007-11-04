/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.model.valueset.ValueSetType;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueSetTypeTest extends TestCase {
    
    public void testGetValueSetType() {
        assertNull(ValueSetType.getValueSetType("unknown"));
        assertEquals(ValueSetType.ALL_VALUES, ValueSetType.getValueSetType(ValueSetType.ALL_VALUES.getId()));
    }

    public void testGetValueSetTypeByName() {
        assertNull(ValueSetType.getValueSetTypeByName("unknown"));
        assertEquals(ValueSetType.ALL_VALUES, ValueSetType.getValueSetTypeByName(
                ValueSetType.ALL_VALUES.getName()));
    }

    public void testGetValueSetTypes() {
        assertEquals(3, ValueSetType.getValueSetTypes().length);
    }

}
