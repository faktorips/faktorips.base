/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttributeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IProductCmptType productCmptType;

    private IProductCmptTypeAttribute productAttribute;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmptType.setPolicyCmptType("Policy");

        productAttribute = productCmptType.newProductCmptTypeAttribute();
        productAttribute.setName("productAttribute");
    }

    @Test
    public void testDelete() {
        productAttribute.delete();
        assertNull(productCmptType.getProductCmptTypeAttribute(productAttribute.getName()));
        assertEquals(0, productCmptType.getNumOfAttributes());
    }

    @Test
    public void testXml() {
        productAttribute.setCategory("foo");
        Element el = productAttribute.toXml(newDocument());

        IProductCmptTypeAttribute a = productCmptType.newProductCmptTypeAttribute();
        a.initFromXml(el);
        assertEquals(productAttribute.getName(), a.getName());
        assertEquals(productAttribute.getModifier(), a.getModifier());
        assertEquals(productAttribute.getDatatype(), a.getDatatype());
        assertEquals(productAttribute.getCategory(), a.getCategory());
    }

    /**
     * Tests if a attributes with properties containing null can be transformed to xml without
     * exceptions as null handling can be a problem especially transforming the xml to strings.
     */
    @Test
    public void testToXml_NullHandlng() throws TransformerException {
        IProductCmptTypeAttribute a = productCmptType.newProductCmptTypeAttribute();
        Element el = a.toXml(newDocument());
        XmlUtil.nodeToString(el, "UTF-8");
    }

    @Test
    public void testIsPolicyCmptTypeProperty() {
        assertFalse(productAttribute.isPolicyCmptTypeProperty());
    }

    @Test
    public void testIsPropertyFor() throws CoreException {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IPropertyValue propertyValue = generation.newAttributeValue(productAttribute);

        assertTrue(productAttribute.isPropertyFor(propertyValue));
    }

    @Test
    public void testFindOverwrittenAttribute() throws CoreException {
        productAttribute.setName("a");
        IProductCmptType supertype = newProductCmptType(productCmptType, "Supertype");
        IProductCmptType supersupertype = newProductCmptType(ipsProject, "SuperSupertype");
        productCmptType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        assertNull(productAttribute.findOverwrittenAttribute(ipsProject));

        IProductCmptTypeAttribute aInSupertype = supersupertype.newProductCmptTypeAttribute();
        aInSupertype.setName("a");

        assertEquals(aInSupertype, productAttribute.findOverwrittenAttribute(ipsProject));

        // cycle in type hierarchy
        supersupertype.setSupertype(productCmptType.getQualifiedName());
        assertEquals(aInSupertype, productAttribute.findOverwrittenAttribute(ipsProject));

        aInSupertype.delete();
        assertNull(productAttribute.findOverwrittenAttribute(ipsProject)); // this should not return
                                                                           // itself!
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentMultiValue() throws Exception {
        productAttribute.setName("name");
        productAttribute.setDatatype("String");
        productAttribute.setMultiValueAttribute(false);
        productAttribute.setOverwrite(true);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES));

        IProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        productCmptType.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setMultiValueAttribute(true);

        ml = productAttribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES));

        productAttribute.setMultiValueAttribute(true);
        superAttr.setMultiValueAttribute(false);

        ml = productAttribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES));

        productAttribute.setMultiValueAttribute(superAttr.isMultiValueAttribute());
        ml = productAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_SINGE_MULTI_VALUE_DIFFERES));
    }

    @Test
    public void testValidate_OverwrittenAttributeHasDifferentChangingOverTime() throws Exception {
        productAttribute.setName("name");
        productAttribute.setDatatype("String");
        productAttribute.setChangingOverTime(false);
        productAttribute.setOverwrite(true);

        MessageList ml = productAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));

        IProductCmptType supertype = newProductCmptType(ipsProject, "sup.SuperType");
        productCmptType.setSupertype(supertype.getQualifiedName());
        IProductCmptTypeAttribute superAttr = supertype.newProductCmptTypeAttribute();
        superAttr.setName("name");
        superAttr.setDatatype("String");
        superAttr.setChangingOverTime(true);

        ml = productAttribute.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));

        productAttribute.setChangingOverTime(superAttr.isChangingOverTime());
        ml = productAttribute.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IProductCmptTypeAttribute.MSGCODE_OVERWRITTEN_ATTRIBUTE_HAS_DIFFERENT_CHANGE_OVER_TIME));
    }

}
