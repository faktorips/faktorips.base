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

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

/**
 * 
 * @author Daniel Hohenberger
 */
public class ComplianceCheckTest extends AbstractIpsPluginTest {

    @Test
    public void testIsComplianceLevelAtLeast5() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        IJavaProject javaProject = ipsProject.getJavaProject();
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.4");
        assertFalse(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject));
        javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.5");
        assertTrue(ComplianceCheck.isComplianceLevelAtLeast5(ipsProject));
    }

}
