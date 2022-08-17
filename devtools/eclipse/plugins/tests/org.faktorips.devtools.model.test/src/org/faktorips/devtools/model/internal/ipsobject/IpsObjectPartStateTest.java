/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsobject;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.junit.Before;
import org.junit.Test;

public class IpsObjectPartStateTest extends AbstractIpsPluginTest {

    IPolicyCmptTypeAttribute attribute;
    PolicyCmptType pcType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProject project = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("products.folder", true, null);

        IIpsSrcFile pdSrcFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
        attribute = pcType.newPolicyCmptTypeAttribute();
    }

    @Test
    public void testAll() {
        IpsObjectPartState state = new IpsObjectPartState(attribute);
        IpsObjectPartState stringState = new IpsObjectPartState(state.toString());

        assertEquals(1, pcType.getNumOfAttributes());
        attribute.delete();
        assertEquals(0, pcType.getNumOfAttributes());
        attribute = (IPolicyCmptTypeAttribute)state.newPart(pcType);
        assertEquals(1, pcType.getNumOfAttributes());
        attribute.delete();
        assertEquals(0, pcType.getNumOfAttributes());
        stringState.newPart(pcType);
        assertEquals(1, pcType.getNumOfAttributes());

        IMethod method = pcType.newMethod();
        state = new IpsObjectPartState(method);
        stringState = new IpsObjectPartState(state.toString());

        assertEquals(1, pcType.getNumOfMethods());
        method.delete();
        assertEquals(0, pcType.getNumOfMethods());
        method = (IMethod)state.newPart(pcType);
        assertEquals(1, pcType.getNumOfMethods());
        method.delete();
        assertEquals(0, pcType.getNumOfMethods());
        stringState.newPart(pcType);
        assertEquals(1, pcType.getNumOfMethods());
    }

}
