/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.util;

import junit.framework.TestCase;

public class PersistenceUtilTest extends TestCase {

    public void testIsValidTableName() {
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier(null));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier(""));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier("    "));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier("_notValidBecauseOfTrailingSpace "));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier(" notValidBecauseOfLeadingSpace"));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier("spaces in between"));
        assertFalse(PersistenceUtil.isValidDatabaseIdentifier("2_digit_start"));

        assertTrue(PersistenceUtil.isValidDatabaseIdentifier("_TABLE"));
        assertTrue(PersistenceUtil.isValidDatabaseIdentifier("VALID_TABLE_NAME_2"));
        assertTrue(PersistenceUtil.isValidDatabaseIdentifier("_2_ALSO_VALID"));
    }

}
