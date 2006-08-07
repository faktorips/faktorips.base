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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
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
    

    protected void setUp() throws Exception {
        super.setUp();
        proj = newIpsProject("Testprojekt");
        setProjectProperty(proj, true, true);
        
        root = proj.getIpsPackageFragmentRoots()[0];
        pcType = newPolicyCmptType(root, "TestPCType");
        attribute = pcType.newAttribute();
        relation = pcType.newRelation();
        prodCmpt = newProductCmpt(root, "TestProdCmpt");
        tableContents = (ITableContents) newIpsObject(root.getIpsDefaultPackageFragment(), IpsObjectType.TABLE_CONTENTS, "TestTableContents");
        tableStructure = (ITableStructure) newIpsObject(root.getIpsDefaultPackageFragment(), IpsObjectType.TABLE_STRUCTURE, "TestTableStructure");
        
        configTypes = new ModelExplorerConfiguration(new Class[] { IPolicyCmptType.class, IProductCmpt.class,
                        IAttribute.class, IRelation.class }, new Class[0]);
    }
    
    private void setProjectProperty(IIpsProject project, boolean model, boolean product) throws CoreException {
        IIpsProjectProperties props= project.getProperties();
        props.setModelProject(model);
        props.setProductDefinitionProject(product);
        project.setProperties(props);
    }
    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration.isAllowedIpsElementType(IIpsElement)'
     */
    public void testIsAllowedIpsElementTypeIIpsElement() {
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
    public void testIsAllowedIpsElementTypeClass() {
        assertTrue(configTypes.isAllowedIpsElementType(pcType.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(prodCmpt.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(attribute.getClass()));
        assertTrue(configTypes.isAllowedIpsElementType(relation.getClass()));
        assertFalse(configTypes.isAllowedIpsElementType(tableContents.getClass()));
        assertFalse(configTypes.isAllowedIpsElementType(tableStructure.getClass()));
    }

}
