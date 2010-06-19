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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

public class LayoutStyleTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    private IPolicyCmptType type;
    private IIpsPackageFragment packOrgMotor;
    private IIpsPackageFragment packOrg;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        type = newPolicyCmptType(ipsProject, "org.motor.Policy");
        packOrgMotor = type.getIpsPackageFragment();
        packOrg = packOrgMotor.getParentIpsPackageFragment();
    }

    public void testGetParent_Flat() {
        assertEquals(root, LayoutStyle.FLAT.getParent(root.getDefaultIpsPackageFragment()));
        assertEquals(root, LayoutStyle.FLAT.getParent(packOrg));
        assertEquals(root, LayoutStyle.FLAT.getParent(packOrgMotor));
    }

    public void testGetParent_Hierarchical() {
        assertEquals(root, LayoutStyle.HIERACHICAL.getParent(root.getDefaultIpsPackageFragment()));
        assertEquals(root, LayoutStyle.HIERACHICAL.getParent(packOrg));
        assertEquals(packOrg, LayoutStyle.HIERACHICAL.getParent(packOrgMotor));
    }

}
