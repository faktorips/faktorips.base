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

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectCacheTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject();
    }
    
    public void test1() throws CoreException {
        IPolicyCmptType type0 = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
        type0.setProductCmptType("SomeType");
        allocateMemory();
        System.gc();
        Thread.yield();
        newProductCmptType(ipsProject, "Product"); // => triggers put in cache with process queue!
        IPolicyCmptType type2 = ipsProject.findPolicyCmptType("Policy");
        assertSame(type0, type2);
    }

    private void allocateMemory() throws CoreException {
        for (int i = 0; i < 100; i++) {
            newProductCmpt(ipsProject, "Test" + i);
        }
    }
}
