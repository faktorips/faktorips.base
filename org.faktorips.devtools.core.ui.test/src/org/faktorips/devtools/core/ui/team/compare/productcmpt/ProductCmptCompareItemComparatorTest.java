/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptCompareItemComparatorTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();

    private IProductCmptGeneration generation1;

    private ProductCmptCompareItem compareItemRoot;

    private IConfiguredDefault configuredDefaultProd;

    private IFormula formula;
    private IConfiguredDefault configuredDefault1;
    private IConfiguredValueSet configuredValueSet1;
    private IConfiguredDefault configuredDefault2;
    private IConfiguredValueSet configuredValueSet2;
    private IAttributeValue attributeValue;
    private ITableContentUsage tableUsage;
    private IProductCmptLink relation1;
    private IProductCmptLink relation2;
    private IProductCmptLink relation3;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject proj = newIpsProject(new ArrayList<Locale>());

        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(proj, "policyCmptType", "productCmptType");
        IPolicyCmptTypeAttribute policyAttribute = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute1");
        IPolicyCmptTypeAttribute policyAttribute2 = policyCmptType.newPolicyCmptTypeAttribute("policyAttribute2");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(proj);

        // 3 products + 3 generations
        IProductCmpt product = newProductCmpt(productCmptType, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(productCmptType, "TestProductCmptReferenced");
        IProductCmpt productReferenced2 = newProductCmpt(productCmptType, "TestProductCmptReferenced2");

        // something for product
        configuredDefaultProd = product.newPropertyValue(policyAttribute, IConfiguredDefault.class);

        // + generation (product)
        GregorianCalendar calendar = new GregorianCalendar();
        generation1 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        // + generation (product)
        product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        // + generation (product)
        product.newGeneration(calendar);

        formula = generation1.newFormula();
        configuredDefault1 = generation1.newPropertyValue(policyAttribute, IConfiguredDefault.class);
        configuredValueSet1 = generation1.newPropertyValue(policyAttribute, IConfiguredValueSet.class);
        configuredDefault2 = generation1.newPropertyValue(policyAttribute2, IConfiguredDefault.class);
        configuredValueSet2 = generation1.newPropertyValue(policyAttribute2, IConfiguredValueSet.class);
        attributeValue = generation1.newAttributeValue();
        tableUsage = generation1.newTableContentUsage();
        tableUsage.setTableContentName("TestTableContents");

        relation1 = generation1.newLink("RelationType");
        relation1.setTarget(productReferenced.getQualifiedName());
        relation2 = generation1.newLink("OtherRelationType");
        relation2.setTarget(productReferenced2.getQualifiedName());
        relation3 = generation1.newLink("RelationType", relation1);
        relation3.setTarget(productReferenced2.getQualifiedName());

        IFile correspondingFile = product.getIpsSrcFile().getCorrespondingFile().unwrap();
        compareItemRoot = (ProductCmptCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    @Test
    public void testCompare() {
        ProductCmptCompareItemComparator comparator = new ProductCmptCompareItemComparator();

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];

        // 1 config composite (configuredDefault) 4 generations
        children = compareItem.getChildren();
        assertEquals(5, children.length);
        ProductCmptCompareItem compareItemDefaultProd = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[3];
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[4];

        assertEquals(configuredDefaultProd, compareItemDefaultProd.getIpsElement());
        assertEquals(-1, comparator.compare(compareItemGen1, compareItemGen2));
        assertEquals(-1, comparator.compare(compareItemGen2, compareItemGen3));
        assertEquals(-2, comparator.compare(compareItemGen1, compareItemGen3));
        assertEquals(2, comparator.compare(compareItemGen3, compareItemGen1));
        assertEquals(0, comparator.compare(compareItemGen1, compareItemGen1));

        // relations and attributes
        children = compareItemGen1.getChildren();
        assertEquals(10, children.length);
        ProductCmptCompareItem compareItemAttribute1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemValueSet1 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemDefault1 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemValueSet2 = (ProductCmptCompareItem)children[3];
        ProductCmptCompareItem compareItemDefault2 = (ProductCmptCompareItem)children[4];
        ProductCmptCompareItem compareItemFormula = (ProductCmptCompareItem)children[5];
        ProductCmptCompareItem compareItemTableUsage = (ProductCmptCompareItem)children[6];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[7];
        ProductCmptCompareItem compareItemRelation2 = (ProductCmptCompareItem)children[8];
        ProductCmptCompareItem compareItemRelation3 = (ProductCmptCompareItem)children[9];

        // attributes are sorted by type: productAttribute, tableUsage, formula, policyAttribute
        assertEquals(attributeValue, compareItemAttribute1.getIpsElement());
        assertEquals(tableUsage, compareItemTableUsage.getIpsElement());
        assertEquals(formula, compareItemFormula.getIpsElement());
        assertEquals(configuredValueSet1, compareItemValueSet1.getIpsElement());
        assertEquals(configuredDefault1, compareItemDefault1.getIpsElement());
        assertEquals(configuredDefault1.getPropertyName(), configuredValueSet1.getPropertyName());
        assertEquals(configuredDefault2, compareItemDefault2.getIpsElement());
        assertEquals(configuredValueSet2, compareItemValueSet2.getIpsElement());
        assertEquals(configuredDefault2.getPropertyName(), configuredValueSet2.getPropertyName());

        // maintain order of relations as defined at creation (rel3 inserted before rel1)
        assertEquals(relation3, compareItemRelation1.getIpsElement());
        assertEquals(relation1, compareItemRelation2.getIpsElement());
        assertEquals(relation2, compareItemRelation3.getIpsElement());
    }

}
