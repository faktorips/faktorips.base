/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptCompareItemCreatorTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();
    private IProductCmptGeneration generation1;
    private IPropertyValueContainer generation2;
    private IPropertyValueContainer generation3;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;

    private ProductCmptCompareItem compareItemRoot;
    private IProductCmpt product;
    private IIpsPackageFragmentRoot root;
    private IConfigElement configElement1;
    private IConfigElement configElement2;
    private IProductCmptLink relation1;
    private IProductCmptLink relation2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject proj = newIpsProject("TestProject", new ArrayList<Locale>());
        root = proj.getIpsPackageFragmentRoots()[0];
        product = newProductCmpt(root, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(root, "TestProductCmptReferenced");

        GregorianCalendar calendar = new GregorianCalendar();
        generation1 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        generation2 = (IPropertyValueContainer)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        generation3 = (IPropertyValueContainer)product.newGeneration(calendar);

        configElement1 = generation1.newConfigElement();
        configElement1.setPolicyCmptTypeAttribute("configElement1"); // set name to ensure sorting
        // order
        configElement2 = generation1.newConfigElement();
        configElement2.setPolicyCmptTypeAttribute("configElement2");
        relation1 = generation1.newLink(productReferenced.getQualifiedName());
        relation2 = generation1.newLink(productReferenced.getQualifiedName());

        srcFile = product.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile();

        compareItemRoot = (ProductCmptCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    @Test
    public void testGetStructure() {
        assertEquals(srcFile, compareItemRoot.getIpsElement());

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        assertEquals(product, compareItem.getIpsElement());

        children = compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[2];

        assertEquals(generation1, compareItemGen1.getIpsElement());
        assertEquals(generation2, compareItemGen2.getIpsElement());
        assertEquals(generation3, compareItemGen3.getIpsElement());

        children = compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemConfigElement2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemRelation2 = (ProductCmptCompareItem)children[3];

        assertEquals(configElement1, compareItemConfigElement1.getIpsElement());
        assertEquals(configElement2, compareItemConfigElement2.getIpsElement());
        assertEquals(relation1, compareItemRelation1.getIpsElement());
        assertEquals(relation2, compareItemRelation2.getIpsElement());
    }

    @Test
    public void testGetContents() {
        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];

        String contentString = structureCreator.getContents(compareItemRoot, false);
        assertEquals(compareItemRoot.getContentString(), contentString);
        contentString = structureCreator.getContents(compareItem, false);
        assertEquals(compareItem.getContentString(), contentString);

        contentString = structureCreator.getContents(compareItemRoot, true);
        assertTrue(compareItemRoot.getContentStringWithoutWhiteSpace().equals(contentString));
        contentString = structureCreator.getContents(compareItem, true);
        assertTrue(compareItem.getContentStringWithoutWhiteSpace().equals(contentString));
    }

}
