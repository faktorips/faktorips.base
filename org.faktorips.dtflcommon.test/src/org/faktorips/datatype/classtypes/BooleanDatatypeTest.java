/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.datatype.classtypes;

import junit.framework.TestCase;

import org.junit.Test;

public class BooleanDatatypeTest extends TestCase {

    @Test
    public void testCompare() {
        BooleanDatatype booleanDatatype = new BooleanDatatype();

        assertEquals(Boolean.TRUE.compareTo(Boolean.FALSE), booleanDatatype.compare("true", "false"));
        assertEquals(Boolean.TRUE.compareTo(Boolean.TRUE), booleanDatatype.compare("true", "true"));
        assertEquals(Boolean.FALSE.compareTo(Boolean.FALSE), booleanDatatype.compare("false", "false"));
        assertEquals(Boolean.FALSE.compareTo(Boolean.TRUE), booleanDatatype.compare("false", "true"));
    }

}
