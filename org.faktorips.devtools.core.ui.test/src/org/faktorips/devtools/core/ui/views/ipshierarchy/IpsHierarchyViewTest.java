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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.type.TypeHierarchy;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

public class IpsHierarchyViewTest extends AbstractIpsPluginTest {
    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IPolicyCmptType supertype;
    private IPolicyCmptType supersupertype;
    private IpsHierarchyView test;
    private IpsSrcFilesChangedEvent event;
    public int eventNr;
    private ProductCmptType productType;
    private IIpsProject pdProject2;
    private IIpsPackageFragmentRoot pdRootFolder2;
    private IIpsPackageFragment pdFolder2;
    private IIpsSrcFile pdSrcFile2;

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
        test = new IpsHierarchyView();
    }

    public void testSupport() {
        assertTrue(IpsHierarchyView.supports(pcType));
    }

    public void testIsNodeOfHierarchy() throws CoreException {
        // UIToolkit t = new UIToolkit(null);
        IpsHierarchyViewMock testMock = new IpsHierarchyViewMock();
        //
        // IWorkbenchPage activePage =
        // WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
        // .getActivePage();
        //
        // IViewPart hierarchyView = activePage
        // .showView(IpsHierarchyView.EXTENSION_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
        //

        Set<IIpsSrcFile> ipsSrcFiles = new HashSet<IIpsSrcFile>();
        ipsSrcFiles.add(pcType.getIpsSrcFile());
        testMock.isNodeOfHierarchy(ipsSrcFiles, TypeHierarchy.getTypeHierarchy(pcType));
        assertEquals(pcType, testMock.element);

        // IIpsElement iipsElement = null;
        // IpsSrcFilesChangedEvent event1 = getTriggeredEvent();
        // Set<IIpsSrcFile> a = event1.getChangedIpsSrcFiles();
        //
        // List<String> expectedFiles = new ArrayList<String>(3);
        //
        //
        // expectedFiles.add("L/TestProject/productdef/products/folder/Supersupertype.ipspolicycmpttype");
        // //
        // expectedFiles.add("L/TestProject/productdef/products/folder/Supertype.ipspolicycmpttype");
        // //
        // //
        // expectedFiles.add("L/TestProject/productdef/products/folder/TestPolicy.ipspolicycmpttype");
        //
        // boolean[] expectedFilesFound = new boolean[3];
        //
        // for (IFile aFile : a) {
        // for (int i = 0; i < 3; i++) {
        // if (expectedFiles.get(i).equals(aFile.toString())) {
        // expectedFilesFound[i] = true;
        // }
        // }
        // }
        //
        // assertTrue(expectedFilesFound[0]);
        // assertTrue(expectedFilesFound[1]);
        // assertTrue(expectedFilesFound[2]);
        //
        // for (IFile b : a) {
        // iipsElement = IpsPlugin.getDefault().getIpsModel().findIpsElement(b);
        // assertTrue(iipsElement instanceof IpsSrcFile);
        // }
        // }
        //

    }

    private class IpsHierarchyViewMock extends IpsHierarchyView {

        private IIpsObject element;

        @Override
        public void showHierarchy(IIpsObject element) {
            this.element = element;
        }

    }
}
