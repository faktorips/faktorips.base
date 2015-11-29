/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.TemplateValueStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptPropertyTypeTest {

    private ProductCmptTypeAttribute pctAttr;
    private ProductCmptTypeAttribute pctAttr2;
    private IProductCmpt prodCmpt;
    private SingletonMockHelper singletonMockHelper;

    @Before
    public void setUp() {
        IpsModel ipsModel = mock(IpsModel.class);
        IpsPlugin ipsPlugin = mock(IpsPlugin.class);
        when(ipsPlugin.getIpsModel()).thenReturn(ipsModel);
        singletonMockHelper = new SingletonMockHelper();
        singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);

        pctAttr = mock(ProductCmptTypeAttribute.class);
        pctAttr2 = mock(ProductCmptTypeAttribute.class);
        prodCmpt = mock(ProductCmpt.class, withSettings().extraInterfaces(IProductCmpt.class));
        when(pctAttr.isMultiValueAttribute()).thenReturn(true);
        when(pctAttr.getPropertyName()).thenReturn("PropName");
        when(pctAttr2.getPropertyName()).thenReturn("PropName2");
    }

    @After
    public void teartDown() {
        singletonMockHelper.reset();
    }

    @Test
    public void createMultiValueAttributeWithDefaultValue() {
        when(pctAttr.getDefaultValue()).thenReturn("defaultValue123");
        IAttributeValue attrValue = (IAttributeValue)ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE
                .createPropertyValue(prodCmpt, pctAttr, "partID");

        MultiValueHolder multiValueHolder = (MultiValueHolder)attrValue.getValueHolder();
        assertEquals(1, multiValueHolder.getValue().size());
        assertEquals("defaultValue123", multiValueHolder.getValue().get(0).getStringValue());
    }

    @Test
    public void creatEmptyMultiValueAttributeIfDefaultValueIsNull() {
        when(pctAttr.getDefaultValue()).thenReturn(null);
        IAttributeValue attrValue = (IAttributeValue)ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE
                .createPropertyValue(prodCmpt, pctAttr, "partID");

        MultiValueHolder multiValueHolder = (MultiValueHolder)attrValue.getValueHolder();
        assertTrue(multiValueHolder.getValue().isEmpty());
    }

    @Test
    public void creatPropertyValue_inheritedWhenUsingTemplate() {
        when(prodCmpt.isUsingTemplate()).thenReturn(true);
        IAttributeValue attrValue = (IAttributeValue)ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE
                .createPropertyValue(prodCmpt, pctAttr2, "partID");

        assertThat(attrValue.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
    }

    @Test
    public void creatPropertyValue_definedWhenNotUsingTemplate() {
        when(prodCmpt.isUsingTemplate()).thenReturn(false);
        IAttributeValue attrValue = (IAttributeValue)ProductCmptPropertyType.PRODUCT_CMPT_TYPE_ATTRIBUTE
                .createPropertyValue(prodCmpt, pctAttr2, "partID");

        assertThat(attrValue.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
    }

}
