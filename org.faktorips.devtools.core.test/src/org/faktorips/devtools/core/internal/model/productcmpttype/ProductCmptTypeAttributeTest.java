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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttributeTest extends AbstractIpsPluginTest implements ContentsChangeListener {

    private ContentChangeEvent lastEvent = null;
    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private org.faktorips.devtools.core.model.pctype.IAttribute policyAttribute;
    
    private IProductCmptTypeAttribute productAttribute;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        policyAttribute = policyCmptType.newAttribute();
        policyAttribute.setName("policyAttribute");
        policyAttribute.setDatatype(Datatype.INTEGER.getName());
        
        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmptType.setPolicyCmptType("Policy");

        productAttribute = productCmptType.newAttribute();
        productAttribute.setName("productAttribute");
        
        ipsProject.getIpsModel().addChangeListener(this);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        ipsProject.getIpsModel().removeChangeListener(this);
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute#setName(java.lang.String)}.
     */
    public void testSetName() {
        productAttribute.setName("newName");
        assertEquals("newName", productAttribute.getName());
        assertEquals(productAttribute, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute#setModifier(org.faktorips.devtools.core.model.pctype.Modifier)}.
     */
    public void testSetModifier() {
        productAttribute.setModifier(Modifier.PUBLIC);
        assertEquals(Modifier.PUBLIC, productAttribute.getModifier());
        assertEquals(productAttribute, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute#setDatatype(java.lang.String)}.
     */
    public void testSetValueDatatype() {
        productAttribute.setDatatype(Datatype.BOOLEAN.getName());
        assertEquals(Datatype.BOOLEAN.getName(), productAttribute.getDatatype());
        assertEquals(productAttribute, lastEvent.getPart());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute#findDatatype()}.
     * @throws CoreException 
     */
    public void testFindValueDatatype() throws CoreException {
        productAttribute.setDatatype(Datatype.BOOLEAN.getName());
        assertEquals(Datatype.BOOLEAN, productAttribute.findDatatype(ipsProject));
        
        productAttribute.setDatatype("unkown");
        assertNull(productAttribute.findDatatype(ipsProject));
    }
    
    public void testSetDefaultValue() {
        productAttribute.setDefaultValue("newValue");
        assertEquals("newValue", productAttribute.getDefaultValue());
        assertEquals(productAttribute, lastEvent.getPart());
    }
    
    public void testDelete() {
        productAttribute.delete();
        assertNull(productCmptType.getAttribute(productAttribute.getName()));
        assertEquals(0, productCmptType.getNumOfAttributes());
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeAttribute#getImage()}.
     */
    public void testGetImage() {
        assertNotNull(productAttribute.getImage());
    }
    
    public void testInitFromXml() {
        IProductCmptTypeAttribute attr = productCmptType.newAttribute();
        Element rootEl = getTestDocument().getDocumentElement();
        
        // product attribute
        attr.setModifier(Modifier.PUBLISHED);
        attr.initFromXml(XmlUtil.getElement(rootEl, ProductCmptTypeAttribute.TAG_NAME, 0));
        assertEquals("rate", attr.getName());
        assertEquals(Modifier.PUBLIC, attr.getModifier());
        assertEquals("Integer", attr.getDatatype());
        
        assertEquals("20", attr.getDefaultValue());
        RangeValueSet range = (RangeValueSet)attr.getValueSet();
        assertEquals("18", range.getLowerBound());
        assertEquals("60", range.getUpperBound());
    }
    
    public void testToXml_ProductAttribute() {
        productAttribute.setName("rate");
        productAttribute.setModifier(Modifier.PUBLIC);
        productAttribute.setDatatype("Integer");
        productAttribute.setDefaultValue("42");
        productAttribute.setValueSetType(ValueSetType.RANGE);
        RangeValueSet range = (RangeValueSet)productAttribute.getValueSet();
        range.setLowerBound("1");
        range.setUpperBound("100");
        
        Element el = productAttribute.toXml(newDocument());
        
        IProductCmptTypeAttribute a = productCmptType.newAttribute();
        a.initFromXml(el);
        assertEquals("rate", a.getName());
        assertEquals(Modifier.PUBLIC, a.getModifier());
        assertEquals("Integer", a.getDatatype());
        assertEquals("42", a.getDefaultValue());
        range = (RangeValueSet)a.getValueSet();
        assertEquals("1", range.getLowerBound());
        assertEquals("100", range.getUpperBound());
        
        
    }
    
    /**
     * Tests if a attributes with properties containing null can be transformed to xml wihtout exceptions
     * as null handling can be a problem especially tranforming the xml to strings.
     * 
     * @throws TransformerException 
     */
    public void testToXml_NullHandlng() throws TransformerException {
        IProductCmptTypeAttribute a = productCmptType.newAttribute();
        Element el = a.toXml(newDocument());
        XmlUtil.nodeToString(el, "UTF-8");
    }
    
    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        lastEvent = event;
    }

}
