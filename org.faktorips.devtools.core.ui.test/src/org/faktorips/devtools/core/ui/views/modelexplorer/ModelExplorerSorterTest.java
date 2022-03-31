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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

public class ModelExplorerSorterTest extends AbstractIpsPluginTest {

    private ModelExplorerSorter sorter;

    private IIpsProject proj;
    private IIpsProject proj2;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment packageFragment;
    private IIpsPackageFragment packageFragment2;
    private IIpsPackageFragment packageFragment3;
    private IIpsPackageFragment packageFragment4;
    private IIpsPackageFragment defaultPackage;

    private IIpsSrcFile policyCTSrcFile;
    private IIpsSrcFile policyCT2SrcFile;
    private IIpsSrcFile tableSrcFile;

    private IFolder folder;
    private IFolder subFolder;
    private IFile file;

    private IProject projectResource1;
    private IProject projectResource2;

    private IPolicyCmptTypeAttribute attr1;

    private IPolicyCmptTypeAssociation rel;

    private IPolicyCmptTypeAttribute attr2;

    private IIpsObjectPathContainer pathContainer;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        sorter = new ModelExplorerSorter(true);
        proj = newIpsProject("aTestProject");
        proj2 = newIpsProject("zProject");
        IIpsProject proj3 = newIpsProject("middleProject");
        IIpsProject proj4 = newIpsProject("CAPITALProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        packageFragment = root.createPackageFragment("TestPackageFragment", false, null);
        packageFragment2 = root.createPackageFragment("ZTestPackageFragment", false, null);
        defaultPackage = root.getDefaultIpsPackageFragment();
        PolicyCmptType policyCT = newPolicyCmptType(proj.getIpsPackageFragmentRoots()[0], "TestPolicy");
        policyCTSrcFile = policyCT.getIpsSrcFile();
        policyCT2SrcFile = newPolicyCmptType(proj.getIpsPackageFragmentRoots()[0], "TestPolicy2").getIpsSrcFile();
        tableSrcFile = newTableStructure(proj.getIpsPackageFragmentRoots()[0], "Aaa").getIpsSrcFile();

        IIpsPackageFragmentRoot root2 = proj2.getIpsPackageFragmentRoots()[0];
        packageFragment3 = root2.createPackageFragment("TestPackageFragment", false, null);
        packageFragment4 = root2.createPackageFragment("ZTestPackageFragment", false, null);

        attr1 = policyCT.newPolicyCmptTypeAttribute();
        rel = policyCT.newPolicyCmptTypeAssociation();
        attr2 = policyCT.newPolicyCmptTypeAttribute();

        projectResource1 = proj3.getCorrespondingResource().unwrap();
        projectResource2 = proj4.getCorrespondingResource().unwrap();
        folder = ((AProject)proj.getCorrespondingResource()).getFolder("folder").unwrap();
        folder.create(true, false, null);
        subFolder = ((AProject)proj.getCorrespondingResource()).getFolder("subfolder").unwrap();
        subFolder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);

        pathContainer = mock(IIpsObjectPathContainer.class);

        // create sort order file
        createSortOrderFile((AFolder)root.getCorrespondingResource(),
                "ZTestPackageFragment", "TestPackageFragment");
    }

    @Test
    public void testCompareViewerObjectObject() {
        // Identity should be evaluated as equal
        assertTrue(sorter.compare(null, proj, proj) == 0);
        // Projects should be sorted lexicographically (ignoring case)
        assertTrue(sorter.compare(null, proj, projectResource1) < 0);
        assertTrue(sorter.compare(null, projectResource2, projectResource1) < 0);
        assertTrue(sorter.compare(null, projectResource1, proj2) < 0);
        assertTrue(sorter.compare(null, proj, proj2) < 0);

        assertTrue(sorter.compare(null, pathContainer, file) < 0);
        assertTrue(sorter.compare(null, pathContainer, root) < 0);
        assertTrue(sorter.compare(null, pathContainer, proj) < 0);

        // in policyCmptTypes sort attributes above relations
        assertTrue(sorter.compare(null, attr1, rel) < 0);
        assertTrue(sorter.compare(null, attr2, rel) < 0);

        // sort PackageFragments lexicographically
        assertTrue(sorter.compare(null, packageFragment3, packageFragment4) < 0);
        assertTrue(sorter.compare(null, packageFragment4, packageFragment3) > 0);
        // sort PackageFragments arbitrary
        assertTrue(sorter.compare(null, packageFragment, packageFragment2) > 0);
        assertTrue(sorter.compare(null, packageFragment2, packageFragment) < 0);
        // sort DefaultPackage above other PackageFragments
        assertTrue(sorter.compare(null, defaultPackage, packageFragment) < 0);
        assertTrue(sorter.compare(null, defaultPackage, packageFragment2) < 0);
        assertTrue(sorter.compare(null, packageFragment, defaultPackage) > 0);
        assertTrue(sorter.compare(null, packageFragment2, defaultPackage) > 0);
        // TableStructures should be sorted after/below PolicyCmptTypes (et vice versa)
        assertTrue(sorter.compare(null, policyCTSrcFile, tableSrcFile) < 0);
        assertTrue(sorter.compare(null, tableSrcFile, policyCTSrcFile) > 0);
        // Tables or PolCmpTypes should sorted below PackageFragments
        assertTrue(sorter.compare(null, packageFragment, tableSrcFile) < 0);
        assertTrue(sorter.compare(null, tableSrcFile, packageFragment) > 0);
        assertTrue(sorter.compare(null, packageFragment, policyCTSrcFile) < 0);
        assertTrue(sorter.compare(null, policyCTSrcFile, packageFragment) > 0);
        // equal Elements should be sorted lexicographicaly
        assertTrue(sorter.compare(null, policyCTSrcFile, policyCT2SrcFile) < 0);
        assertTrue(sorter.compare(null, policyCT2SrcFile, policyCTSrcFile) > 0);

        // IResource tests
        assertTrue(sorter.compare(null, folder, file) < 0);
        assertTrue(sorter.compare(null, file, folder) > 0);
        assertTrue(sorter.compare(null, folder, subFolder) < 0);
        assertTrue(sorter.compare(null, subFolder, folder) > 0);
    }

    @Test
    public void testCompareViewer_WithoutCategory() {
        sorter.setSupportCategories(false);

        assertTrue(sorter.compare(null, policyCTSrcFile, tableSrcFile) > 0);
        assertTrue(sorter.compare(null, tableSrcFile, policyCTSrcFile) < 0);
        assertTrue(sorter.compare(null, policyCTSrcFile, policyCT2SrcFile) < 0);
        assertTrue(sorter.compare(null, policyCT2SrcFile, policyCTSrcFile) > 0);
    }

}
