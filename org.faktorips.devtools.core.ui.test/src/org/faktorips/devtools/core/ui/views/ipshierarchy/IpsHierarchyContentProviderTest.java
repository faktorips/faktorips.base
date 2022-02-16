/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.ipshierarchy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.type.TypeHierarchy;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.type.IType;
import org.junit.Before;
import org.junit.Test;

public class IpsHierarchyContentProviderTest extends AbstractIpsPluginTest {
    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IPolicyCmptType supertype;
    private IPolicyCmptType supersupertype;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();

        // create two more types that act as supertype and supertype's supertype
        IIpsSrcFile file1 = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supertype", true, null);
        supertype = (PolicyCmptType)file1.getIpsObject();
        IIpsSrcFile file2 = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supersupertype", true, null);
        supersupertype = (PolicyCmptType)file2.getIpsObject();
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
    }

    @Test
    public void testHasChildren() {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        assertTrue(a.hasChildren(supersupertype));
        assertTrue(a.hasChildren(supertype));
        assertFalse(a.hasChildren(pcType));
    }

    @Test
    public void testGetParent() {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        IType c = (IType)a.getParent(supertype);
        assertTrue(c.getName().equals("Supersupertype"));
        IType b = (IType)a.getParent(pcType);
        assertTrue(b.getName().equals("Supertype"));
    }

    @Test
    public void testGetChildren() {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        Object[] b = a.getChildren(supersupertype);
        assertTrue(((IType)b[0]).getName().equals("Supertype"));
        Object[] c = a.getChildren(supertype);
        assertTrue(((IType)c[0]).getName().equals("TestPolicy"));
    }

    @Test
    public void testGetElements() {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        Object[] b = a.getElements(pcType);
        assertTrue(((IType)b[0]).getName().equals("Supersupertype"));
    }
}
