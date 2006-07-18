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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.IIpsProjectProperties;

/*
 * Testrichtlinie: immer so testen, dass TextContent erweitert werden kann. nie ueber alle Elemente iterieren, sondern nur welche erwarten
 */
public class ModelContentProviderTest extends AbstractIpsPluginTest {
    private ModelContentProvider flatProvider= new ModelContentProvider(true);
    private ModelContentProvider hierarchyProvider= new ModelContentProvider(false);

    private IpsProject proj; 
    private IpsProject modelProj;
    private IpsProject prodDefProj; 
    private IpsPackageFragmentRoot root;
    private IpsPackageFragmentRoot emptyRoot;
    private IpsPackageFragment subPackage;
    private IpsPackageFragment subsubPackage;
    private IpsPackageFragment emptyPackage;
    private PolicyCmptType polCmptType;
    private PolicyCmptType polCmptType2;
    private ProductCmpt prodCmpt;
    
    protected void setUp() throws Exception {
        super.setUp();
        proj= (IpsProject)newIpsProject("TestProject");
        setModelProjectProperty(proj, true);
        modelProj= (IpsProject)newIpsProject("TestModelProject");
        setModelProjectProperty(modelProj, true);
        prodDefProj= (IpsProject)newIpsProject("TestProductDefinitionProject");
        setModelProjectProperty(prodDefProj, false);

        root= (IpsPackageFragmentRoot) proj.getIpsPackageFragmentRoots()[0];
        emptyRoot= (IpsPackageFragmentRoot) prodDefProj.getIpsPackageFragmentRoots()[0];
        subPackage= (IpsPackageFragment) root.createPackageFragment("subpackage", true, null);
        subsubPackage= (IpsPackageFragment) root.createPackageFragment("subpackage.subsubpackage", true, null);
        emptyPackage= (IpsPackageFragment) root.createPackageFragment("subpackage.subsubpackage.emptypackage", true, null);
        polCmptType= newPolicyCmptType(root, "subpackage.subsubpackage.TestPolicy");
        polCmptType2= newPolicyCmptType(root, "subpackage.subsubpackage.TestPolicy2");
        polCmptType.newAttribute();
        polCmptType.newAttribute();
        polCmptType.newAttribute();
        prodCmpt= newProductCmpt(root, "subpackage.subsubpackage.TestProductComponent");
    }

    private void setModelProjectProperty(IpsProject project, boolean b) throws CoreException {
        IIpsProjectProperties props= project.getProperties();
        props.setModelProject(b);
        project.setProperties(props);
    }


    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider.getChildren(Object)'
     */
    public void testGetChildren(){        
        // Tests for hierarchical layout style
        // root has exactly one child: subpackage
        Object[] children= hierarchyProvider.getChildren(root);
        assertEquals(1, children.length);
        assertEquals(children[0], subPackage);
        // subPackage has exactly one child: subpackage.subsubPackage
        children= hierarchyProvider.getChildren(subPackage);
        assertEquals(1, children.length);
        assertEquals(children[0], subsubPackage);
        // subsubPackage has four children, emptyPackage, two policyCmptTypes and one ProductComponent
        children= hierarchyProvider.getChildren(subsubPackage);
        assertEquals(4, children.length);
        IpsPackageFragment emptyP= (IpsPackageFragment) children[0];
        assertEquals(children[0], emptyP);
        
        // tests for flat layout
        // root returns all its child PackageFragments. It contains the following four children 
        // pFragment with emptyString as name, pFragment subpackage, pFragment subpackage.subsubpackage, pFragment subpackage.subsubpackage.emptypackage
        children= flatProvider.getChildren(root);
        assertEquals(4, children.length);  
        List fragments= Arrays.asList(children);
        assertTrue(fragments.contains(subPackage));
        assertTrue(fragments.contains(subsubPackage));
        assertTrue(fragments.contains(emptyPackage));
        // subPackage contains no children
        assertEquals(0, flatProvider.getChildren(subPackage).length);
        // subsubpackage contains three children, two PolicyCmptTypes, one ProductComponent
        children= flatProvider.getChildren(subsubPackage);
        assertEquals(3, children.length);
        List ipsObjects= Arrays.asList(children);
        assertTrue(ipsObjects.contains(polCmptType));
        assertTrue(ipsObjects.contains(polCmptType2));
        assertTrue(ipsObjects.contains(prodCmpt));
        
        // general tests
        // emptyPackage has no children
        assertEquals(0, hierarchyProvider.getChildren(emptyPackage).length);
        // PolCmptType attributes are returned as children, polCmptType contains three children
        children= hierarchyProvider.getChildren(polCmptType);
        assertEquals(3, children.length);  
        
        assertEquals(0, flatProvider.getChildren(emptyPackage).length);
        // PolCmptType attributes are returned as children, polCmptType contains three children
        children= flatProvider.getChildren(polCmptType);
        assertEquals(3, children.length);  
        
        // TODO Tests using TableContents and TableStructures 
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider.getParent(Object)'
     */
    public void testGetParent() {
        // tests for flat layout
        Object parent= flatProvider.getParent(emptyPackage);
        assertEquals(root, parent);
        parent= flatProvider.getParent(subsubPackage);
        assertEquals(root, parent);
        parent= flatProvider.getParent(subPackage);
        assertEquals(root, parent);
        // tests for hierarchical layout
        // Test packageFragments for corresponding ressource. Tests for Object identity can not be used as PFragments (handles for said ressources) are created during tests. 
        parent= hierarchyProvider.getParent(emptyPackage);
        assertEquals(subsubPackage.getCorrespondingResource(), ((IpsPackageFragment)parent).getCorrespondingResource());
        parent= hierarchyProvider.getParent(parent);
        assertEquals(subPackage.getCorrespondingResource(), ((IpsPackageFragment)parent).getCorrespondingResource());
        parent= hierarchyProvider.getParent(parent);
        assertEquals(root.getIpsDefaultPackageFragment().getCorrespondingResource(), ((IpsPackageFragment)parent).getCorrespondingResource());
        
        // for all other types getParent() acts like IpsElement#getParent()
        parent= hierarchyProvider.getParent(proj);
        assertEquals(IpsPlugin.getDefault().getIpsModel(), parent);
        parent= hierarchyProvider.getParent(root);
        assertEquals(proj, parent);
        parent= hierarchyProvider.getParent(polCmptType);
        assertEquals(subsubPackage, parent);
        parent= hierarchyProvider.getParent(polCmptType2);
        assertEquals(subsubPackage, parent);
        parent= hierarchyProvider.getParent(prodCmpt);
        assertEquals(subsubPackage, parent);

        parent= flatProvider.getParent(proj);
        assertEquals(IpsPlugin.getDefault().getIpsModel(), parent);
        parent= flatProvider.getParent(root);
        assertEquals(proj, parent);
        parent= flatProvider.getParent(polCmptType);
        assertEquals(subsubPackage, parent);
        parent= flatProvider.getParent(polCmptType2);
        assertEquals(subsubPackage, parent);
        parent= flatProvider.getParent(prodCmpt);
        assertEquals(subsubPackage, parent);
        
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider.hasChildren(Object)'
     */
    public void testHasChildren() throws CoreException {
        // Test for Projects
        assertTrue(flatProvider.hasChildren(proj));
        assertTrue(hierarchyProvider.hasChildren(proj));
        // an empty project returns its default PackageFragmentRoot
        assertTrue(flatProvider.hasChildren(modelProj));
        assertTrue(hierarchyProvider.hasChildren(modelProj));
        // Test for PackageFragmentRoots
        assertTrue(flatProvider.hasChildren(root));
        assertTrue(hierarchyProvider.hasChildren(root));
        // in flat layout getChildren 
        // an empty PFragmentRoot in flat layout returns at least one child, the default packageFragment
        assertTrue(flatProvider.hasChildren(emptyRoot));
        // a PackageFragmentRoot in hierarchical layout returns the children (files) of the defaultPackageFragment, 
        // an empty PackageFragmentRoot thus returns no children
        assertFalse(hierarchyProvider.hasChildren(emptyRoot));
        // hierarchichal tests
        assertTrue(hierarchyProvider.hasChildren(subPackage));
        assertTrue(hierarchyProvider.hasChildren(subsubPackage));
        assertFalse(hierarchyProvider.hasChildren(emptyPackage));
        // Flat tests
        assertFalse(flatProvider.hasChildren(subPackage));
        assertTrue(flatProvider.hasChildren(subsubPackage));
        assertFalse(flatProvider.hasChildren(emptyPackage));
        // PolicyCmptType tests
        assertTrue(flatProvider.hasChildren(polCmptType));
        assertTrue(hierarchyProvider.hasChildren(polCmptType));
        assertFalse(flatProvider.hasChildren(polCmptType2));
        assertFalse(hierarchyProvider.hasChildren(polCmptType2));
        // ProductCmpt tests
        assertFalse(hierarchyProvider.hasChildren(prodCmpt));
        assertFalse(flatProvider.hasChildren(prodCmpt));
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider.getElements(Object)'
     */
    public void testGetElements() throws CoreException {
        // getElement returns all model-projects
        Object[] children= hierarchyProvider.getElements(new Object());
        assertEquals(2, children.length);
        
        // getElement returns all model-projects
        children= flatProvider.getElements(new Object());
        assertEquals(2, children.length);
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider.dispose()'
     */
    public void testDispose() {
        //no tests
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelContentProvider.inputChanged(Viewer, Object, Object)'
     */
    public void testInputChanged() {
        //no tests
    }


}
