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
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

public class ModelExplorerConfigurationTest extends AbstractIpsPluginTest {

    private IIpsProject proj;
    
    private PolicyCmptType pcType;
    private IProductCmpt prodCmpt;
    private IIpsPackageFragmentRoot root;
    private IAttribute attribute;
    private IRelation relation;
    private ITableContents tableContents;
    private ITableStructure tableStructure;
    
    private ModelExplorerConfiguration configTypes;

    private IFolder folder;

    private IFile file;

    private IIpsPackageFragment defaultPackage;
    

    protected void setUp() throws Exception {
        super.setUp();
        proj = newIpsProject("Testprojekt");
        
        root = proj.getIpsPackageFragmentRoots()[0];
        defaultPackage = root.getIpsDefaultPackageFragment();
        pcType = newPolicyCmptType(root, "TestPCType");
        attribute = pcType.newAttribute();
        relation = pcType.newRelation();
        prodCmpt = newProductCmpt(root, "TestProdCmpt");
        tableContents = (ITableContents) newIpsObject(root.getIpsDefaultPackageFragment(), IpsObjectType.TABLE_CONTENTS, "TestTableContents");
        tableStructure = (ITableStructure) newIpsObject(root.getIpsDefaultPackageFragment(), IpsObjectType.TABLE_STRUCTURE, "TestTableStructure");
        
        configTypes = new ModelExplorerConfiguration(new Class[] { IPolicyCmptType.class, IProductCmpt.class,
                        IAttribute.class, IRelation.class }, new Class[]{IFolder.class});

        folder = ((IProject)proj.getCorrespondingResource()).getFolder("testfolder");
        folder.create(true, false, null);
        file = folder.getFile("test.txt");
        file.create(null, true, null);
        
    }
    
    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration.isAllowedIpsElementType(IIpsElement)'
     */
    public void testIsAllowedIpsElement() {
        assertTrue(configTypes.isAllowedIpsElement(proj));
        assertTrue(configTypes.isAllowedIpsElement(root));
        assertTrue(configTypes.isAllowedIpsElement(defaultPackage));
        assertTrue(configTypes.isAllowedIpsElement(pcType));
        assertTrue(configTypes.isAllowedIpsElement(prodCmpt));
        assertTrue(configTypes.isAllowedIpsElement(attribute));
        assertTrue(configTypes.isAllowedIpsElement(relation));
        assertFalse(configTypes.isAllowedIpsElement(tableContents));
        assertFalse(configTypes.isAllowedIpsElement(tableStructure));
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration.isAllowedIpsElementType(Class)'
     */
    public void testIsAllowedIpsElementType() {
        assertTrue(configTypes.isAllowedIpsElementType(proj.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(root.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(defaultPackage.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(pcType.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(prodCmpt.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(attribute.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(relation.getClass()));
        assertFalse(configTypes.isAllowedIpsElementType(tableContents.getClass()));
        assertFalse(configTypes.isAllowedIpsElementType(tableStructure.getClass()));
    }
    public void testIsAllowedResource(){
        assertTrue(configTypes.isAllowedResource(folder));
        assertFalse(configTypes.isAllowedResource(file));

        assertFalse(configTypes.isAllowedResource(proj));
        assertFalse(configTypes.isAllowedResource(root));
        assertFalse(configTypes.isAllowedResource(defaultPackage));
    }
    public void testIsAllowedResourceType(){
        assertTrue(configTypes.isAllowedResourceType(folder.getClass()));
        assertFalse(configTypes.isAllowedResourceType(file.getClass()));
        assertFalse(configTypes.isAllowedResourceType(IProject.class));
        
        assertFalse(configTypes.isAllowedResourceType(proj.getClass()));
        assertFalse(configTypes.isAllowedResourceType(root.getClass()));
        assertFalse(configTypes.isAllowedResourceType(defaultPackage.getClass()));
    }

}
