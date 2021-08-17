/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt.deltaentries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.productcmpt.AbstractValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.AttributeValue;
import org.faktorips.devtools.model.internal.productcmpt.DelegatingValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.AttributeValueType;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.value.ValueFactory;
import org.junit.Before;
import org.junit.Test;

public class HiddenAttributeMismatchEntryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IProductCmptType productCmptType;

    private IProductCmpt productCmpt;

    private IProductCmptTypeAttribute newAttribute;

    private IAttributeValue attrValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmpt = newProductCmpt(productCmptType, "ProductA");

        newAttribute = (IProductCmptTypeAttribute)productCmptType.newAttribute();
        newAttribute.setName("attribute");
        attrValue = new AttributeValue(productCmpt.getProductCmpt(), "Produkt", "attribute");
    }

    @Test
    public void testFix_SingleValueHolder() {
        newAttribute.setDefaultValue("defaultvalue");
        newAttribute.setName("attribute");
        attrValue.setValueHolder(new SingleValueHolder(attrValue, "someValue"));
        HiddenAttributeMismatchEntry deltaEntry = new HiddenAttributeMismatchEntry(attrValue, newAttribute);

        deltaEntry.fix();

        assertEquals("defaultvalue", attrValue.getValueHolder().getValue().toString());
    }

    @Test
    public void testFix_MultiValueHolder() {
        newAttribute.setDefaultValue("80 | 60");
        newAttribute.setName("attribute");
        attrValue.setValueHolder(new MultiValueHolder(attrValue, null));
        HiddenAttributeMismatchEntry deltaEntry = new HiddenAttributeMismatchEntry(attrValue, newAttribute);

        deltaEntry.fix();

        assertEquals(ValueFactory.createValue(false, "80 | 60"), attrValue.getValueHolder().getValue());
    }

    @Test
    public void testIsMismatch_missmatch() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IValueHolder<?> valueHolder = AttributeValueType.SINGLE_VALUE.newHolderInstance(attributeValue,
                ValueFactory.createStringValue("default"));
        doReturn(valueHolder).when(attributeValue).getValueHolder();

        HiddenAttributeMismatchEntry mismatchEntry = new HiddenAttributeMismatchEntry(attributeValue, attribute);
        assertTrue(mismatchEntry.isMismatch());
    }

    @Test
    public void testIsMismatch_noMissmatch() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IValueHolder<?> valueHolder = AttributeValueType.SINGLE_VALUE.newHolderInstance(attributeValue,
                ValueFactory.createStringValue("default"));
        doReturn(valueHolder).when(attributeValue).getValueHolder();
        when(attribute.getDefaultValue()).thenReturn("default");

        HiddenAttributeMismatchEntry mismatchEntry = new HiddenAttributeMismatchEntry(attributeValue, attribute);
        assertFalse(mismatchEntry.isMismatch());
    }

    @Test
    public void testIsMismatch_noMultiValueMissmatch() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        List<ISingleValueHolder> defaultValues = new ArrayList<>();
        defaultValues.add(new SingleValueHolder(attributeValue, "default1"));
        defaultValues.add(new SingleValueHolder(attributeValue, "default2"));
        MultiValueHolder valueHolder = new MultiValueHolder(attributeValue, defaultValues);
        doReturn(valueHolder).when(attributeValue).getValueHolder();
        when(attribute.getDefaultValue()).thenReturn("default1 |default2");
        when(attribute.isMultiValueAttribute()).thenReturn(true);

        HiddenAttributeMismatchEntry mismatchEntry = new HiddenAttributeMismatchEntry(attributeValue, attribute);
        assertFalse(mismatchEntry.isMismatch());
    }

    @Test
    public void testIsMismatch_noMissmatchWhenDelegateMatches() throws Exception {
        IAttributeValue attributeValue = mock(IAttributeValue.class);
        IProductCmptTypeAttribute attribute = mock(IProductCmptTypeAttribute.class);
        IValueHolder<?> valueHolder = AttributeValueType.SINGLE_VALUE.newHolderInstance(attributeValue,
                ValueFactory.createStringValue("default"));
        @SuppressWarnings("unchecked")
        DelegatingValueHolder<String> delegatingValueHolder = new DelegatingValueHolder<>(attributeValue,
                (AbstractValueHolder<String>)valueHolder);
        doReturn(delegatingValueHolder).when(attributeValue).getValueHolder();
        when(attribute.getDefaultValue()).thenReturn("default");

        HiddenAttributeMismatchEntry mismatchEntry = new HiddenAttributeMismatchEntry(attributeValue, attribute);
        assertFalse(mismatchEntry.isMismatch());
    }

}
