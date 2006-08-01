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
import org.faktorips.devtools.core.internal.model.pctype.Attribute;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.pctype.Relation;
import org.faktorips.devtools.core.internal.model.product.ProductCmpt;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

public class ModelExplorerConfigurationTest extends AbstractIpsPluginTest {

    private IIpsProject proj;
    private IIpsProject projModel;
    private IIpsProject projProduct;
    private IIpsProject projNoFlags;
    
    private PolicyCmptType pcType;
    private IProductCmpt prodCmpt;
    private IIpsPackageFragmentRoot root;
    private IAttribute attribute;
    private IRelation relation;
    private ITableContents tableContents;
    private ITableStructure tableStructure;
    
    private ModelExplorerConfiguration configTypes;
    private ModelExplorerConfiguration configModel;
    private ModelExplorerConfiguration configProduct;
    private ModelExplorerConfiguration configAll;
    private ModelExplorerConfiguration configNonMP;
    private ModelExplorerConfiguration configNonProduct;
    private ModelExplorerConfiguration configNonModel;
    private ModelExplorerConfiguration configNone;
    

    protected void setUp() throws Exception {
        super.setUp();
        proj = newIpsProject("Testprojekt");
        setProjectProperty(proj, true, true);
        projModel = newIpsProject("TestprojektModel");
        setProjectProperty(projModel, true, false);
        projProduct = newIpsProject("TestprojektProduct");
        setProjectProperty(projProduct, false, true);
        projNoFlags = newIpsProject("TestprojektNoFlags");
        setProjectProperty(projNoFlags, false, false);
        
        root = proj.getIpsPackageFragmentRoots()[0];
        pcType = newPolicyCmptType(root, "TestPCType");
        attribute = pcType.newAttribute();
        relation = pcType.newRelation();
        prodCmpt = newProductCmpt(root, "TestProdCmpt");
        tableContents = (ITableContents) newIpsObject(root.getIpsDefaultPackageFragment(), IpsObjectType.TABLE_CONTENTS, "TestTableContents");
        tableStructure = (ITableStructure) newIpsObject(root.getIpsDefaultPackageFragment(), IpsObjectType.TABLE_STRUCTURE, "TestTableStructure");
        
        configTypes = new ModelExplorerConfiguration(new Class[] { PolicyCmptType.class, ProductCmpt.class,
                        Attribute.class, Relation.class }, new Class[0], 0);
        configModel = new ModelExplorerConfiguration(new Class[0], new Class[0],
                        ModelExplorerConfiguration.ALLOW_MODEL_PROJECTS);
        configProduct = new ModelExplorerConfiguration(new Class[0], new Class[0],
                        ModelExplorerConfiguration.ALLOW_PRODUCTDEFINITION_PROJECTS);
        configAll = new ModelExplorerConfiguration(new Class[0], new Class[0],
                        ModelExplorerConfiguration.ALLOW_PRODUCTDEFINITION_PROJECTS
                                | ModelExplorerConfiguration.ALLOW_MODEL_PROJECTS
                                | ModelExplorerConfiguration.ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
        configNonMP = new ModelExplorerConfiguration(new Class[0], new Class[0],
                        ModelExplorerConfiguration.ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
        configNonProduct = new ModelExplorerConfiguration(new Class[0], new Class[0],
                ModelExplorerConfiguration.ALLOW_MODEL_PROJECTS 
                        | ModelExplorerConfiguration.ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
        configNonModel = new ModelExplorerConfiguration(new Class[0], new Class[0],
                ModelExplorerConfiguration.ALLOW_PRODUCTDEFINITION_PROJECTS 
                        | ModelExplorerConfiguration.ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
        configNone = new ModelExplorerConfiguration(new Class[0], new Class[0], 0);
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
        assertTrue(configTypes.isAllowedIpsElementType(pcType));
        assertTrue(configTypes.isAllowedIpsElementType(prodCmpt));
        assertTrue(configTypes.isAllowedIpsElementType(attribute));
        assertTrue(configTypes.isAllowedIpsElementType(relation));
        assertFalse(configTypes.isAllowedIpsElementType(tableContents));
        assertFalse(configTypes.isAllowedIpsElementType(tableStructure));
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

    /*
     * Test method for 'org.faktorips.devtools.core.ui.views.modelexplorer.ModelExplorerConfiguration.isAllowedIpsProjectType(IIpsProject)'
     */
    public void testIsAllowedIpsProjectType() {
        // test model and productdefinition projects
        assertTrue(configAll.isAllowedIpsProjectType(proj));
        assertTrue(configAll.isAllowedIpsProjectType(projModel));
        assertTrue(configAll.isAllowedIpsProjectType(projProduct));
        assertTrue(configAll.isAllowedIpsProjectType(projNoFlags));

        // test for model-property
        assertTrue(configModel.isAllowedIpsProjectType(proj));
        assertTrue(configModel.isAllowedIpsProjectType(projModel));
        assertFalse(configModel.isAllowedIpsProjectType(projProduct));
        assertFalse(configModel.isAllowedIpsProjectType(projNoFlags));

        // test for productdefinition-property
        assertTrue(configProduct.isAllowedIpsProjectType(proj));
        assertFalse(configProduct.isAllowedIpsProjectType(projModel));
        assertTrue(configProduct.isAllowedIpsProjectType(projProduct));
        assertFalse(configProduct.isAllowedIpsProjectType(projNoFlags));
        
        // test for all projects
        assertTrue(configAll.isAllowedIpsProjectType(proj));
        assertTrue(configAll.isAllowedIpsProjectType(projModel));
        assertTrue(configAll.isAllowedIpsProjectType(projProduct));
        assertTrue(configAll.isAllowedIpsProjectType(projNoFlags));

        // test for nonmodel nonproductdefintion projects
        assertFalse(configNonMP.isAllowedIpsProjectType(proj));
        assertFalse(configNonMP.isAllowedIpsProjectType(projModel));
        assertFalse(configNonMP.isAllowedIpsProjectType(projProduct));
        assertTrue(configNonMP.isAllowedIpsProjectType(projNoFlags));

        // test for nonproductdefintion projects
        assertTrue(configNonProduct.isAllowedIpsProjectType(proj));
        assertTrue(configNonProduct.isAllowedIpsProjectType(projModel));
        assertFalse(configNonProduct.isAllowedIpsProjectType(projProduct));
        assertTrue(configNonProduct.isAllowedIpsProjectType(projNoFlags));

        // test for nonmodel projects
        assertTrue(configNonModel.isAllowedIpsProjectType(proj));
        assertFalse(configNonModel.isAllowedIpsProjectType(projModel));
        assertTrue(configNonModel.isAllowedIpsProjectType(projProduct));
        assertTrue(configNonModel.isAllowedIpsProjectType(projNoFlags));
        
        // test for no projects
        assertFalse(configNone.isAllowedIpsProjectType(proj));
        assertFalse(configNone.isAllowedIpsProjectType(projModel));
        assertFalse(configNone.isAllowedIpsProjectType(projProduct));
        assertFalse(configNone.isAllowedIpsProjectType(projNoFlags));
    }

}
