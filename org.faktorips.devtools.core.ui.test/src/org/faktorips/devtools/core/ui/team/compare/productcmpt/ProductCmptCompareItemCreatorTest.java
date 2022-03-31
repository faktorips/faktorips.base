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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptCompareItemCreatorTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();
    private IProductCmptGeneration generation1;
    private IProductCmptGeneration generation2;
    private IProductCmptGeneration generation3;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;

    private ProductCmptCompareItem compareItemRoot;
    private IProductCmpt product;
    private IConfiguredDefault configDefaultProd;
    private IConfiguredDefault configDefaultGen1;
    private IConfiguredDefault configDefaultGen2;
    private IProductCmptLink relation1;
    private IProductCmptLink relation2;
    private IProductCmptLink staticLink1;
    private IProductCmptLink staticLink2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject proj = newIpsProject(new ArrayList<Locale>());

        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(proj, "policyCmpt", "productCmpt");

        IPolicyCmptTypeAttribute pAttributeProduct = policyCmptType.newPolicyCmptTypeAttribute("attr1");
        pAttributeProduct.setChangingOverTime(false);
        IPolicyCmptTypeAttribute pAttributeGen1 = policyCmptType.newPolicyCmptTypeAttribute("attr2");
        pAttributeGen1.setChangingOverTime(true);
        IPolicyCmptTypeAttribute pAttributeGen2 = policyCmptType.newPolicyCmptTypeAttribute("attr3");
        pAttributeGen1.setChangingOverTime(true);

        IProductCmptType productCmptType = policyCmptType.findProductCmptType(proj);

        product = newProductCmpt(productCmptType, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(productCmptType, "TestProductCmptReferenced");

        staticLink1 = product.newLink(productReferenced.getQualifiedName());
        staticLink2 = product.newLink(productReferenced.getQualifiedName());

        GregorianCalendar calendar = new GregorianCalendar();
        generation1 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        generation2 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        generation3 = (IProductCmptGeneration)product.newGeneration(calendar);

        configDefaultProd = product.newPropertyValue(pAttributeProduct, IConfiguredDefault.class);
        configDefaultGen1 = generation1.newPropertyValue(pAttributeGen1, IConfiguredDefault.class);
        configDefaultGen2 = generation1.newPropertyValue(pAttributeGen2, IConfiguredDefault.class);

        relation1 = generation1.newLink(productReferenced.getQualifiedName());
        relation2 = generation1.newLink(productReferenced.getQualifiedName());

        srcFile = product.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile().unwrap();

        compareItemRoot = (ProductCmptCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    @Test
    public void testGetStructure() {
        assertEquals(srcFile, compareItemRoot.getIpsElement());

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        assertEquals(product, compareItem.getIpsElement());

        children = compareItem.getChildren();
        ProductCmptCompareItem compareConfigDefault = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareStaticLink1 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareStaticLink2 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[4];
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[5];
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[6];

        assertEquals(configDefaultProd, compareConfigDefault.getIpsElement());
        assertEquals(staticLink1, compareStaticLink1.getIpsElement());
        assertEquals(staticLink2, compareStaticLink2.getIpsElement());
        assertEquals(generation1, compareItemGen1.getIpsElement());
        assertEquals(generation2, compareItemGen2.getIpsElement());
        assertEquals(generation3, compareItemGen3.getIpsElement());

        children = compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElement1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemConfigElement2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemRelation2 = (ProductCmptCompareItem)children[3];

        assertEquals(configDefaultGen1, compareItemConfigElement1.getIpsElement());
        assertEquals(configDefaultGen2, compareItemConfigElement2.getIpsElement());
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
