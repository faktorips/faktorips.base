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
import org.faktorips.devtools.core.internal.model.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;

public class EmptyPackageFilterTest extends AbstractIpsPluginTest {

    private EmptyPackageFilter filter= new EmptyPackageFilter();
    
    private IpsProject proj; 
    private IpsPackageFragmentRoot root;
    private PolicyCmptType polCmptType;
    private IpsPackageFragment subPackage;
    private IpsPackageFragment subsubPackage;
    private IpsPackageFragment empty;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        proj= (IpsProject)newIpsProject("TestProject");
        root= (IpsPackageFragmentRoot) proj.getIpsPackageFragmentRoots()[0];
        polCmptType= newPolicyCmptType(root, "subpackage.subsubpackage.TestPolicy");
        subPackage= (IpsPackageFragment) root.createPackageFragment("subpackage", true, null);
        subsubPackage= (IpsPackageFragment) root.createPackageFragment("subpackage.subsubpackage", true, null);
        empty= (IpsPackageFragment) root.createPackageFragment("subpackage.subsubpackage.emptypackage", true, null);
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.EmptyPackageFilter.select(Viewer, Object, Object)'
     */
    public void testSelectViewerObjectObject() {
        // packageFragments are filtered if they contain subFolders and at the same time do NOT contain IPSObjects.
        assertFalse(filter.select(null, null, subPackage));
        // packageFragments containing IpsObjects are selected
        assertTrue(filter.select(null, null, subsubPackage));
        // completely empty packagefragments are selected
        assertTrue(filter.select(null, null, empty));
        // all other types are selected
        assertTrue(filter.select(null, null, proj));
        assertTrue(filter.select(null, null, root));
        assertTrue(filter.select(null, null, polCmptType));
    }

}
