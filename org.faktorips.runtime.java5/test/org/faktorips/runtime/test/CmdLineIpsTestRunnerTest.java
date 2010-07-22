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

package org.faktorips.runtime.test;

import junit.framework.TestCase;

/**
 * 
 * @author Joerg Ortmann
 */
public class CmdLineIpsTestRunnerTest extends TestCase {

    /*
     * Test method for 'org.faktorips.runtime.test.CmdLineIpsTestRunner.main(String[])'
     */
    public void testMain() {
        CmdLineIpsTestRunner.main(new String[] { "{org/faktorips/runtime/testrepository/faktorips-repository-toc.xml}",
                "{test}" });
    }
}
