/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testMethod() {
        String oldName = "test";
        assertEquals(Messages.StringUtils_copyOfNamePrefix + oldName, StringUtils.computeCopyOfName(0, oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName,
                StringUtils.computeCopyOfName(1, Messages.StringUtils_copyOfNamePrefix + oldName));
        assertEquals(Messages.StringUtils_copyOfNamePrefix + "(3)_" + oldName,
                StringUtils.computeCopyOfName(2, Messages.StringUtils_copyOfNamePrefix + "(2)_" + oldName));
    }

}
