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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.enums.EnumContent;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Stefan Widmaier
 */
public class ModelContentProviderTest extends AbstractIpsPluginTest {

    private ModelExplorerConfiguration config;

    private ModelContentProvider flatProvider;
    private ModelContentProvider hierarchyProvider;

    private IIpsProject proj;
    private IIpsProject modelProj;
    private IIpsProject prodDefProj;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragmentRoot emptyRoot;
    private IIpsPackageFragment defaultPackage;
    private IIpsPackageFragment subPackage;
    private IIpsPackageFragment modelPackage;
    private IIpsPackageFragment productPackage;
    private IIpsPackageFragment emptyPackage;
    private IIpsPackageFragment filePackage;
    private IPolicyCmptType polCmptType;
    private IPolicyCmptType polCmptType2;
    private IProductCmpt prodCmpt;
    private ITableContents tableContents;
    private ITableStructure tableStructure;

    private IFolder folder;
    private IFolder subFolder;
    private IFile file;

    private IPolicyCmptTypeAttribute attribute;

    private IIpsPackageFragmentRoot modelRoot;
    private IIpsPackageFragment modelDefaultPackage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<IpsObjectType> allowedTypes = new ArrayList<>(
                Arrays.asList(IIpsModel.get().getIpsObjectTypes()));
        // The tests seems to expect that product component type is not a allowed type
        allowedTypes.remove(IpsObjectType.PRODUCT_CMPT_TYPE);
        config = new ModelExplorerConfiguration(allowedTypes.toArray(new IpsObjectType[0]));

        flatProvider = new ModelContentProvider(config, LayoutStyle.FLAT);
        hierarchyProvider = new ModelContentProvider(config, LayoutStyle.HIERACHICAL);

        proj = newIpsProject("TestProject");
        setModelProjectProperty(proj, true);
        modelProj = newIpsProject("TestModelProject");
        setModelProjectProperty(modelProj, true);
        prodDefProj = newIpsProject("TestProductDefinitionProject");
        setModelProjectProperty(prodDefProj, false);

        // Content of proj
        root = proj.getIpsPackageFragmentRoots()[0];
        emptyRoot = prodDefProj.getIpsPackageFragmentRoots()[0];
        defaultPackage = root.getDefaultIpsPackageFragment();
        subPackage = root.createPackageFragment("subpackage", true, null);
        modelPackage = root.createPackageFragment("subpackage.model", true, null);
        productPackage = root.createPackageFragment("subpackage.product", true, null);
        emptyPackage = root.createPackageFragment("subpackage.model.emptypackage", true, null);
        filePackage = root.createPackageFragment("subpackage.files", true, null);
        // create files for filepackage
        IFolder packageFolder = (IFolder)filePackage.getCorrespondingResource();
        IFile packageFile = packageFolder.getFile(new Path("testfile.txt"));
        packageFile.create(null, true, null);

        polCmptType = newPolicyCmptType(root, "subpackage.model.TestPolicy");
        attribute = polCmptType.newPolicyCmptTypeAttribute();
        polCmptType.newPolicyCmptTypeAttribute();
        polCmptType.newPolicyCmptTypeAssociation();
        tableStructure = (ITableStructure)newIpsObject(root, IpsObjectType.TABLE_STRUCTURE,
                "subpackage.model.TestTableStructure");
        prodCmpt = newProductCmpt(root, "subpackage.product.TestProductComponent");
        tableContents = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS,
                "subpackage.product.TestTableContents");
        polCmptType2 = newPolicyCmptType(root, "TestPolicy2"); // defaultpackage

        folder = ((IProject)proj.getCorrespondingResource()).getFolder("testfolder");
        folder.create(true, false, null);
        subFolder = folder.getFolder("subfolder");
        subFolder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);

        // contents of modelProj
        modelRoot = modelProj.getIpsPackageFragmentRoots()[0];
        modelDefaultPackage = modelRoot.getDefaultIpsPackageFragment();

    }

    private void setModelProjectProperty(IIpsProject project, boolean b) throws CoreRuntimeException {
        IIpsProjectProperties props = project.getProperties();
        props.setModelProject(b);
        project.setProperties(props);
    }

    @Test
    public void testGetChildren() throws CoreRuntimeException {
        List<Object> list;
        // --- Tests for hierarchical layout style ---
        // Proj contains only the packageFragmentRoot and a folder,
        // hidden files (.project) and java output folders and classpath entries are ignored, except
        // for the .ipsproject file
        Object[] children = flatProvider.getChildren(proj);
        assertEquals(3, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(root));
        assertTrue(list.contains(folder));
        assertTrue(list.contains(((IProject)proj.getCorrespondingResource()).getFile(".ipsproject")));
        // root has two children: the defaultpackage and subPackage
        children = hierarchyProvider.getChildren(root);
        assertEquals(2, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(defaultPackage));
        assertTrue(list.contains(subPackage));
        // subPackage has two children modelPackage and productPackage
        children = hierarchyProvider.getChildren(subPackage);
        assertEquals(3, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(modelPackage));
        assertTrue(list.contains(productPackage));
        assertTrue(list.contains(filePackage));
        // modelPackage has three children, emptyPackage, one policyCmptType and one TableStructure
        children = hierarchyProvider.getChildren(modelPackage);
        assertEquals(3, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(emptyPackage));
        assertTrue(list.contains(polCmptType.getIpsSrcFile()));
        assertTrue(list.contains(tableStructure.getIpsSrcFile()));
        // productPackage has two children: one productCmpt and one tableContent
        children = hierarchyProvider.getChildren(productPackage);
        assertEquals(2, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(prodCmpt.getIpsSrcFile()));
        assertTrue(list.contains(tableContents.getIpsSrcFile()));
        // emptyPackage has no children
        assertEquals(0, hierarchyProvider.getChildren(emptyPackage).length);
        // filePackage contains a file
        assertEquals(1, flatProvider.getChildren(filePackage).length);

        // polCmptType contains two attributes and one relation
        // TODO possible racing condition: ipsObject/IpsSrcfile is not yet updated with new
        // attributes (setUp()) when test is run
        children = hierarchyProvider.getChildren(polCmptType);
        assertEquals(3, children.length);
        // Attributes have no children
        assertEquals(0, hierarchyProvider.getChildren(attribute).length);
        // ProductCmpts have no children
        assertEquals(0, hierarchyProvider.getChildren(prodCmpt).length);

        // --- Tests for flat layout ---
        children = flatProvider.getChildren(proj);
        assertEquals(3, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(root));
        assertTrue(list.contains(folder));
        assertTrue(list.contains(((IProject)proj.getCorrespondingResource()).getFile(".ipsproject")));
        // Packages that contain no files but folders are ignored (subpackage).
        children = flatProvider.getChildren(root);
        assertEquals(5, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(defaultPackage));
        assertTrue(list.contains(modelPackage));
        assertTrue(list.contains(productPackage));
        assertTrue(list.contains(emptyPackage));
        assertTrue(list.contains(filePackage));
        // defaultpackage is ignored, if empty
        children = flatProvider.getChildren(modelRoot);
        assertEquals(0, children.length);
        list = Arrays.asList(children);
        assertFalse(list.contains(modelDefaultPackage));

        // subPackage contains no children
        assertEquals(0, flatProvider.getChildren(subPackage).length);
        // modelPackage contains two children, one PolicyCmptTypes, one TableStructure
        children = flatProvider.getChildren(modelPackage);
        assertEquals(2, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(polCmptType.getIpsSrcFile()));
        assertTrue(list.contains(tableStructure.getIpsSrcFile()));
        // productPackage contains two children, one ProductComponent, one TableContents
        children = flatProvider.getChildren(productPackage);
        assertEquals(2, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(prodCmpt.getIpsSrcFile()));
        assertTrue(list.contains(tableContents.getIpsSrcFile()));
        // emptyPackage has no children
        assertEquals(0, flatProvider.getChildren(emptyPackage).length);
        // filePackage contains a file
        assertEquals(1, flatProvider.getChildren(filePackage).length);

        // polCmptType contains two attributes and one relation
        children = flatProvider.getChildren(polCmptType);
        assertEquals(3, children.length);
        // Attributes have no children
        assertEquals(0, flatProvider.getChildren(attribute).length);
        // ProductCmpts have no children
        assertEquals(0, flatProvider.getChildren(prodCmpt).length);

        // test for IResources
        children = hierarchyProvider.getChildren(folder);
        assertEquals(2, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(subFolder));
        assertTrue(list.contains(file));
        children = flatProvider.getChildren(folder);
        assertEquals(2, children.length);
        list = Arrays.asList(children);
        assertTrue(list.contains(subFolder));
        assertTrue(list.contains(file));

        // test for type with no children
        assertEquals(0, hierarchyProvider.getChildren(tableContents.getIpsSrcFile()).length);
        EnumContent enumContent = newEnumContent(root, "enumcontent");
        assertEquals(0, hierarchyProvider.getChildren(enumContent.getIpsSrcFile()).length);
        // test with allowed children
        assertEquals(3, hierarchyProvider.getChildren(polCmptType.getIpsSrcFile()).length);
    }

    @Test
    public void testGetParent() {
        Object parent;
        // tests for hierarchical layout
        // Test packageFragments for corresponding ressource. Tests for Object identity can not be
        // used as PFragments (handles for said ressources) are created during tests.
        parent = hierarchyProvider.getParent(emptyPackage);
        assertEquals(modelPackage, parent);
        parent = hierarchyProvider.getParent(modelPackage);
        assertEquals(subPackage, parent);
        // The default package, as opposed to other packages, is only a valid parent for files and
        // IpsObjects
        parent = hierarchyProvider.getParent(subPackage);
        assertEquals(root, parent);
        // tests for flat layout
        parent = flatProvider.getParent(emptyPackage);
        assertEquals(root, parent);
        parent = flatProvider.getParent(modelPackage);
        assertEquals(root, parent);
        parent = flatProvider.getParent(subPackage);
        assertEquals(root, parent);
        parent = flatProvider.getParent(defaultPackage);
        assertEquals(root, parent);

        // for all other types getParent() acts like IpsElement#getParent()
        parent = hierarchyProvider.getParent(proj);
        assertEquals(IIpsModel.get(), parent);
        parent = hierarchyProvider.getParent(root);
        assertEquals(proj, parent);
        parent = hierarchyProvider.getParent(polCmptType);
        assertEquals(modelPackage, parent);
        parent = hierarchyProvider.getParent(tableStructure);
        assertEquals(modelPackage, parent);
        parent = hierarchyProvider.getParent(prodCmpt);
        assertEquals(productPackage, parent);
        parent = hierarchyProvider.getParent(tableContents);
        assertEquals(productPackage, parent);
        parent = hierarchyProvider.getParent(polCmptType2);
        assertEquals(defaultPackage, parent);

        parent = flatProvider.getParent(proj);
        assertEquals(IIpsModel.get(), parent);
        parent = flatProvider.getParent(root);
        assertEquals(proj, parent);
        parent = flatProvider.getParent(polCmptType);
        assertEquals(modelPackage, parent);
        parent = flatProvider.getParent(tableStructure);
        assertEquals(modelPackage, parent);
        parent = flatProvider.getParent(prodCmpt);
        assertEquals(productPackage, parent);
        parent = flatProvider.getParent(tableContents);
        assertEquals(productPackage, parent);
        parent = flatProvider.getParent(polCmptType2);
        assertEquals(defaultPackage, parent);

        // test for IResources
        parent = hierarchyProvider.getParent(file);
        assertEquals(folder, parent);
        parent = hierarchyProvider.getParent(subFolder);
        assertEquals(folder, parent);

        parent = flatProvider.getParent(file);
        assertEquals(folder, parent);
        parent = flatProvider.getParent(subFolder);
        assertEquals(folder, parent);
    }

    @Test
    public void testHasChildren() {
        // hierarchical tests
        assertTrue(hierarchyProvider.hasChildren(proj));
        assertTrue(hierarchyProvider.hasChildren(modelProj));
        assertTrue(hierarchyProvider.hasChildren(root));
        // a PackageFragmentRoot in hierarchical layout returns the children (files) of the
        // defaultPackageFragment,
        // an empty PackageFragmentRoot thus returns no children
        assertFalse(hierarchyProvider.hasChildren(emptyRoot));

        assertTrue(hierarchyProvider.hasChildren(subPackage));
        assertTrue(hierarchyProvider.hasChildren(modelPackage));
        assertFalse(hierarchyProvider.hasChildren(emptyPackage));
        assertTrue(hierarchyProvider.hasChildren(polCmptType));
        assertFalse(hierarchyProvider.hasChildren(polCmptType2));
        assertFalse(hierarchyProvider.hasChildren(prodCmpt));

        // flat tests
        assertTrue(flatProvider.hasChildren(proj));
        assertTrue(flatProvider.hasChildren(modelProj));
        assertTrue(flatProvider.hasChildren(root));
        // an empty PFragmentRoot in flat layout returns no child if the default packageFragment is
        // empty
        assertFalse(flatProvider.hasChildren(emptyRoot));

        assertFalse(flatProvider.hasChildren(subPackage));
        assertTrue(flatProvider.hasChildren(modelPackage));
        assertFalse(flatProvider.hasChildren(emptyPackage));
        assertTrue(flatProvider.hasChildren(polCmptType));
        assertFalse(flatProvider.hasChildren(polCmptType2));
        assertFalse(flatProvider.hasChildren(prodCmpt));

        // test for IResources
        assertTrue(hierarchyProvider.hasChildren(folder));
        assertFalse(hierarchyProvider.hasChildren(subFolder));
        assertFalse(hierarchyProvider.hasChildren(file));

        assertTrue(flatProvider.hasChildren(folder));
        assertFalse(flatProvider.hasChildren(subFolder));
        assertFalse(flatProvider.hasChildren(file));
    }

    @Test
    public void testGetElements() throws CoreRuntimeException {
        ICoreRunnable runnable = $ -> {
            IProject project = newPlatformProject("TestJavaProject");
            addJavaCapabilities(project);
            IProject project2 = newPlatformProject("TestJavaProject2");
            addJavaCapabilities(project2);
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
        Object[] children = hierarchyProvider.getElements(IIpsModel.get());
        assertEquals(5, children.length);
        children = flatProvider.getElements(IIpsModel.get());
        assertEquals(5, children.length);

        flatProvider.setExcludeNoIpsProjects(true);
        children = flatProvider.getElements(IIpsModel.get());
        assertEquals(3, children.length);

        hierarchyProvider.setExcludeNoIpsProjects(true);
        children = flatProvider.getElements(IIpsModel.get());
        assertEquals(3, children.length);
    }

    @Test
    public void testDispose() {
        // no tests
    }

    @Test
    public void testInputChanged() {
        // no tests
    }

}
