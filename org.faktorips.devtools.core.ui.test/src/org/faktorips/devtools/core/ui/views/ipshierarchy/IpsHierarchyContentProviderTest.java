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

public class IpsHierarchyContentProviderTest extends AbstractIpsPluginTest {
    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IPolicyCmptType supertype;
    private IPolicyCmptType supersupertype;

    @Override
    protected void setUp() throws Exception {
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

    public void testHasChildren() throws CoreException {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        assertTrue(a.hasChildren(supersupertype));
        assertTrue(a.hasChildren(supertype));
        assertFalse(a.hasChildren(pcType));
    }

    public void testGetParent() throws CoreException {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        IType c = (IType)a.getParent(supertype);
        assertTrue(c.getName().equals("Supersupertype"));
        IType b = (IType)a.getParent(pcType);
        assertTrue(b.getName().equals("Supertype"));
    }

    public void testGetChildren() throws CoreException {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        IType[] b = (IType[])a.getChildren(supersupertype);
        assertTrue(b[0].getName().equals("Supertype"));
        IType[] c = (IType[])a.getChildren(supertype);
        assertTrue(c[0].getName().equals("TestPolicy"));
    }

    public void testGetElements() throws CoreException {
        HierarchyContentProvider a = new HierarchyContentProvider();
        a.inputChanged(null, null, TypeHierarchy.getTypeHierarchy(supertype));
        Object[] b = a.getElements(pcType);
        assertTrue(((IType)b[0]).getName().equals("Supersupertype"));
    }
}
