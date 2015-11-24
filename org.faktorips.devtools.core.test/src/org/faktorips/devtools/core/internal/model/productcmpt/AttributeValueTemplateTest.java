/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue.TemplateStatus;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
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
    public void setUp() throws CoreException {
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
        superTemplate.newPropertyValue(attrA);
        superTemplate.newPropertyValue(attrB);
        superTemplate.newPropertyValue(attrC);
        superTemplate.newPropertyValue(attrX);
        superTemplate.newPropertyValue(attrY);
        superTemplate.newPropertyValue(attrZ);

        superA = superTemplate.getAttributeValue("a");
        superA.setTemplateStatus(TemplateStatus.DEFINED);
        superA.setValueHolder(new SingleValueHolder(superA, "5"));

        superB = superTemplate.getAttributeValue("b");
        superB.setTemplateStatus(TemplateStatus.UNDEFINED);

        superC = superTemplate.getAttributeValue("c");
        superC.setTemplateStatus(TemplateStatus.DEFINED);
        superC.setValueHolder(new SingleValueHolder(superC, "42"));

        superX = superTemplate.getAttributeValue("x");
        superX.setTemplateStatus(TemplateStatus.DEFINED);
        superX.setValueHolder(new SingleValueHolder(superX, "23"));

        superY = superTemplate.getAttributeValue("y");
        superY.setTemplateStatus(TemplateStatus.DEFINED);
        superY.setValueHolder(new SingleValueHolder(superY, "13"));

        superZ = superTemplate.getAttributeValue("z");
        superZ.setTemplateStatus(TemplateStatus.DEFINED);
        superZ.setValueHolder(new SingleValueHolder(superZ, "8"));
    }

    private void setUpRegularTemplate() {
        regularTemplate.newPropertyValue(attrA);
        regularTemplate.newPropertyValue(attrB);
        regularTemplate.newPropertyValue(attrC);
        regularTemplate.newPropertyValue(attrX);
        regularTemplate.newPropertyValue(attrY);
        regularTemplate.newPropertyValue(attrZ);

        regularA = regularTemplate.getAttributeValue("a");
        regularA.setTemplateStatus(TemplateStatus.UNDEFINED);

        regularB = regularTemplate.getAttributeValue("b");
        regularB.setTemplateStatus(TemplateStatus.DEFINED);
        regularB.setValueHolder(new SingleValueHolder(regularB, "6"));

        regularC = regularTemplate.getAttributeValue("c");
        regularC.setTemplateStatus(TemplateStatus.DEFINED);
        regularC.setValueHolder(new SingleValueHolder(regularC, "42"));

        regularX = regularTemplate.getAttributeValue("x");
        regularX.setTemplateStatus(TemplateStatus.INHERITED);

        regularY = regularTemplate.getAttributeValue("y");
        regularY.setTemplateStatus(TemplateStatus.UNDEFINED);

        regularZ = regularTemplate.getAttributeValue("z");
        regularZ.setTemplateStatus(TemplateStatus.DEFINED);
        regularZ.setValueHolder(new SingleValueHolder(regularZ, "6"));
    }

    private void setUpProductCmpt() {
        productCmpt.newPropertyValue(attrA);
        productCmpt.newPropertyValue(attrB);
        productCmpt.newPropertyValue(attrC);
        productCmpt.newPropertyValue(attrX);
        productCmpt.newPropertyValue(attrY);
        productCmpt.newPropertyValue(attrZ);

        prodCmptA = productCmpt.getAttributeValue("a");
        prodCmptA.setTemplateStatus(TemplateStatus.INHERITED);

        prodCmptB = productCmpt.getAttributeValue("b");
        prodCmptB.setTemplateStatus(TemplateStatus.INHERITED);

        prodCmptC = productCmpt.getAttributeValue("c");
        prodCmptC.setTemplateStatus(TemplateStatus.INHERITED);

        prodCmptX = productCmpt.getAttributeValue("x");
        prodCmptX.setTemplateStatus(TemplateStatus.UNDEFINED);
        prodCmptX.setValueHolder(new SingleValueHolder(prodCmptX, "15"));

        prodCmptY = productCmpt.getAttributeValue("y");
        prodCmptY.setTemplateStatus(TemplateStatus.UNDEFINED);
        prodCmptY.setValueHolder(new SingleValueHolder(prodCmptY, "20"));

        prodCmptZ = productCmpt.getAttributeValue("z");
        prodCmptZ.setTemplateStatus(TemplateStatus.INHERITED);
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
