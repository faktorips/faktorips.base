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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptPropertyTypeTest {

    private ProductCmptTypeAttribute pctAttr;
    private IProductCmpt prodCmpt;

    @Before
    public void setUp() {
        pctAttr = mock(ProductCmptTypeAttribute.class);
        prodCmpt = mock(IProductCmpt.class);
        when(pctAttr.isMultiValueAttribute()).thenReturn(true);
        when(pctAttr.getPropertyName()).thenReturn("PropName");
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

}
