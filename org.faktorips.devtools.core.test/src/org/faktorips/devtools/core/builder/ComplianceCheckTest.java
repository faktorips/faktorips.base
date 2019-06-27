/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
