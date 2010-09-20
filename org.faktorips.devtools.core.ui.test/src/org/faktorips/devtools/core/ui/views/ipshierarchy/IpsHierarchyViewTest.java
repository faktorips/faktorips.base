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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

public class IpsHierarchyViewTest extends AbstractIpsPluginTest implements IResourceChangeListener {
    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IPolicyCmptType supertype;
    private IPolicyCmptType supersupertype;
    private IpsHierarchyView test;
    private IResourceChangeEvent event;
    public int eventNr;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        eventNr = 0;
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
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD);
    }

    public void testSupport() {
        assertTrue(IpsHierarchyView.supports(pcType));
    }

    public void testGetPath() throws InterruptedException, CoreException {
        IIpsElement iipsElement = null;
        IResourceChangeEvent event1 = getTriggeredEvent();
        List<IFile> a = test.getChangedFiles(event1.getDelta().getAffectedChildren());

        List<String> expectedFiles = new ArrayList<String>(3);
        expectedFiles.add("L/TestProject/productdef/products/folder/Supersupertype.ipspolicycmpttype");
        expectedFiles.add("L/TestProject/productdef/products/folder/Supertype.ipspolicycmpttype");
        expectedFiles.add("L/TestProject/productdef/products/folder/TestPolicy.ipspolicycmpttype");

        boolean[] expectedFilesFound = new boolean[3];

        for (IFile aFile : a) {
            for (int i = 0; i < 3; i++) {
                if (expectedFiles.get(i).equals(aFile.toString())) {
                    expectedFilesFound[i] = true;
                }
            }
        }

        assertTrue(expectedFilesFound[0]);
        assertTrue(expectedFilesFound[1]);
        assertTrue(expectedFilesFound[2]);

        for (IFile b : a) {
            iipsElement = IpsPlugin.getDefault().getIpsModel().findIpsElement(b);
            assertTrue(iipsElement instanceof IpsSrcFile);
        }
    }

    private IResourceChangeEvent getTriggeredEvent() throws InterruptedException {
        while (event == null) {
            Thread.sleep(500);
            Thread.yield();
        }
        IResourceChangeEvent recentEvent = event;
        event = null;
        return recentEvent;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (eventNr == 0) {
            eventNr++;
            this.event = event;
        }
    }
}
