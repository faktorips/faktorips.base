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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import javax.xml.transform.TransformerException;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.Modifier;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttributeTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute policyAttribute;
    
    private IProductCmptTypeAttribute productAttribute;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        policyAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyAttribute.setName("policyAttribute");
        policyAttribute.setDatatype(Datatype.INTEGER.getName());
        
        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmptType.setPolicyCmptType("Policy");

        productAttribute = productCmptType.newProductCmptTypeAttribute();
        productAttribute.setName("productAttribute");
    }
    
    public void testDelete() {
        productAttribute.delete();
        assertNull(productCmptType.getProductCmptTypeAttribute(productAttribute.getName()));
        assertEquals(0, productCmptType.getNumOfAttributes());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute#getImage()}.
     */
    public void testGetImage() {
        assertNotNull(productAttribute.getImage());
    }
    
    public void testInitFromXml() {
        IProductCmptTypeAttribute attr = productCmptType.newProductCmptTypeAttribute();
        Element rootEl = getTestDocument().getDocumentElement();
        
        // product attribute
        attr.setModifier(Modifier.PUBLISHED);
        attr.initFromXml(XmlUtil.getElement(rootEl, ProductCmptTypeAttribute.TAG_NAME, 0));
        assertEquals("rate", attr.getName());
        assertEquals(Modifier.PUBLIC, attr.getModifier());
        assertEquals("Integer", attr.getDatatype());
    }
    
    public void testToXml_ProductAttribute() {
        Element el = productAttribute.toXml(newDocument());
        
        IProductCmptTypeAttribute a = productCmptType.newProductCmptTypeAttribute();
        a.initFromXml(el);
        assertEquals(productAttribute.getName(), a.getName());
        assertEquals(productAttribute.getModifier(), a.getModifier());
        assertEquals(productAttribute.getDatatype(), a.getDatatype());
    }
    
    /**
     * Tests if a attributes with properties containing null can be transformed to xml wihtout exceptions
     * as null handling can be a problem especially tranforming the xml to strings.
     * 
     * @throws TransformerException 
     */
    public void testToXml_NullHandlng() throws TransformerException {
        IProductCmptTypeAttribute a = productCmptType.newProductCmptTypeAttribute();
        Element el = a.toXml(newDocument());
        XmlUtil.nodeToString(el, "UTF-8");
    }

}
