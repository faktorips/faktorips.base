/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptCompareItemComparatorTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();

    private IProductCmptGeneration generation1;

    private ProductCmptCompareItem compareItemRoot;

    private IFormula formula;
    private IConfigElement policyAttribute;
    private IAttributeValue productAttribute;
    private ITableContentUsage tableUsage;
    private IProductCmptLink relation1;
    private IProductCmptLink relation2;
    private IProductCmptLink relation3;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject proj = newIpsProject(new ArrayList<Locale>());
        IIpsPackageFragmentRoot root = proj.getIpsPackageFragmentRoots()[0];
        IProductCmpt product = newProductCmpt(root, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(root, "TestProductCmptReferenced");
        IProductCmpt productReferenced2 = newProductCmpt(root, "TestProductCmptReferenced2");

        GregorianCalendar calendar = new GregorianCalendar();
        generation1 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        product.newGeneration(calendar);

        formula = generation1.newFormula();
        policyAttribute = generation1.newConfigElement();
        productAttribute = generation1.newAttributeValue();
        tableUsage = generation1.newTableContentUsage();
        tableUsage.setTableContentName("TestTableContents");

        relation1 = generation1.newLink("RelationType");
        relation1.setTarget(productReferenced.getQualifiedName());
        relation2 = generation1.newLink("OtherRelationType");
        relation2.setTarget(productReferenced2.getQualifiedName());
        relation3 = generation1.newLink("RelationType", relation1);
        relation3.setTarget(productReferenced2.getQualifiedName());

        IFile correspondingFile = product.getIpsSrcFile().getCorrespondingFile();
        compareItemRoot = (ProductCmptCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    @Test
    public void testCompare() {
        ProductCmptCompareItemComparator comparator = new ProductCmptCompareItemComparator();

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];

        // generations
        children = compareItem.getChildren();
        assertEquals(3, children.length);
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[2];

        assertEquals(-1, comparator.compare(compareItemGen1, compareItemGen2));
        assertEquals(-1, comparator.compare(compareItemGen2, compareItemGen3));
        assertEquals(-2, comparator.compare(compareItemGen1, compareItemGen3));
        assertEquals(2, comparator.compare(compareItemGen3, compareItemGen1));
        assertEquals(0, comparator.compare(compareItemGen1, compareItemGen1));

        // relations and attributes
        children = compareItemGen1.getChildren();
        assertEquals(7, children.length);
        ProductCmptCompareItem compareItemAttribute1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemAttribute2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemAttribute3 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemAttribute4 = (ProductCmptCompareItem)children[3];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[4];
        ProductCmptCompareItem compareItemRelation2 = (ProductCmptCompareItem)children[5];
        ProductCmptCompareItem compareItemRelation3 = (ProductCmptCompareItem)children[6];

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
