/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptPropertyTypeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmptTypeAttribute pctAttr;
    private IProductCmptTypeAttribute pctAttr2;
    private IProductCmpt prodCmpt;
    private IProductCmpt templateCmpt;

    private IAttributeValue templateValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();

        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");

        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        prodCmpt = newProductCmpt(productCmptType, "TestProduct");

        pctAttr = productCmptType.newProductCmptTypeAttribute("propName");
        pctAttr.setMultiValueAttribute(true);

        pctAttr2 = productCmptType.newProductCmptTypeAttribute("propName2");
    }

    @Test
    public void testCreatePropertyValue_MultiValueAttributeWithDefaultValue() {
        pctAttr.setDefaultValue("defaultValue123");
        IAttributeValue attrValue = (IAttributeValue)PropertyValueType.ATTRIBUTE_VALUE.createPropertyValue(prodCmpt,
                pctAttr, "partID");

        MultiValueHolder multiValueHolder = (MultiValueHolder)attrValue.getValueHolder();
        assertEquals(1, multiValueHolder.getValue().size());
        assertEquals("defaultValue123", multiValueHolder.getValue().get(0).getStringValue());
    }

    @Test
    public void testCreatePropertyValue_EmptyMultiValueAttributeIfDefaultValueIsNull() {
        pctAttr.setDefaultValue(null);
        IAttributeValue attrValue = (IAttributeValue)PropertyValueType.ATTRIBUTE_VALUE.createPropertyValue(prodCmpt,
                pctAttr, "partID");

        MultiValueHolder multiValueHolder = (MultiValueHolder)attrValue.getValueHolder();
        assertTrue(multiValueHolder.getValue().isEmpty());
    }

    @Test
    public void testCreatePropertyValue_TemplateValueStatusShouldBeInheritedWhenUsingTemplate() {
        templateCmpt = newProductTemplate(productCmptType, "template");
        prodCmpt.setTemplate(templateCmpt.getQualifiedName());

        templateValue = templateCmpt.newPropertyValue(pctAttr2, IAttributeValue.class);
        templateValue.setTemplateValueStatus(TemplateValueStatus.DEFINED);

        IAttributeValue attrValue = (IAttributeValue)PropertyValueType.ATTRIBUTE_VALUE.createPropertyValue(prodCmpt,
                pctAttr2, "partID");

        assertThat(attrValue.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
    }

    @Test
    public void testCreatePropertyValue_TemplateValueStatusShouldBeDefinedWhenNotUsingTemplate() {
        IAttributeValue attrValue = (IAttributeValue)PropertyValueType.ATTRIBUTE_VALUE.createPropertyValue(prodCmpt,
                pctAttr2, "partID");

        assertThat(attrValue.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
    }

}
