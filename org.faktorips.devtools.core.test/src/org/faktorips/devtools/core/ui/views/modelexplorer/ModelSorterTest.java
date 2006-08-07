/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.IIpsPackageFragment;

public class ModelSorterTest extends AbstractIpsPluginTest {

    private ModelExplorerSorter sorter;

    private IpsProject proj; 
    private IpsProject proj2; 
    private PolicyCmptType policyCT;
    private PolicyCmptType policyCT2;
    private TableStructure table;
    private IIpsPackageFragment fragment;
    

    
    protected void setUp() throws Exception {
        super.setUp();
        sorter= new ModelExplorerSorter();
        proj= (IpsProject)newIpsProject("TestProject");
        proj2= (IpsProject)newIpsProject("ZProject");
        fragment= proj.getIpsPackageFragmentRoots()[0].createPackageFragment("TestPackageFragment", false, null);
        policyCT = newPolicyCmptType(proj.getIpsPackageFragmentRoots()[0], "TestPolicy");
        policyCT2 = newPolicyCmptType(proj.getIpsPackageFragmentRoots()[0], "TestPolicy2");
        table= new TableStructure();
    }
    
	/*
	 * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelSorter.compare(Viewer, Object, Object)'
	 */
	public void testCompareViewerObjectObject() {
		// Identity should be evaluated as equal
		assertTrue(sorter.compare(null, proj, proj) == 0);
		// Projects should be sorted lexicographically
		assertTrue(sorter.compare(null, proj, proj2) < 0);
		assertTrue(sorter.compare(null, proj2, proj) > 0);
		// TableStructures should be sorted after/below PolicyCmptTypes (et vice versa) 
		assertTrue(sorter.compare(null, policyCT, table) < 0);
		assertTrue(sorter.compare(null, table, policyCT) > 0);
        // Tables or PolCmpTypes should sorted below PackageFragments
        assertTrue(sorter.compare(null, fragment, table) < 0);
        assertTrue(sorter.compare(null, table, fragment) > 0);
        assertTrue(sorter.compare(null, fragment, policyCT) < 0);
        assertTrue(sorter.compare(null, policyCT, fragment) > 0);
        // equal Elements should be sorted lexicographicaly
        assertTrue(sorter.compare(null, policyCT, policyCT2) < 0);
        assertTrue(sorter.compare(null, policyCT2, policyCT) > 0);
	}
	
}
