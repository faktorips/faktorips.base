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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

public class ModelLabelProviderTest extends AbstractIpsPluginTest {

    private static final String REFERENCED_PROJECT = "ReferencedProject";
    private static final String CONTAINER_NAME = "ContainerName";
    private ModelLabelProvider flatProvider = new ModelLabelProvider(true);
    private ModelLabelProvider hierarchyProvider = new ModelLabelProvider(false);

    private IIpsProject proj;
    private IIpsPackageFragmentRoot root;
    private IPolicyCmptType polCmptType;
    private IIpsPackageFragment defaultPackage;
    private IIpsPackageFragment subPackage;
    private IIpsPackageFragment subsubPackage;
    private IIpsPackageFragment empty;
    private IPolicyCmptTypeAttribute attr;
    private IPolicyCmptTypeAttribute attr2;
    private IPolicyCmptTypeAttribute attr3;

    private IIpsObjectPathContainer container;

    private IFolder folder;
    private IFolder subFolder;
    private IFile file;
    private ReferencedIpsProjectViewItem projectViewItem;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        proj = newIpsProject("TestProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        defaultPackage = root.getDefaultIpsPackageFragment();
        subPackage = root.createPackageFragment("subpackage", true, null);
        subsubPackage = root.createPackageFragment("subpackage.subsubpackage", true, null);
        empty = root.createPackageFragment("subpackage.subsubpackage.emptypackage", true, null);
        polCmptType = newPolicyCmptType(root, "subpackage.subsubpackage.TestPolicy");
        attr = polCmptType.newPolicyCmptTypeAttribute();
        attr.setDatatype("String");
        attr.setName("a1");
        attr.setAttributeType(AttributeType.CONSTANT);
        attr2 = polCmptType.newPolicyCmptTypeAttribute();
        attr2.setDatatype("int");
        attr2.setName("a2");
        attr2.setAttributeType(AttributeType.CHANGEABLE);
        attr3 = polCmptType.newPolicyCmptTypeAttribute();
        attr3.setName("a3");
        attr3.setDatatype("float");
        attr3.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);

        folder = ((AProject)proj.getCorrespondingResource()).getFolder("testfolder").unwrap();
        folder.create(true, false, null);
        subFolder = folder.getFolder("subfolder");
        subFolder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);

        container = mock(IIpsObjectPathContainer.class);
        when(container.getName()).thenReturn(CONTAINER_NAME);

        IIpsProject project = mock(IIpsProject.class);
        when(project.getName()).thenReturn(REFERENCED_PROJECT);
        projectViewItem = new ReferencedIpsProjectViewItem(project);

    }

    @Test
    public void testGetText_Attribute() {
        assertEquals("a1 : String", flatProvider.getText(attr));
        assertEquals("a1 : String", hierarchyProvider.getText(attr));

        assertEquals("/ a3 : float", flatProvider.getText(attr3));
        assertEquals("/ a3 : float", hierarchyProvider.getText(attr3));
    }

    @Test
    public void testGetImage() {
        // Image returned by getImage() equals Image returned by IpsElement#getImage()
        Image img = IpsUIPlugin.getImageHandling().getImage(proj.getProject());
        assertTrue(img == flatProvider.getImage(proj));
        assertTrue(img == hierarchyProvider.getImage(proj));
        img = IpsUIPlugin.getImageHandling().getImage(root);
        assertTrue(img == flatProvider.getImage(root));
        assertTrue(img == hierarchyProvider.getImage(root));
        img = IpsUIPlugin.getImageHandling().getImage(polCmptType);
        assertTrue(img == flatProvider.getImage(polCmptType));
        assertTrue(img == hierarchyProvider.getImage(polCmptType));
        img = IpsUIPlugin.getImageHandling().getImage(subPackage);
        assertEquals(flatProvider.getImage(subPackage), img);
        assertTrue(img == hierarchyProvider.getImage(subPackage));

        // tests for none faktor-ips classes, e.g. IFile
        assertNotNull(flatProvider.getImage(file));
        assertNotNull(hierarchyProvider.getImage(file));
    }

    @Test
    public void testIfImagesAreReusedAndDisposedCorrectly() {
        Image image = flatProvider.getImage(file);
        assertSame(image, flatProvider.getImage(file));

        Image image2 = flatProvider.getImage(folder);
        assertSame(image2, flatProvider.getImage(folder));

        flatProvider.dispose();
    }

    @Test
    public void testGetText() throws IpsException, CoreException {
        String fragmentName;
        // packagefragment Labels
        // hierarchical Layout
        fragmentName = hierarchyProvider.getText(subPackage);
        assertEquals("subpackage", fragmentName);
        fragmentName = hierarchyProvider.getText(subsubPackage);
        assertEquals("subsubpackage", fragmentName);
        fragmentName = hierarchyProvider.getText(empty);
        assertEquals("emptypackage", fragmentName);
        fragmentName = hierarchyProvider.getText(defaultPackage);
        assertEquals(Messages.ModelExplorer_defaultPackageLabel, fragmentName);
        // Flat Layout
        fragmentName = flatProvider.getText(subPackage);
        assertEquals("subpackage", fragmentName);
        fragmentName = flatProvider.getText(subsubPackage);
        assertEquals("subpackage.subsubpackage", fragmentName);
        fragmentName = flatProvider.getText(empty);
        assertEquals("subpackage.subsubpackage.emptypackage", fragmentName);
        fragmentName = flatProvider.getText(defaultPackage);
        assertEquals(Messages.ModelExplorer_defaultPackageLabel, fragmentName);

        // other types: returned String equals getName()
        String name = hierarchyProvider.getText(proj);
        assertEquals(proj.getName(), name);
        name = hierarchyProvider.getText(root);
        assertEquals(root.getName(), name);
        name = hierarchyProvider.getText(polCmptType);
        assertEquals(polCmptType.getName(), name);

        name = flatProvider.getText(proj);
        assertEquals(proj.getName(), name);
        name = flatProvider.getText(root);
        assertEquals(root.getName(), name);
        name = flatProvider.getText(polCmptType);
        assertEquals(polCmptType.getName(), name);

        // IResources
        String resName = hierarchyProvider.getText(folder);
        assertEquals(folder.getName(), resName);
        resName = hierarchyProvider.getText(file);
        assertEquals(file.getName(), resName);
        resName = hierarchyProvider.getText(subFolder);
        assertEquals(subFolder.getName(), resName);

        resName = flatProvider.getText(folder);
        assertEquals(folder.getName(), resName);
        resName = flatProvider.getText(file);
        assertEquals(file.getName(), resName);
        resName = flatProvider.getText(subFolder);
        assertEquals(subFolder.getName(), resName);

        // non ips projects in model explorer
        IProject platformProject = newPlatformProject("PlatformProject");

        resName = hierarchyProvider.getText(platformProject);
        assertEquals(platformProject.getName() + " (" + Messages.ModelExplorer_nonIpsProjectLabel + ")", resName);
        resName = flatProvider.getText(platformProject);
        assertEquals(platformProject.getName() + " (" + Messages.ModelExplorer_nonIpsProjectLabel + ")", resName);

        assertEquals(CONTAINER_NAME, flatProvider.getText(container));
        assertEquals(CONTAINER_NAME, hierarchyProvider.getText(container));

        assertEquals(REFERENCED_PROJECT, flatProvider.getText(projectViewItem));
        assertEquals(REFERENCED_PROJECT, hierarchyProvider.getText(projectViewItem));

        // non ips projects in product definition explorer
        hierarchyProvider.setProductDefinitionLabelProvider(true);
        flatProvider.setProductDefinitionLabelProvider(true);
        resName = hierarchyProvider.getText(platformProject);
        assertEquals(platformProject.getName() + " (" + Messages.ModelLabelProvider_noProductDefinitionProjectLabel
                + ")", resName);
        resName = flatProvider.getText(platformProject);
        assertEquals(platformProject.getName() + " (" + Messages.ModelLabelProvider_noProductDefinitionProjectLabel
                + ")", resName);

        name = hierarchyProvider.getText(proj);
        assertEquals(proj.getName() + " (" + Messages.ModelLabelProvider_noProductDefinitionProjectLabel + ")", name);

        name = flatProvider.getText(proj);
        assertEquals(proj.getName() + " (" + Messages.ModelLabelProvider_noProductDefinitionProjectLabel + ")", name);
    }

    @Test
    public void testAddListener() {
        // no tests
    }

    @Test
    public void testDispose() {
        // no tests
    }

    @Test
    public void testIsLabelProperty() {
        // no tests
    }

    @Test
    public void testRemoveListener() {
        // no tests
    }

}
