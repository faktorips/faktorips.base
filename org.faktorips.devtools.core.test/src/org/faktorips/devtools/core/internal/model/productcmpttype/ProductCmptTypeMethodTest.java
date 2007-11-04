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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
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
    
    public void testValidate_FormulaMustntBeAbstract() throws CoreException {
        method.setFormulaSignatureDefinition(true);
        method.setAbstract(true);
        MessageList result = method.validate();
        assertNotNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));
        
        method.setAbstract(false);
        result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));
        
        method.setFormulaSignatureDefinition(false);
        method.setAbstract(true);
        result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));
        
        method.setAbstract(false);
        result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));
    }
    
    public void testValidate_FormulaNameIsMissing() throws CoreException {
        method.setFormulaSignatureDefinition(false);
        method.setFormulaName("someName");
        MessageList result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));
        
        method.setFormulaSignatureDefinition(true);
        result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));
        
        method.setFormulaName("");
        result = method.validate();
        assertNotNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));
        
        method.setFormulaSignatureDefinition(false);
        result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));
    }
    
    public void testValidate_DatatypeMustBeAValueDatatypeForFormulaSignature() throws CoreException {
        method.setDatatype("void");
        method.setFormulaSignatureDefinition(false);
        MessageList result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
        method.setFormulaSignatureDefinition(true);
        result = method.validate();
        assertNotNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
        
        method.setFormulaSignatureDefinition(false);
        method.setDatatype(pcType.getQualifiedName());
        result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
        method.setFormulaSignatureDefinition(true);
        result = method.validate();
        assertNotNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
        
        method.setDatatype("Integer");
        result = method.validate();
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
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
        method.setFormulaSignatureDefinition(true);
        method.setAbstract(true);
        method.setFormulaName("Premium");
        IParameter param0 = method.newParameter();
        param0.setName("p0");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p1");
        param1.setDatatype("Money");

        Element element = method.toXml(this.newDocument());
        
        IProductCmptTypeMethod copy = pcType.newProductCmptTypeMethod();
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
