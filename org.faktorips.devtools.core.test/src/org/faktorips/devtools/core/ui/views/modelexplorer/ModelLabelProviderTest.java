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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

public class ModelLabelProviderTest extends AbstractIpsPluginTest {
    private ModelLabelProvider flatProvider= new ModelLabelProvider(true);
    private ModelLabelProvider hierarchyProvider= new ModelLabelProvider(false);

    private IIpsProject proj; 
    private IIpsPackageFragmentRoot root;
    private IPolicyCmptType polCmptType;
    private IIpsPackageFragment defaultPackage;
    private IIpsPackageFragment subPackage;
    private IIpsPackageFragment subsubPackage;
    private IIpsPackageFragment empty;
    private IAttribute attr;
    private IAttribute attr2;
    private IAttribute attr3;

    private IFolder folder;
    private IFolder subFolder;
    private IFile file;
    
    protected void setUp() throws Exception {
        super.setUp();
        proj= newIpsProject("TestProject");
        root= proj.getIpsPackageFragmentRoots()[0];
        defaultPackage= root.getDefaultIpsPackageFragment();
        subPackage= root.createPackageFragment("subpackage", true, null);
        subsubPackage= root.createPackageFragment("subpackage.subsubpackage", true, null);
        empty= root.createPackageFragment("subpackage.subsubpackage.emptypackage", true, null);
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

        folder = ((IProject)proj.getCorrespondingResource()).getFolder("testfolder");
        folder.create(true, false, null);
        subFolder = folder.getFolder("subfolder");
        subFolder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);
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
        img= subPackage.getImage();
        assertEquals(flatProvider.getImage(subPackage), img);
        assertTrue(img==hierarchyProvider.getImage(subPackage));
        
        // tests for IResources
        IWorkbenchAdapter adapter= (IWorkbenchAdapter) folder.getAdapter(IWorkbenchAdapter.class);
        assertNotNull(adapter);
        img= adapter.getImageDescriptor(folder).createImage();
        // TODO test equality of images
//        assertEquals(hierarchyProvider.getImage(folder), img);
//        assertEquals(flatProvider.getImage(folder), img);
        
        adapter= (IWorkbenchAdapter) file.getAdapter(IWorkbenchAdapter.class);
        assertNotNull(adapter);
        img= adapter.getImageDescriptor(file).createImage();
//        assertEquals(hierarchyProvider.getImage(file), img);
//        assertEquals(flatProvider.getImage(file), img);
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelLabelProvider.getText(Object)'
     */
    public void testGetText() throws CoreException {
        // test attribute labels 
        testAttribute(flatProvider, attr);
        testAttribute(flatProvider, attr2);
        testAttribute(flatProvider, attr3);
        testAttribute(hierarchyProvider, attr);
        testAttribute(hierarchyProvider, attr2);
        testAttribute(hierarchyProvider, attr3);
        
        String fragmentName;
        // packagefragment Labels
        // hierarchical Layout
        fragmentName= hierarchyProvider.getText(subPackage);
        assertEquals("subpackage", fragmentName);
        fragmentName= hierarchyProvider.getText(subsubPackage);
        assertEquals("subsubpackage", fragmentName);
        fragmentName= hierarchyProvider.getText(empty);
        assertEquals("emptypackage", fragmentName);
        fragmentName= hierarchyProvider.getText(defaultPackage);
        assertEquals(Messages.ModelExplorer_defaultPackageLabel, fragmentName);
        // Flat Layout
        fragmentName= flatProvider.getText(subPackage);
        assertEquals("subpackage", fragmentName);
        fragmentName= flatProvider.getText(subsubPackage);
        assertEquals("subpackage.subsubpackage", fragmentName);
        fragmentName= flatProvider.getText(empty);
        assertEquals("subpackage.subsubpackage.emptypackage", fragmentName);
        fragmentName= flatProvider.getText(defaultPackage);
        assertEquals(Messages.ModelExplorer_defaultPackageLabel, fragmentName);
        
        // other types: returned String equals getName()
        String name= hierarchyProvider.getText(proj);
        assertEquals(proj.getName(), name);
        name= hierarchyProvider.getText(root);
        assertEquals(root.getName(), name);
        name= hierarchyProvider.getText(polCmptType);
        assertEquals(polCmptType.getName(), name);
        
        name= flatProvider.getText(proj);
        assertEquals(proj.getName(), name);
        name= flatProvider.getText(root);
        assertEquals(root.getName(), name);
        name= flatProvider.getText(polCmptType);
        assertEquals(polCmptType.getName(), name);
        
        // IResources
        String resName= hierarchyProvider.getText(folder);
        assertEquals(folder.getName(), resName);
        resName= hierarchyProvider.getText(file);
        assertEquals(file.getName(), resName);
        resName= hierarchyProvider.getText(subFolder);
        assertEquals(subFolder.getName(), resName);
        
        resName= flatProvider.getText(folder);
        assertEquals(folder.getName(), resName);
        resName= flatProvider.getText(file);
        assertEquals(file.getName(), resName);
        resName= flatProvider.getText(subFolder);
        assertEquals(subFolder.getName(), resName);
        
        // non ips projects
        IProject platformProject= newPlatformProject("PlatformProject");
        resName= hierarchyProvider.getText(platformProject);
        assertEquals(platformProject.getName()+" ("+Messages.ModelExplorer_nonIpsProjectLabel+")", resName);
        resName= flatProvider.getText(platformProject);
        assertEquals(platformProject.getName()+" ("+Messages.ModelExplorer_nonIpsProjectLabel+")", resName);
    }
    
    // format: "<attributeName><blank>:<blank><dataType>,<blank><attributeType>" 
    private void testAttribute(ModelLabelProvider provider, IAttribute a) {
        String attrLabel= provider.getText(a);
        assertTrue(attrLabel.startsWith(a.getName()));
        String dType= attrLabel.substring(attrLabel.indexOf(":")+2, attrLabel.lastIndexOf(",")); // +2 -> ignore following blank
        assertEquals(a.getDatatype(), dType);
        String aType= attrLabel.substring(attrLabel.indexOf(",")+2, attrLabel.length());
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
