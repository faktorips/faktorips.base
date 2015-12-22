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

import org.faktorips.abstracttest.SingletonMockHelper;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.productcmpt.AttributeValue;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.PropertyValueType;
import org.faktorips.devtools.core.model.productcmpt.template.TemplateValueStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptPropertyTypeTest {

    @Mock
    private ProductCmptTypeAttribute pctAttr;

    @Mock
    private ProductCmptTypeAttribute pctAttr2;

    @Mock
    private IProductCmpt templateCmpt;

    @Mock
    private AttributeValue templateValue;

    @Mock(extraInterfaces = IProductCmpt.class)
    private ProductCmpt prodCmpt;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IIpsProject ipsProject;

    private SingletonMockHelper singletonMockHelper;

    @Before
    public void setUp() {
        IpsModel ipsModel = mock(IpsModel.class);
        IpsPlugin ipsPlugin = mock(IpsPlugin.class);
        when(ipsPlugin.getIpsModel()).thenReturn(ipsModel);
        singletonMockHelper = new SingletonMockHelper();
        singletonMockHelper.setSingletonInstance(IpsPlugin.class, ipsPlugin);

        when(prodCmpt.getIpsProject()).thenReturn(ipsProject);
        when(pctAttr.isMultiValueAttribute()).thenReturn(true);
        when(pctAttr.getPropertyName()).thenReturn("PropName");
        when(pctAttr2.getPropertyName()).thenReturn("PropName2");
    }

    @After
    public void teartDown() {
        singletonMockHelper.reset();
    }

    @Test
    public void testCreatePropertyValue_MultiValueAttributeWithDefaultValue() {
        when(pctAttr.getDefaultValue()).thenReturn("defaultValue123");
        IAttributeValue attrValue = (IAttributeValue)PropertyValueType.ATTRIBUTE_VALUE.createPropertyValue(prodCmpt,
                pctAttr, "partID");

        MultiValueHolder multiValueHolder = (MultiValueHolder)attrValue.getValueHolder();
        assertEquals(1, multiValueHolder.getValue().size());
        assertEquals("defaultValue123", multiValueHolder.getValue().get(0).getStringValue());
    }

    @Test
    public void testCreatePropertyValue_EmptyMultiValueAttributeIfDefaultValueIsNull() {
        when(pctAttr.getDefaultValue()).thenReturn(null);
        IAttributeValue attrValue = (IAttributeValue)PropertyValueType.ATTRIBUTE_VALUE.createPropertyValue(prodCmpt,
                pctAttr, "partID");

        MultiValueHolder multiValueHolder = (MultiValueHolder)attrValue.getValueHolder();
        assertTrue(multiValueHolder.getValue().isEmpty());
    }

    @Test
    public void testCreatePropertyValue_TemplateValueStatusShouldBeInheritedWhenUsingTemplate() {
        when(templateValue.getTemplateValueStatus()).thenReturn(TemplateValueStatus.DEFINED);
        when(prodCmpt.isUsingTemplate()).thenReturn(true);
        when(prodCmpt.findTemplate(ipsProject)).thenReturn(templateCmpt);
        when(templateCmpt.getPropertyValue(pctAttr2.getPropertyName(), IAttributeValue.class))
        .thenReturn(templateValue);
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
