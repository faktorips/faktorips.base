/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AttributeValueTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IProductCmptTypeAttribute attribute;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;

    private IAttributeValue attrValue;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("Minimum Premium");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmpt = newProductCmpt(productCmptType, "ProductA");
        generation = productCmpt.getProductCmptGeneration(0);
        attrValue = generation.newAttributeValue(attribute, "42");
    }

    public void testValidate_UnknownAttribute() throws CoreException {
        MessageList ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        attrValue.setAttribute("AnotherAttribute");
        ml = attrValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        IProductCmptType supertype = newProductCmptType(ipsProject, "SuperProduct");
        productCmptType.setSupertype(supertype.getQualifiedName());

        supertype.newProductCmptTypeAttribute().setName("AnotherAttribute");
        ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));
    }

    public void testValidate_ValueNotParsable() throws CoreException {
        MessageList ml = attrValue.validate(ipsProject);
        assertNull(ml
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        attrValue.setValue("abc");
        ml = attrValue.validate(ipsProject);
        assertNotNull(ml
                .getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
    }

    public void testValidate_ValueNotInSet() throws CoreException {
        MessageList ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));

        attribute.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet range = (IRangeValueSet)attribute.getValueSet();
        range.setLowerBound("0");
        range.setUpperBound("100");

        attrValue.setValue("0");
        ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attrValue.setValue("100");
        ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attrValue.setValue("42");
        ml = attrValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attrValue.setValue("-1");
        ml = attrValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

        attrValue.setValue("101");
        ml = attrValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_VALUE_NOT_IN_SET));

    }

    public void testSetAttribute() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_ATTRIBUTE, attrValue,
                "premium");
    }

    public void testSetValue() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_VALUE, attrValue, "newValue");
    }

    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        attrValue.initFromXml(el);
        assertEquals("rate", attrValue.getAttribute());
        assertEquals("42", attrValue.getValue());
    }

    public void testToXml() {
        Document doc = newDocument();
        attrValue.setValue("42");
        attrValue.setAttribute("rate");
        Element el = attrValue.toXml(doc);

        IAttributeValue copy = generation.newAttributeValue();
        copy.initFromXml(el);
        assertEquals("rate", copy.getAttribute());
        assertEquals("42", copy.getValue());
    }

    public void testGetCaption() throws CoreException {
        Locale locale = Locale.US;
        ILabel label = attribute.newLabel();
        label.setLocale(locale);
        label.setValue("TheCaption");
        assertEquals("TheCaption", attrValue.getCaption(locale));
    }

    public void testGetCaptionNotExistent() throws CoreException {
        assertNull(attrValue.getCaption(Locale.US));
    }

    public void testGetLastResortCaption() {
        assertEquals(attrValue.getAttribute(), attrValue.getLastResortCaption());
    }

}
