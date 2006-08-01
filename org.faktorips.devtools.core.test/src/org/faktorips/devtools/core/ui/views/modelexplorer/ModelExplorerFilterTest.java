/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

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

public class ModelExplorerFilterTest extends AbstractIpsPluginTest {

    private IIpsProject proj;
    private IIpsProject projModel;
    private IIpsProject projProduct;
    private IIpsProject projNoFlags;
    
    private PolicyCmptType pcType;
    private IProductCmpt prodCmpt;
    private IIpsPackageFragmentRoot root;
    
    private ModelExplorerFilter typesFilter;
    private ModelExplorerFilter modelProjFilter;
    private ModelExplorerFilter productProjFilter;
    private ModelExplorerFilter allProjFilter;
    private ModelExplorerFilter nonMPProjFilter;
    private ModelExplorerFilter nonProductProjFilter;
    
    private IAttribute attribute;
    private IRelation relation;
    private ITableContents tableContents;
    private ITableStructure tableStructure;
    

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
        
        ModelExplorerConfiguration configTypes = new ModelExplorerConfiguration(new Class[] { PolicyCmptType.class, ProductCmpt.class,
                Attribute.class, Relation.class }, new Class[0], 0);
        ModelExplorerConfiguration configModel = new ModelExplorerConfiguration(new Class[0], new Class[0],
                ModelExplorerConfiguration.ALLOW_MODEL_PROJECTS);
        ModelExplorerConfiguration configProduct = new ModelExplorerConfiguration(new Class[0], new Class[0],
                ModelExplorerConfiguration.ALLOW_PRODUCTDEFINITION_PROJECTS);
        ModelExplorerConfiguration configAll = new ModelExplorerConfiguration(new Class[0], new Class[0],
                ModelExplorerConfiguration.ALLOW_PRODUCTDEFINITION_PROJECTS
                        | ModelExplorerConfiguration.ALLOW_MODEL_PROJECTS
                        | ModelExplorerConfiguration.ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
        ModelExplorerConfiguration configNonMP = new ModelExplorerConfiguration(new Class[0], new Class[0],
                ModelExplorerConfiguration.ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
        ModelExplorerConfiguration configNonProduct = new ModelExplorerConfiguration(new Class[0], new Class[0],
                ModelExplorerConfiguration.ALLOW_MODEL_PROJECTS 
                        | ModelExplorerConfiguration.ALLOW_NONMODEL_NONPRODUCTDEFINTION_PROJECTS);
        
        typesFilter = new ModelExplorerFilter(configTypes);
        modelProjFilter = new ModelExplorerFilter(configModel);
        productProjFilter = new ModelExplorerFilter(configProduct);
        allProjFilter = new ModelExplorerFilter(configAll);
        nonMPProjFilter = new ModelExplorerFilter(configNonMP);
        nonProductProjFilter = new ModelExplorerFilter(configNonProduct);
    }

    public void testSelect() {
        // test allowed types
        assertTrue(typesFilter.select(null, null, pcType));
        assertTrue(typesFilter.select(null, null, prodCmpt));
        assertTrue(typesFilter.select(null, null, attribute));
        assertTrue(typesFilter.select(null, null, relation));
        assertFalse(typesFilter.select(null, null, tableContents));
        assertFalse(typesFilter.select(null, null, tableStructure));

        // test for model-property
        assertTrue(modelProjFilter.select(null, null, proj));
        assertTrue(modelProjFilter.select(null, null, projModel));
        assertFalse(modelProjFilter.select(null, null, projProduct));
        assertFalse(modelProjFilter.select(null, null, projNoFlags));

        // test for productdefinition-property
        assertTrue(productProjFilter.select(null, null, proj));
        assertFalse(productProjFilter.select(null, null, projModel));
        assertTrue(productProjFilter.select(null, null, projProduct));
        assertFalse(productProjFilter.select(null, null, projNoFlags));
        
        // test for all projects
        assertTrue(allProjFilter.select(null, null, proj));
        assertTrue(allProjFilter.select(null, null, projModel));
        assertTrue(allProjFilter.select(null, null, projProduct));
        assertTrue(allProjFilter.select(null, null, projNoFlags));

        // test for nonmodel nonproductdefintion projects
        assertFalse(nonMPProjFilter.select(null, null, proj));
        assertFalse(nonMPProjFilter.select(null, null, projModel));
        assertFalse(nonMPProjFilter.select(null, null, projProduct));
        assertTrue(nonMPProjFilter.select(null, null, projNoFlags));
        
        // test for nonproductdefintion projects
        assertTrue(nonProductProjFilter.select(null, null, proj));
        assertTrue(nonProductProjFilter.select(null, null, projModel));
        assertFalse(nonProductProjFilter.select(null, null, projProduct));
        assertTrue(nonProductProjFilter.select(null, null, projNoFlags));
    }

    private void setProjectProperty(IIpsProject project, boolean model, boolean product) throws CoreException {
        IIpsProjectProperties props= project.getProperties();
        props.setModelProject(model);
        props.setProductDefinitionProject(product);
        project.setProperties(props);
    }
}
