/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.core.model.product.IAttributeValue;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype2.IAttribute;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AttributeValueTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IProductCmptType productCmptType;
    private IAttribute attribute;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    
    private IAttributeValue attrValue;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "Product");
        attribute = productCmptType.newAttribute();
        attribute.setName("Minimum Premium");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        productCmpt = newProductCmpt(productCmptType, "ProductA");
        generation = productCmpt.getProductCmptGeneration(0);
        attrValue = generation.newAttributeValue(attribute, "42");
    }
    
    public void testValidate_UnknownAttribute() throws CoreException {
        MessageList ml = attrValue.validate();
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));
        
        attrValue.setAttribute("AnotherAttribute");
        ml = attrValue.validate();
        assertNotNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));
        
        IProductCmptType supertype = newProductCmptType(ipsProject, "SuperProduct");
        productCmptType.setSupertype(supertype.getQualifiedName());
        
        supertype.newAttribute().setName("AnotherAttribute");
        ml = attrValue.validate();
        assertNull(ml.getMessageByCode(IAttributeValue.MSGCODE_UNKNWON_ATTRIBUTE));
    }
    
    public void testValidate_ValueNotParsable() throws CoreException {
        MessageList ml = attrValue.validate();
        assertNull(ml.getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
        
        attrValue.setValue("abc");
        ml = attrValue.validate();
        assertNotNull(ml.getMessageByCode(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));
    }    
    
    public void testSetAttribute() {
        super.testPropertyAccessReadWrite(IAttributeValue.class, IAttributeValue.PROPERTY_ATTRIBUTE, attrValue, "premium");
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
    
}
