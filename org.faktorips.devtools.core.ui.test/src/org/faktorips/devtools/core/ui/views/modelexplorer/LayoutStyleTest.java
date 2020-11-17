/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
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
