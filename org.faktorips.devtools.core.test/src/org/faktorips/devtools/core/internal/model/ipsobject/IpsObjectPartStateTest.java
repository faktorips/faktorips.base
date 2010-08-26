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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IMethod;

public class IpsObjectPartStateTest extends AbstractIpsPluginTest {

    IPolicyCmptTypeAttribute attribute;
    IPolicyCmptType pcType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IIpsProject project = this.newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = project.getIpsPackageFragmentRoots()[0];
        IIpsPackageFragment pack = root.createPackageFragment("products.folder", true, null);

        IIpsSrcFile pdSrcFile = pack.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();
        attribute = pcType.newPolicyCmptTypeAttribute();
    }

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
