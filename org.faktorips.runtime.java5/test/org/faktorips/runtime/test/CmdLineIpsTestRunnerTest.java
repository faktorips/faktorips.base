/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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
        CmdLineIpsTestRunner.main(new String[]{"{org/faktorips/runtime/testrepository/faktorips-repository-toc.xml}", "{test}"});
    }
}
