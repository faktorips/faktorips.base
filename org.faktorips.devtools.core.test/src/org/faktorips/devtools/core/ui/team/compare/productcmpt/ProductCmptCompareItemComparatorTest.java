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

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.ITableContentUsage;

public class ProductCmptCompareItemComparatorTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();
    
    private IProductCmptGeneration generation1;
    
    private ProductCmptCompareItem compareItemRoot;
    
    private IConfigElement formula;
    private IConfigElement policyAttribute;
    private IConfigElement productAttribute;
    private ITableContentUsage tableUsage;
    private IProductCmptRelation relation1;
    private IProductCmptRelation relation2;
    private IProductCmptRelation relation3;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject proj= (IpsProject)newIpsProject("TestProject");
        IIpsPackageFragmentRoot root = proj.getIpsPackageFragmentRoots()[0];
        IProductCmpt product = newProductCmpt(root, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(root, "TestProductCmptReferenced");
        IProductCmpt productReferenced2 = newProductCmpt(root, "TestProductCmptReferenced2");
        
        GregorianCalendar calendar= new GregorianCalendar();
        generation1 = (IProductCmptGeneration) product.newGeneration(calendar);
        calendar= new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        product.newGeneration(calendar);
        calendar= new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        product.newGeneration(calendar);

        formula = generation1.newConfigElement();
        formula.setType(ConfigElementType.FORMULA);
        policyAttribute = generation1.newConfigElement();
        policyAttribute.setType(ConfigElementType.POLICY_ATTRIBUTE);
        productAttribute = generation1.newConfigElement();
        productAttribute.setType(ConfigElementType.PRODUCT_ATTRIBUTE);
        tableUsage= generation1.newTableContentUsage();
        tableUsage.setTableContentName("TestTableContents");
        
        relation1 = generation1.newRelation("RelationType");
        relation1.setTarget(productReferenced.getQualifiedName());
        relation2 = generation1.newRelation("OtherRelationType");
        relation2.setTarget(productReferenced2.getQualifiedName());
        relation3 = generation1.newRelation("RelationType", relation1);
        relation3.setTarget(productReferenced2.getQualifiedName());
        
        IFile correspondingFile = product.getIpsSrcFile().getCorrespondingFile();
        compareItemRoot = (ProductCmptCompareItem) structureCreator.getStructure(new ResourceNode(correspondingFile));
    }
    
    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.ProductCmptCompareItemComparator.compare(Object, Object)'
     */
    public void testCompare() {
        ProductCmptCompareItemComparator comparator= new ProductCmptCompareItemComparator();

        Object[] children= compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem= (ProductCmptCompareItem) children[0];
        
        // generations
        children= compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemGen2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemGen3= (ProductCmptCompareItem) children[2];
        
        assertEquals(-1, comparator.compare(compareItemGen1, compareItemGen2));
        assertEquals(-1, comparator.compare(compareItemGen2, compareItemGen3));
        assertEquals(-2, comparator.compare(compareItemGen1, compareItemGen3));
        assertEquals(2, comparator.compare(compareItemGen3, compareItemGen1));
        assertEquals(0, comparator.compare(compareItemGen1, compareItemGen1));

        // relations and attributes
        children= compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemAttribute1= (ProductCmptCompareItem) children[0];
        ProductCmptCompareItem compareItemAttribute2= (ProductCmptCompareItem) children[1];
        ProductCmptCompareItem compareItemAttribute3= (ProductCmptCompareItem) children[2];
        ProductCmptCompareItem compareItemAttribute4= (ProductCmptCompareItem) children[3];
        ProductCmptCompareItem compareItemRelation1= (ProductCmptCompareItem) children[4];
        ProductCmptCompareItem compareItemRelation2= (ProductCmptCompareItem) children[5];
        ProductCmptCompareItem compareItemRelation3= (ProductCmptCompareItem) children[6];
        
        // attributes are sorted by type: productAttribute, tableUsage, formula, policyAttribute
        assertEquals(compareItemAttribute1.getIpsElement(), productAttribute);
        assertEquals(compareItemAttribute2.getIpsElement(), tableUsage);
        assertEquals(compareItemAttribute3.getIpsElement(), formula);
        assertEquals(compareItemAttribute4.getIpsElement(), policyAttribute);
        
        // maintain order of relations as defined at creation (rel3 inserted before rel1) 
        assertEquals(compareItemRelation1.getIpsElement(), relation3);
        assertEquals(compareItemRelation2.getIpsElement(), relation1);
        assertEquals(compareItemRelation3.getIpsElement(), relation2);
    }

}
