/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.junit.Before;
import org.junit.Test;

public class LayoutStyleTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IIpsPackageFragmentRoot root;
    private IPolicyCmptType type;
    private IIpsPackageFragment packOrgMotor;
    private IIpsPackageFragment packOrg;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        root = ipsProject.getIpsPackageFragmentRoots()[0];
        type = newPolicyCmptType(ipsProject, "org.motor.Policy");
        packOrgMotor = type.getIpsPackageFragment();
        packOrg = packOrgMotor.getParentIpsPackageFragment();
    }

    @Test
    public void testGetParent_Flat() {
        assertEquals(root, LayoutStyle.FLAT.getParent(root.getDefaultIpsPackageFragment()));
        assertEquals(root, LayoutStyle.FLAT.getParent(packOrg));
        assertEquals(root, LayoutStyle.FLAT.getParent(packOrgMotor));
    }

    @Test
    public void testGetParent_Hierarchical() {
        assertEquals(root, LayoutStyle.HIERACHICAL.getParent(root.getDefaultIpsPackageFragment()));
        assertEquals(root, LayoutStyle.HIERACHICAL.getParent(packOrg));
        assertEquals(packOrg, LayoutStyle.HIERACHICAL.getParent(packOrgMotor));
    }

}
