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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.internal.resources.WorkspaceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.eclipse.AEclipseProject;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
// suppress the warning because of using WorkspaceRoot
public class ModelExplorerConfigurationTest extends AbstractIpsPluginTest {

    private IIpsProject proj;

    private PolicyCmptType pcType;
    private IProductCmpt prodCmpt;
    private IIpsPackageFragmentRoot root;
    private IPolicyCmptTypeAttribute attribute;
    private IPolicyCmptTypeAssociation relation;
    private ITableContents tableContents;
    private ITableStructure tableStructure;

    private ModelExplorerConfiguration config;

    private IFolder folder;

    private IFile file;

    private IResource failRessource;

    private IIpsPackageFragment defaultPackage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        proj = newIpsProject("Testprojekt");

        root = proj.getIpsPackageFragmentRoots()[0];
        defaultPackage = root.getDefaultIpsPackageFragment();
        pcType = newPolicyCmptType(root, "TestPCType");
        attribute = pcType.newPolicyCmptTypeAttribute();
        relation = pcType.newPolicyCmptTypeAssociation();
        prodCmpt = newProductCmpt(root, "TestProdCmpt");
        tableContents = (ITableContents)newIpsObject(root.getDefaultIpsPackageFragment(), IpsObjectType.TABLE_CONTENTS,
                "TestTableContents");
        tableStructure = (ITableStructure)newIpsObject(root.getDefaultIpsPackageFragment(),
                IpsObjectType.TABLE_STRUCTURE, "TestTableStructure");
        List<IpsObjectType> allowedTypes = new ArrayList<>(
                Arrays.asList(IIpsModel.get().getIpsObjectTypes()));
        // config should not support TableStructure and TableContents
        allowedTypes.remove(IpsObjectType.TABLE_STRUCTURE);
        allowedTypes.remove(IpsObjectType.TABLE_CONTENTS);
        config = new ModelExplorerConfiguration(allowedTypes.toArray(new IpsObjectType[0]));

        folder = ((AEclipseProject)proj.getCorrespondingResource()).getFolder("testfolder").unwrap();
        folder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);
        failRessource = new WorkspaceRoot(Path.ROOT, null) {

        };

    }

    @Test
    public void testShouldDisplayChildrenFor() {
        assertTrue(config.shouldDisplayChildrenFor(IpsObjectType.POLICY_CMPT_TYPE));
        assertTrue(config.shouldDisplayChildrenFor(IpsObjectType.PRODUCT_CMPT_TYPE));
        assertTrue(config.shouldDisplayChildrenFor(IpsObjectType.PRODUCT_CMPT));
        assertTrue(config.shouldDisplayChildrenFor(IpsObjectType.PRODUCT_TEMPLATE));
        assertTrue(config.shouldDisplayChildrenFor(IpsObjectType.IPS_SOURCE_FILE));
        assertTrue(config.shouldDisplayChildrenFor(IpsObjectType.ENUM_TYPE));
        assertTrue(config.shouldDisplayChildrenFor(IpsObjectType.TABLE_STRUCTURE));
        assertFalse(config.shouldDisplayChildrenFor(IpsObjectType.ENUM_CONTENT));
        assertFalse(config.shouldDisplayChildrenFor(IpsObjectType.TABLE_CONTENTS));
        assertFalse(config.shouldDisplayChildrenFor(IpsObjectType.TEST_CASE));
        assertFalse(config.shouldDisplayChildrenFor(IpsObjectType.TEST_CASE_TYPE));
    }

    @Test
    public void testIsAllowedIpsElement() {
        assertTrue(config.isAllowedIpsElement(proj));
        assertTrue(config.isAllowedIpsElement(root));
        assertTrue(config.isAllowedIpsElement(defaultPackage));
        assertTrue(config.isAllowedIpsElement(pcType));
        assertTrue(config.isAllowedIpsElement(prodCmpt));
        assertTrue(config.isAllowedIpsElement(attribute));
        assertTrue(config.isAllowedIpsElement(relation));
        assertFalse(config.isAllowedIpsElement(tableContents));
        assertFalse(config.isAllowedIpsElement(tableStructure));
    }

    @Test
    public void testIsAllowedIpsElementType() {
        assertTrue(config.isAllowedIpsElementType(pcType.getIpsObjectType()));
        assertTrue(config.isAllowedIpsElementType(prodCmpt.getIpsObjectType()));
        assertFalse(config.isAllowedIpsElementType(tableContents.getIpsObjectType()));
        assertFalse(config.isAllowedIpsElementType(tableStructure.getIpsObjectType()));
    }

    @Test
    public void testIsAllowedResource() {
        assertTrue(config.isAllowedResource(folder));
        assertTrue(config.isAllowedResource(file));
        assertFalse(config.isAllowedResource(failRessource));
    }

    @Test
    public void testIsAllowedResourceType() {
        assertTrue(config.isAllowedResourceType(folder.getClass()));
        assertTrue(config.isAllowedResourceType(file.getClass()));
        assertTrue(config.isAllowedResourceType(((IResource)proj.getCorrespondingResource().unwrap()).getClass()));
        assertFalse(config.isAllowedResourceType(failRessource.getClass()));
    }

    @Test
    public void testRepresentsProject() {
        assertTrue(config.representsProject(proj));
        assertTrue(config.representsProject(proj.getCorrespondingResource().unwrap()));
        assertFalse(config.representsProject(folder));
        assertFalse(config.representsProject(root));
        assertFalse(config.representsProject(defaultPackage));
        assertFalse(config.representsProject(file));
        assertFalse(config.representsProject(pcType));
    }

    @Test
    public void testRepresentsFolder() {
        assertFalse(config.representsFolder(proj));
        assertFalse(config.representsFolder(proj.getCorrespondingResource().unwrap()));
        assertTrue(config.representsFolder(folder));
        assertTrue(config.representsFolder(root));
        assertTrue(config.representsFolder(defaultPackage));
        assertFalse(config.representsFolder(file));
        assertFalse(config.representsFolder(pcType));
    }

    @Test
    public void testRepresentsFile() throws CoreRuntimeException {
        assertFalse(config.representsFile(proj));
        assertFalse(config.representsFile(proj.getCorrespondingResource().unwrap()));
        assertFalse(config.representsFile(folder));
        assertFalse(config.representsFile(root));
        assertFalse(config.representsFile(defaultPackage));
        assertTrue(config.representsFile(file));
        assertTrue(config.representsFile(pcType));

        IIpsObjectPath path = proj.getIpsObjectPath();
        AFile file = proj.getProject().getFile("Archive.ipsar");
        file.create(new ByteArrayInputStream("".getBytes()), null);
        path.newArchiveEntry(proj.getProject().getFile("Archive.ipsar").getLocation());
        proj.setIpsObjectPath(path);
        IIpsPackageFragmentRoot archiveRoot = proj.findIpsPackageFragmentRoot("Archive.ipsar");
        assertTrue(archiveRoot.isBasedOnIpsArchive());
        assertTrue(config.representsFile(archiveRoot));
    }
}
