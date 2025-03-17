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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.internal.ipsobject.TimedIpsObject;
import org.faktorips.devtools.model.internal.pctype.PolicyCmptType;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptCompareItemTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new ProductCmptCompareItemCreator();

    private IPolicyCmptTypeAttribute attribute1;
    private IPolicyCmptTypeAttribute attribute2;

    private IProductCmptTypeAttribute pAttribute;

    private IProductCmptGeneration generation1;
    private IProductCmptGeneration generation2;
    private IProductCmptGeneration generation3;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;

    private ProductCmptCompareItem compareItemRoot;
    private IProductCmpt product;
    private IConfiguredDefault configDefault1;
    private IConfiguredDefault configDefault2;
    private IConfiguredValueSet configValueSet1;
    private IConfiguredValueSet configValueSet2;
    private IAttributeValue attributeValue;
    private IProductCmptLink relation1;
    private IProductCmptLink relation2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject proj = newIpsProject(new ArrayList<>());

        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(proj, "policyType", "productType");
        attribute1 = policyCmptType.newPolicyCmptTypeAttribute("attribute1");
        attribute2 = policyCmptType.newPolicyCmptTypeAttribute("attribute2");

        IProductCmptType productCmptType = policyCmptType.findProductCmptType(proj);
        pAttribute = productCmptType.newProductCmptTypeAttribute("pAttribute");

        product = newProductCmpt(productCmptType, "TestProductCmpt");
        IProductCmpt productReferenced = newProductCmpt(productCmptType, "TestProductCmptReferenced");

        GregorianCalendar calendar = new GregorianCalendar();
        generation1 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        generation2 = (IProductCmptGeneration)product.newGeneration(calendar);
        calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 2);
        generation3 = (IProductCmptGeneration)product.newGeneration(calendar);

        // names of the policy attributes ensure sorting order
        configDefault1 = generation1.newPropertyValue(attribute1, IConfiguredDefault.class);
        configDefault2 = generation1.newPropertyValue(attribute2, IConfiguredDefault.class);
        configDefault1.setValue("10");
        configValueSet1 = generation1.newPropertyValue(attribute1, IConfiguredValueSet.class);
        configValueSet2 = generation1.newPropertyValue(attribute2, IConfiguredValueSet.class);
        configValueSet2.setValueSetType(ValueSetType.ENUM);

        attributeValue = generation1.newPropertyValue(pAttribute, IAttributeValue.class);
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, "TestWert"));

        relation1 = generation1.newLink(productReferenced.getQualifiedName());
        relation2 = generation1.newLink(productReferenced.getQualifiedName());

        srcFile = product.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile().unwrap();

        compareItemRoot = (ProductCmptCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    @Test
    public void testGetChildren() {
        Object[] children = compareItemRoot.getChildren();
        // Srcfile contains ProductComponent
        assertEquals(1, children.length);

        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        children = compareItem.getChildren();
        // product component contains 4 generations, 1 automatically generated by newProductCmpt, 3
        // manually generated generations
        assertEquals(4, children.length);

        ProductCmptCompareItem compareItemGen = (ProductCmptCompareItem)children[1];
        children = compareItemGen.getChildren();
        // first manually generated generation has 2 ConfigElementComposites, 1 AttributeValue and 2
        // relations
        assertEquals(7, children.length);
    }

    @Test
    public void testGetImage() {
        assertEquals(IpsUIPlugin.getImageHandling().getImage(srcFile), compareItemRoot.getImage());

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        assertEquals(IpsUIPlugin.getImageHandling().getImage(product), compareItem.getImage());

        children = compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[0];
        assertEquals(IpsUIPlugin.getImageHandling().getImage(generation1), compareItemGen1.getImage());
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[1];
        assertEquals(IpsUIPlugin.getImageHandling().getImage(generation2), compareItemGen2.getImage());
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[2];
        assertEquals(IpsUIPlugin.getImageHandling().getImage(generation3), compareItemGen3.getImage());
    }

    @Test
    public void testGetType() {
        assertEquals("ipsproduct", compareItemRoot.getType());
    }

    @Test
    public void testGetParent() {
        assertNull(compareItemRoot.getParent());

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        assertNotNull(compareItem.getParent());
        assertEquals(compareItemRoot, compareItem.getParent());

        children = compareItem.getChildren();
        // first generation is not setted up
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[3];

        assertNotNull(compareItemGen1.getParent());
        assertEquals(compareItem, compareItemGen1.getParent());
        assertNotNull(compareItemGen2.getParent());
        assertEquals(compareItem, compareItemGen2.getParent());
        assertNotNull(compareItemGen3.getParent());
        assertEquals(compareItem, compareItemGen3.getParent());

        // CompareItemComparator sorts Configelements above Relations
        children = compareItemGen1.getChildren();
        ProductCmptCompareItem compareItemConfigElementComposite1 = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemConfigElementComposite2 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemAttributeValue = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[3];
        ProductCmptCompareItem compareItemRelation2 = (ProductCmptCompareItem)children[4];

        assertNotNull(compareItemConfigElementComposite1.getParent());
        assertEquals(compareItemGen1, compareItemConfigElementComposite1.getParent());
        assertNotNull(compareItemConfigElementComposite2.getParent());
        assertEquals(compareItemGen1, compareItemConfigElementComposite2.getParent());
        assertNotNull(compareItemAttributeValue.getParent());
        assertEquals(compareItemGen1, compareItemAttributeValue.getParent());
        assertNotNull(compareItemRelation1.getParent());
        assertEquals(compareItemGen1, compareItemRelation1.getParent());
        assertNotNull(compareItemRelation2.getParent());
        assertEquals(compareItemGen1, compareItemRelation2.getParent());
    }

    @Test
    public void testGetIpsElement() {
        assertEquals(srcFile, compareItemRoot.getIpsElement());

        Object[] children = compareItemRoot.getChildren();
        ProductCmptCompareItem compareItem = (ProductCmptCompareItem)children[0];
        assertEquals(product, compareItem.getIpsElement());

        children = compareItem.getChildren();
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemGen2 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemGen3 = (ProductCmptCompareItem)children[3];

        assertEquals(generation1, compareItemGen1.getIpsElement());
        assertEquals(generation2, compareItemGen2.getIpsElement());
        assertEquals(generation3, compareItemGen3.getIpsElement());

        children = compareItemGen1.getChildren();
        /*
         * Die Kinder jedes CompareItems werden sortiert. Dabei werden ProduktAttribute
         * (compareItem3) vor VertragsAttribute (compareItem1 und -2) gestellt.
         */
        ProductCmptCompareItem compareItemAttributeValue = (ProductCmptCompareItem)children[0];
        ProductCmptCompareItem compareItemValueSet1 = (ProductCmptCompareItem)children[1];
        ProductCmptCompareItem compareItemDefault1 = (ProductCmptCompareItem)children[2];
        ProductCmptCompareItem compareItemValueSet2 = (ProductCmptCompareItem)children[3];
        ProductCmptCompareItem compareItemDefault2 = (ProductCmptCompareItem)children[4];
        ProductCmptCompareItem compareItemRelation1 = (ProductCmptCompareItem)children[5];
        ProductCmptCompareItem compareItemRelation2 = (ProductCmptCompareItem)children[6];

        assertEquals(configValueSet1, compareItemValueSet1.getIpsElement());
        assertEquals(configDefault1, compareItemDefault1.getIpsElement());
        assertEquals(configValueSet2, compareItemValueSet2.getIpsElement());
        assertEquals(configDefault2, compareItemDefault2.getIpsElement());
        assertEquals(attributeValue, compareItemAttributeValue.getIpsElement());
        assertEquals(relation1, compareItemRelation1.getIpsElement());
        assertEquals(relation2, compareItemRelation2.getIpsElement());
    }

    @Test
    public void testEquals_self() {
        ProductCmptCompareItem productCompareItem = (ProductCmptCompareItem)compareItemRoot.getChildren()[0];
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)productCompareItem.getChildren()[1];

        assertThat(productCompareItem.equals(new ProductCmptCompareItem(compareItemRoot, product)), is(true));
        assertThat(compareItemGen1.equals(new ProductCmptCompareItem(productCompareItem, generation1)), is(true));
    }

    @Test
    public void testEquals_sameIdIsNotEnough() throws Exception {
        ProductCmptCompareItem productCompareItem = (ProductCmptCompareItem)compareItemRoot.getChildren()[0];
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)productCompareItem.getChildren()[1];

        Method newGenerationInternalMethod = TimedIpsObject.class.getDeclaredMethod("newGenerationInternal",
                String.class);
        newGenerationInternalMethod.setAccessible(true);
        IProductCmptGeneration generation1b = (IProductCmptGeneration)newGenerationInternalMethod.invoke(product,
                generation1.getId());
        generation1b.setValidFrom(new GregorianCalendar(2017, Calendar.MONTH, 1));
        assertThat(compareItemGen1.equals(new ProductCmptCompareItem(productCompareItem, generation1b)), is(false));
    }

    @Test
    public void testEquals_sameName() throws Exception {
        ProductCmptCompareItem productCompareItem = (ProductCmptCompareItem)compareItemRoot.getChildren()[0];
        ProductCmptCompareItem compareItemGen1 = (ProductCmptCompareItem)productCompareItem.getChildren()[1];

        generation2.setValidFrom(generation1.getValidFrom());
        assertThat(compareItemGen1.equals(new ProductCmptCompareItem(productCompareItem, generation2)), is(true));
    }

    @Test
    public void testGetContentString() {
        configValueSet1.setValueSet(new EnumValueSet(attribute1, Arrays.asList("true"), "false"));

        ProductCmptCompareItem productCmptCompareItem = new ProductCmptCompareItem(null, configValueSet1);
        productCmptCompareItem.init();
        String contentStringWithoutDatatype = productCmptCompareItem.getContentString();

        attribute1.setDatatype(ValueDatatype.BOOLEAN.getName());
        ProductCmptCompareItem productCmptCompareItem2 = new ProductCmptCompareItem(null, configValueSet1);
        productCmptCompareItem2.init();
        String contentStringWithDatatype = productCmptCompareItem2.getContentString();

        assertEquals(contentStringWithoutDatatype, contentStringWithDatatype);
    }

}
