/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.internal.InternationalString;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.value.InternationalStringValue;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ValueFactoryTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IProductCmptTypeAttribute attribute;
    private IProductCmpt productCmpt;

    private IProductCmptGeneration generation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("produktname");
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        attribute.setMultilingual(true);
        productCmpt = newProductCmpt(productCmptType, "ProductA");
        generation = productCmpt.getProductCmptGeneration(0);

    }

    @Test
    public void testCreateValueElement() {
        IAttributeValue attrValue = generation.newAttributeValue(attribute);
        Element el = getTestDocument().getDocumentElement();
        attrValue.initFromXml(el);
        assertEquals("produktname", attrValue.getAttribute());
        IValueHolder<?> valueHolder = attrValue.getValueHolder();
        assertNotNull(valueHolder);
        assertTrue(valueHolder instanceof SingleValueHolder);
        IValue<?> value = (IValue<?>)valueHolder.getValue();
        assertTrue(value instanceof InternationalStringValue);
        InternationalString intString = (InternationalString)value.getContent();
        assertEquals(2, intString.values().size());
        assertEquals("Versicherung", intString.get(Locale.GERMAN).getValue());
        assertEquals("Insurance", intString.get(Locale.ENGLISH).getValue());
    }

    @Test
    public void testCreateValueMultilingual() {
        IValue<?> value = ValueFactory.createValue(true, null);
        assertNotNull(value);
        assertEquals(ValueType.INTERNATIONAL_STRING, ValueType.getValueType(value));
        assertNotNull(value.getContent());
        assertTrue(value.getContent() instanceof InternationalString);

        value = ValueFactory.createValue(false, "foo");
        assertNotNull(value);
        assertEquals(ValueType.STRING, ValueType.getValueType(value));
        assertNotNull(value.getContent());
        assertTrue(value.getContent() instanceof String);
        assertEquals("foo", value.getContentAsString());

    }
}
