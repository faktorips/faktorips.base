/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.ipshierarchy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.type.TypeHierarchy;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.IType;
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
    public void testHasChildren() throws CoreException {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        assertTrue(a.hasChildren(supersupertype));
        assertTrue(a.hasChildren(supertype));
        assertFalse(a.hasChildren(pcType));
    }

    @Test
    public void testGetParent() throws CoreException {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        IType c = (IType)a.getParent(supertype);
        assertTrue(c.getName().equals("Supersupertype"));
        IType b = (IType)a.getParent(pcType);
        assertTrue(b.getName().equals("Supertype"));
    }

    @Test
    public void testGetChildren() throws CoreException {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        Object[] b = a.getChildren(supersupertype);
        assertTrue(((IType)b[0]).getName().equals("Supertype"));
        Object[] c = a.getChildren(supertype);
        assertTrue(((IType)c[0]).getName().equals("TestPolicy"));
    }

    @Test
    public void testGetElements() throws CoreException {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        Object[] b = a.getElements(pcType);
        assertTrue(((IType)b[0]).getName().equals("Supersupertype"));
    }
}
