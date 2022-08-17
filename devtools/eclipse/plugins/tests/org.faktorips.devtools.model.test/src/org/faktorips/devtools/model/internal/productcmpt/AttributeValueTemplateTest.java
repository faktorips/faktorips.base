/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

public class AttributeValueTemplateTest extends AbstractIpsPluginTest {

    private ProductCmpt superTemplate;
    private ProductCmpt regularTemplate;
    private ProductCmpt productCmpt;
    private IIpsProject ipsProject;
    private IProductCmptTypeAttribute attrA;
    private IProductCmptTypeAttribute attrB;
    private IProductCmptTypeAttribute attrC;
    private IProductCmptTypeAttribute attrX;
    private IProductCmptTypeAttribute attrY;
    private IProductCmptTypeAttribute attrZ;
    private IAttributeValue superA;
    private IAttributeValue superB;
    private IAttributeValue superC;
    private IAttributeValue superX;
    private IAttributeValue superY;
    private IAttributeValue superZ;
    private IAttributeValue regularA;
    private IAttributeValue regularB;
    private IAttributeValue regularC;
    private IAttributeValue regularX;
    private IAttributeValue regularY;
    private IAttributeValue regularZ;
    private IAttributeValue prodCmptA;
    private IAttributeValue prodCmptB;
    private IAttributeValue prodCmptC;
    private IAttributeValue prodCmptX;
    private IAttributeValue prodCmptY;
    private IAttributeValue prodCmptZ;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IProductCmptType superType = newProductCmptType(ipsProject, "superType");
        attrA = newAttribute(superType, "a");
        attrB = newAttribute(superType, "b");
        attrC = newAttribute(superType, "c");
        attrX = newAttribute(superType, "x");
        attrY = newAttribute(superType, "y");
        attrZ = newAttribute(superType, "z");
        IProductCmptType regularType = newProductCmptType(superType, "regularType");
        IProductCmptType subType = newProductCmptType(regularType, "subType");

        superTemplate = newProductTemplate(superType, "SuperTemplate");
        regularTemplate = newProductTemplate(regularType, "RegularTemplate");
        regularTemplate.setTemplate(superTemplate.getQualifiedName());
        productCmpt = newProductCmpt(subType, "ProductCmpt");
        productCmpt.setTemplate(regularTemplate.getQualifiedName());

        setUpSuperTemplate();
        setUpRegularTemplate();
        setUpProductCmpt();
    }

    private IProductCmptTypeAttribute newAttribute(IProductCmptType superType, String name) {
        IProductCmptTypeAttribute attr = superType.newProductCmptTypeAttribute(name);
        attr.setDatatype("Integer");
        return attr;
    }

    private void setUpSuperTemplate() {
        superTemplate.newPropertyValues(attrA);
        superTemplate.newPropertyValues(attrB);
        superTemplate.newPropertyValues(attrC);
        superTemplate.newPropertyValues(attrX);
        superTemplate.newPropertyValues(attrY);
        superTemplate.newPropertyValues(attrZ);

        superA = superTemplate.getAttributeValue("a");
        superA.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        superA.setValueHolder(new SingleValueHolder(superA, "5"));

        superB = superTemplate.getAttributeValue("b");
        superB.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        superC = superTemplate.getAttributeValue("c");
        superC.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        superC.setValueHolder(new SingleValueHolder(superC, "42"));

        superX = superTemplate.getAttributeValue("x");
        superX.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        superX.setValueHolder(new SingleValueHolder(superX, "23"));

        superY = superTemplate.getAttributeValue("y");
        superY.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        superY.setValueHolder(new SingleValueHolder(superY, "13"));

        superZ = superTemplate.getAttributeValue("z");
        superZ.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        superZ.setValueHolder(new SingleValueHolder(superZ, "8"));
    }

    private void setUpRegularTemplate() {
        regularTemplate.newPropertyValues(attrA);
        regularTemplate.newPropertyValues(attrB);
        regularTemplate.newPropertyValues(attrC);
        regularTemplate.newPropertyValues(attrX);
        regularTemplate.newPropertyValues(attrY);
        regularTemplate.newPropertyValues(attrZ);

        regularA = regularTemplate.getAttributeValue("a");
        regularA.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        regularB = regularTemplate.getAttributeValue("b");
        regularB.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        regularB.setValueHolder(new SingleValueHolder(regularB, "6"));

        regularC = regularTemplate.getAttributeValue("c");
        regularC.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        regularC.setValueHolder(new SingleValueHolder(regularC, "42"));

        regularX = regularTemplate.getAttributeValue("x");
        regularX.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        regularY = regularTemplate.getAttributeValue("y");
        regularY.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);

        regularZ = regularTemplate.getAttributeValue("z");
        regularZ.setTemplateValueStatus(TemplateValueStatus.DEFINED);
        regularZ.setValueHolder(new SingleValueHolder(regularZ, "6"));
    }

    private void setUpProductCmpt() {
        productCmpt.newPropertyValues(attrA);
        productCmpt.newPropertyValues(attrB);
        productCmpt.newPropertyValues(attrC);
        productCmpt.newPropertyValues(attrX);
        productCmpt.newPropertyValues(attrY);
        productCmpt.newPropertyValues(attrZ);

        prodCmptA = productCmpt.getAttributeValue("a");
        prodCmptA.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        prodCmptB = productCmpt.getAttributeValue("b");
        prodCmptB.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        prodCmptC = productCmpt.getAttributeValue("c");
        prodCmptC.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        prodCmptX = productCmpt.getAttributeValue("x");
        prodCmptX.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        prodCmptX.setValueHolder(new SingleValueHolder(prodCmptX, "15"));

        prodCmptY = productCmpt.getAttributeValue("y");
        prodCmptY.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        prodCmptY.setValueHolder(new SingleValueHolder(prodCmptY, "20"));

        prodCmptZ = productCmpt.getAttributeValue("z");
        prodCmptZ.setTemplateValueStatus(TemplateValueStatus.INHERITED);
    }

    @Test
    public void testGetPropertyValue_noParentTemplate() {
        assertThat(superA.findTemplateProperty(ipsProject), is(nullValue()));
    }

    /**
     * Search b from regularTemplate. Should find null, as the parent template defines no value for
     * B.
     */
    @Test
    public void testGetPropertyValue_NoValueDefinedInParentTemplate() {
        assertThat(regularB.findTemplateProperty(ipsProject), is(nullValue()));
    }

    /**
     * Search c from regularTemplate. Should find superTemplate.c, as the parent template also
     * defines a value for C. The status of c in regularTemplate is ignored.
     */
    @Test
    public void testGetPropertyValue_definedInBothCurrentAndParent() {
        assertThat(regularC.findTemplateProperty(ipsProject), is(superC));
    }

    /**
     * Search b from productCmpt. Should find regularTemplate.b, as the parent template defines a
     * value for B.
     */
    @Test
    public void testGetPropertyValue_inheritFromDirectParent() {
        assertThat(prodCmptB.findTemplateProperty(ipsProject), is(regularB));
    }

    /**
     * Search x from productCmpt. Should find superTemplate.x, as the value is inherited across two
     * templates.
     */
    @Test
    public void testGetPropertyValue_inheritFromTransitiveParent() {
        assertThat(prodCmptX.findTemplateProperty(ipsProject), is(superX));
    }

    /**
     * Search y from productCmpt. Should find null, as the super template marks X as undefined.
     */
    @Test
    public void testGetPropertyValue_nullWhenUndefined() {
        assertThat(prodCmptY.findTemplateProperty(ipsProject), is(nullValue()));
    }

    /**
     * Search z from productCmpt. Should find regularTemplate.z, as it is the first defined value in
     * the hierarchy.
     */
    @Test
    public void testGetPropertyValue_findFirstDefinition() {
        assertThat(prodCmptZ.findTemplateProperty(ipsProject), is(regularZ));
    }

}
