/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.internal.resources.WorkspaceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

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
    protected void setUp() throws Exception {
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

        List<IpsObjectType> allowedTypes = new ArrayList<IpsObjectType>(Arrays.asList(IpsPlugin.getDefault()
                .getIpsModel().getIpsObjectTypes()));
        // config should not support TableStructure and TableContents
        allowedTypes.remove(IpsObjectType.TABLE_STRUCTURE);
        allowedTypes.remove(IpsObjectType.TABLE_CONTENTS);
        config = new ModelExplorerConfiguration(allowedTypes.toArray(new IpsObjectType[0]));

        folder = ((IProject)proj.getCorrespondingResource()).getFolder("testfolder");
        folder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);
        failRessource = new WorkspaceRoot(Path.ROOT, null) {

        };

    }

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

    public void testIsAllowedIpsElementType() {
        assertTrue(config.isAllowedIpsElementType(pcType.getIpsObjectType()));
        assertTrue(config.isAllowedIpsElementType(prodCmpt.getIpsObjectType()));
        assertFalse(config.isAllowedIpsElementType(tableContents.getIpsObjectType()));
        assertFalse(config.isAllowedIpsElementType(tableStructure.getIpsObjectType()));
    }

    public void testIsAllowedResource() {
        assertTrue(config.isAllowedResource(folder));
        assertTrue(config.isAllowedResource(file));
        assertFalse(config.isAllowedResource(failRessource));
    }

    public void testIsAllowedResourceType() {
        assertTrue(config.isAllowedResourceType(folder.getClass()));
        assertTrue(config.isAllowedResourceType(file.getClass()));
        assertTrue(config.isAllowedResourceType(proj.getCorrespondingResource().getClass()));
        assertFalse(config.isAllowedResourceType(failRessource.getClass()));
    }

    public void testRepresentsProject() {
        assertTrue(config.representsProject(proj));
        assertTrue(config.representsProject(proj.getCorrespondingResource()));
        assertFalse(config.representsProject(folder));
        assertFalse(config.representsProject(root));
        assertFalse(config.representsProject(defaultPackage));
        assertFalse(config.representsProject(file));
        assertFalse(config.representsProject(pcType));
    }

    public void testRepresentsFolder() {
        assertFalse(config.representsFolder(proj));
        assertFalse(config.representsFolder(proj.getCorrespondingResource()));
        assertTrue(config.representsFolder(folder));
        assertTrue(config.representsFolder(root));
        assertTrue(config.representsFolder(defaultPackage));
        assertFalse(config.representsFolder(file));
        assertFalse(config.representsFolder(pcType));
    }

    public void testRepresentsFile() throws CoreException {
        assertFalse(config.representsFile(proj));
        assertFalse(config.representsFile(proj.getCorrespondingResource()));
        assertFalse(config.representsFile(folder));
        assertFalse(config.representsFile(root));
        assertFalse(config.representsFile(defaultPackage));
        assertTrue(config.representsFile(file));
        assertTrue(config.representsFile(pcType));

        IIpsObjectPath path = proj.getIpsObjectPath();
        IFile file = proj.getProject().getFile("Archive.ipsar");
        file.create(new ByteArrayInputStream("".getBytes()), true, null);
        path.newArchiveEntry(proj.getProject().getFile("Archive.ipsar").getLocation());
        proj.setIpsObjectPath(path);
        IIpsPackageFragmentRoot archiveRoot = proj.findIpsPackageFragmentRoot("Archive.ipsar");
        assertTrue(archiveRoot.isBasedOnIpsArchive());
        assertTrue(config.representsFile(archiveRoot));
    }
}
