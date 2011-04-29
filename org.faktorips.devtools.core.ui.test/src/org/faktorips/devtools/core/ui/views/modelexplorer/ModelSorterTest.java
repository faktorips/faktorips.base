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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

public class ModelSorterTest extends AbstractIpsPluginTest {

    private ModelExplorerSorter sorter;

    private IIpsProject proj;
    private IIpsProject proj2;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment packageFragment;
    private IIpsPackageFragment packageFragment2;
    private IIpsPackageFragment packageFragment3;
    private IIpsPackageFragment packageFragment4;
    private IIpsPackageFragment defaultPackage;

    private IPolicyCmptType policyCT;
    private IPolicyCmptType policyCT2;
    private ITableStructure table;

    private IFolder folder;
    private IFolder subFolder;
    private IFile file;

    private IProject projectResource1;
    private IProject projectResource2;

    private IPolicyCmptTypeAttribute attr1;

    private IPolicyCmptTypeAssociation rel;

    private IPolicyCmptTypeAttribute attr2;

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
        policyCT = newPolicyCmptType(proj.getIpsPackageFragmentRoots()[0], "TestPolicy");
        policyCT2 = newPolicyCmptType(proj.getIpsPackageFragmentRoots()[0], "TestPolicy2");
        table = new TableStructure();

        IIpsPackageFragmentRoot root2 = proj2.getIpsPackageFragmentRoots()[0];
        packageFragment3 = root2.createPackageFragment("TestPackageFragment", false, null);
        packageFragment4 = root2.createPackageFragment("ZTestPackageFragment", false, null);

        attr1 = policyCT.newPolicyCmptTypeAttribute();
        rel = policyCT.newPolicyCmptTypeAssociation();
        attr2 = policyCT.newPolicyCmptTypeAttribute();

        projectResource1 = (IProject)proj3.getCorrespondingResource();
        projectResource2 = (IProject)proj4.getCorrespondingResource();
        folder = ((IProject)proj.getCorrespondingResource()).getFolder("folder");
        folder.create(true, false, null);
        subFolder = ((IProject)proj.getCorrespondingResource()).getFolder("subfolder");
        subFolder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);

        // create sort order file
        List<String> list = new ArrayList<String>();
        list.add("ZTestPackageFragment");
        list.add("TestPackageFragment");
        createPackageOrderFile((IFolder)root.getCorrespondingResource(), list);
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
        assertTrue(sorter.compare(null, policyCT, table) < 0);
        assertTrue(sorter.compare(null, table, policyCT) > 0);
        // Tables or PolCmpTypes should sorted below PackageFragments
        assertTrue(sorter.compare(null, packageFragment, table) < 0);
        assertTrue(sorter.compare(null, table, packageFragment) > 0);
        assertTrue(sorter.compare(null, packageFragment, policyCT) < 0);
        assertTrue(sorter.compare(null, policyCT, packageFragment) > 0);
        // equal Elements should be sorted lexicographicaly
        assertTrue(sorter.compare(null, policyCT, policyCT2) < 0);
        assertTrue(sorter.compare(null, policyCT2, policyCT) > 0);

        // IResource tests
        assertTrue(sorter.compare(null, folder, file) < 0);
        assertTrue(sorter.compare(null, file, folder) > 0);
        assertTrue(sorter.compare(null, folder, subFolder) < 0);
        assertTrue(sorter.compare(null, subFolder, folder) > 0);
    }

}
