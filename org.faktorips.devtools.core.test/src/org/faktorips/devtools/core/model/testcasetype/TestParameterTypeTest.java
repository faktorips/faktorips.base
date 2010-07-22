/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.testcasetype;

import junit.framework.TestCase;

public class TestParameterTypeTest extends TestCase {

    public void testIsTypeMatching() {
        assertTrue(TestParameterType.isTypeMatching(TestParameterType.INPUT, TestParameterType.COMBINED));
        assertTrue(TestParameterType.isTypeMatching(TestParameterType.EXPECTED_RESULT, TestParameterType.COMBINED));
        assertTrue(TestParameterType.isTypeMatching(TestParameterType.COMBINED, TestParameterType.COMBINED));

        assertTrue(TestParameterType.isTypeMatching(TestParameterType.COMBINED, TestParameterType.INPUT));
        assertTrue(TestParameterType.isTypeMatching(TestParameterType.COMBINED, TestParameterType.EXPECTED_RESULT));
    }

}
