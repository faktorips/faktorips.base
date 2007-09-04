/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype2;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.w3c.dom.Element;


/**
 *
 */
public class ProductCmptTypeMethodTest extends AbstractIpsPluginTest {
    
    private IProductCmptType pcType;
    private IProductCmptTypeMethod method;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject= newIpsProject("TestProject");
        pcType = newProductCmptType(ipsProject, "Type");
        method = pcType.newProductCmptTypeMethod();
    }
    
    public void testInitFromXml() {
        Element docElement = this.getTestDocument().getDocumentElement();
        method.setFormulaSignatureDefinition(false);
        method.initFromXml(XmlUtil.getElement(docElement, "Method", 0));
        assertTrue(method.isFormulaSignatureDefinition());
        assertEquals("Premium", method.getFormulaName());
        assertEquals(42, method.getId());
        assertEquals("calcPremium", method.getName());
        assertEquals("Money", method.getDatatype());
        assertEquals(Modifier.PUBLIC, method.getModifier());
        assertTrue(method.isAbstract());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        method = pcType.newProductCmptTypeMethod(); // => id=1, because it's the second method
        method.setName("getAge");
        method.setModifier(Modifier.PUBLIC);
        method.setDatatype("Decimal");
        method.setAbstract(true);
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("Premium");
        IParameter param0 = method.newParameter();
        param0.setName("p0");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p1");
        param1.setDatatype("Money");

        Element element = method.toXml(this.newDocument());
        
        IProductCmptTypeMethod copy = pcType.newProductCmptTypeMethod();
        method.setFormulaSignatureDefinition(false);
        copy.initFromXml(element);
        assertTrue(copy.isFormulaSignatureDefinition());
        assertEquals("Premium", copy.getFormulaName());
        IParameter[] copyParams = copy.getParameters();  
        assertEquals(1, copy.getId());
        assertEquals("getAge", copy.getName());
        assertEquals("Decimal", copy.getDatatype());
        assertEquals(Modifier.PUBLIC, copy.getModifier());
        assertTrue(copy.isAbstract());
        assertEquals(2, copyParams.length);
        assertEquals("p0", copyParams[0].getName());
        assertEquals("Decimal", copyParams[0].getDatatype());
        assertEquals("p1", copyParams[1].getName());
        assertEquals("Money", copyParams[1].getDatatype());

    }
    
}
