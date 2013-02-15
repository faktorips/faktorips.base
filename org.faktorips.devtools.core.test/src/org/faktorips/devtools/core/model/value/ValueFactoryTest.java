/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.value.InternationalStringValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
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

        value = ValueFactory.createValue(false, "foo");
        assertNotNull(value);
        assertEquals(ValueType.STRING, ValueType.getValueType(value));
        assertNotNull(value.getContent());
        assertEquals("foo", value.getContentAsString());

    }
}
