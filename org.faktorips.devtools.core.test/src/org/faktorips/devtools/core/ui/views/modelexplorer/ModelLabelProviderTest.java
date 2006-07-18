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

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsPackageFragment;
import org.faktorips.devtools.core.internal.model.IpsPackageFragmentRoot;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;

public class ModelLabelProviderTest extends AbstractIpsPluginTest {
    private ModelLabelProvider flatProvider= new ModelLabelProvider(true);
    private ModelLabelProvider hierarchyProvider= new ModelLabelProvider(false);

    private IpsProject proj; 
    private IpsPackageFragmentRoot root;
    private PolicyCmptType polCmptType;
    private IpsPackageFragment subPackage;
    private IpsPackageFragment subsubPackage;
    private IpsPackageFragment empty;
    private IAttribute attr;
    private IAttribute attr2;
    private IAttribute attr3;
    
    protected void setUp() throws Exception {
        super.setUp();
        proj= (IpsProject)newIpsProject("TestProject");
        root= (IpsPackageFragmentRoot) proj.getIpsPackageFragmentRoots()[0];
        subPackage= (IpsPackageFragment) root.createPackageFragment("subpackage", true, null);
        subsubPackage= (IpsPackageFragment) root.createPackageFragment("subpackage.subsubpackage", true, null);
        empty= (IpsPackageFragment) root.createPackageFragment("subpackage.subsubpackage.emptypackage", true, null);
        polCmptType= newPolicyCmptType(root, "subpackage.subsubpackage.TestPolicy");
        attr= polCmptType.newAttribute();
        attr.setDatatype("String");
        attr.setAttributeType(AttributeType.CONSTANT);
        attr2= polCmptType.newAttribute();
        attr2.setDatatype("int");
        attr2.setAttributeType(AttributeType.CHANGEABLE);
        attr3= polCmptType.newAttribute();
        attr3.setDatatype("float");
        attr3.setAttributeType(AttributeType.COMPUTED);
    }


    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider.getImage(Object)'
     */
    public void testGetImage() {
        // Image returned by getImage() equals Image returned by IpsElement#getImage()
        Image img= proj.getImage();
        assertTrue(img==flatProvider.getImage(proj));
        assertTrue(img==hierarchyProvider.getImage(proj));
        img= root.getImage();
        assertTrue(img==flatProvider.getImage(root));
        assertTrue(img==hierarchyProvider.getImage(root));
        img= polCmptType.getImage();
        assertTrue(img==flatProvider.getImage(polCmptType));
        assertTrue(img==hierarchyProvider.getImage(polCmptType));
        img= IpsPlugin.getDefault().getImage("folder_open.gif");
        assertTrue(img==flatProvider.getImage(subPackage));
        assertTrue(img==hierarchyProvider.getImage(subPackage));
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider.getText(Object)'
     */
    public void testGetText() {
        // test attribute labels 
        testAttribute(flatProvider, attr);
        testAttribute(flatProvider, attr2);
        testAttribute(flatProvider, attr3);
        testAttribute(hierarchyProvider, attr);
        testAttribute(hierarchyProvider, attr2);
        testAttribute(hierarchyProvider, attr3);
        
        // packagefragment Labels
        // Flat Layout
        String fragmentName= flatProvider.getText(subPackage);
        assertEquals("subpackage", fragmentName);
        fragmentName= flatProvider.getText(subsubPackage);
        assertEquals("subpackage.subsubpackage", fragmentName);
        fragmentName= flatProvider.getText(empty);
        assertEquals("subpackage.subsubpackage.emptypackage", fragmentName);
        // hierarchical Layout
        fragmentName= hierarchyProvider.getText(subPackage);
        assertEquals("subpackage", fragmentName);
        fragmentName= hierarchyProvider.getText(subsubPackage);
        assertEquals("subsubpackage", fragmentName);
        fragmentName= hierarchyProvider.getText(empty);
        assertEquals("emptypackage", fragmentName);
        
        // other types: returned String equals getName()
        String projName= flatProvider.getText(proj);
        assertEquals(proj.getName(), projName);
        String rootName= flatProvider.getText(root);
        assertEquals(root.getName(), rootName);
        String polName= flatProvider.getText(polCmptType);
        assertEquals(polCmptType.getName(), polName);
        
        projName= hierarchyProvider.getText(proj);
        assertEquals(proj.getName(), projName);
        rootName= hierarchyProvider.getText(root);
        assertEquals(root.getName(), rootName);
        polName= hierarchyProvider.getText(polCmptType);
        assertEquals(polCmptType.getName(), polName);
    }
    
    // format: "<attributeName><5 blanks>[<dataType>,<blank><attributeType>]" 
    private void testAttribute(ModelLabelProvider provider, IAttribute a) {
        String attrLabel= provider.getText(a);
        assertTrue(attrLabel.startsWith(a.getName()));
        String dType= attrLabel.substring(attrLabel.indexOf("[")+1, attrLabel.lastIndexOf(","));
        assertEquals(a.getDatatype(), dType);
        String aType= attrLabel.substring(attrLabel.indexOf(",")+2, attrLabel.lastIndexOf("]"));
        assertEquals(a.getAttributeType().getId().toString(), aType);
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider.addListener(ILabelProviderListener)'
     */
    public void testAddListener() {
//      no tests
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider.dispose()'
     */
    public void testDispose() {
        // no tests
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider.isLabelProperty(Object, String)'
     */
    public void testIsLabelProperty() {
//      no tests
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider.removeListener(ILabelProviderListener)'
     */
    public void testRemoveListener() {
//      no tests
    }

}
