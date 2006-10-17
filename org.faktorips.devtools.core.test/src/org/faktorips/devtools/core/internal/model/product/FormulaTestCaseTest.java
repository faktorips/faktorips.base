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
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.Parameter;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IFormulaTestCase;
import org.faktorips.devtools.core.model.product.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FormulaTestCaseTest extends AbstractIpsPluginTest {
    private IIpsProject ipsProject;
    private IConfigElement configElement;
    private IFormulaTestCase formulaTestCase;

    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "policyCmpt");
        IProductCmpt productCmpt = newProductCmpt(ipsProject, "productCmpt");
        productCmpt.setPolicyCmptType(policyCmptType.getQualifiedName());
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        configElement = generation.newConfigElement();
        configElement.setType(ConfigElementType.FORMULA);
        formulaTestCase = configElement.newFormulaTestCase();
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        formulaTestCase.initFromXml((Element)doc.getDocumentElement());
        
        assertEquals("formulaTestCase", formulaTestCase.getName());
        assertEquals("4711", formulaTestCase.getExpectedResult());
        assertEquals(2, formulaTestCase.getFormulaTestInputValues().length);
    }
    
    public void testToXmlDocument() {
        IFormulaTestCase formulaTestCase = configElement.newFormulaTestCase();
        formulaTestCase.setExpectedResult("101");
        formulaTestCase.setName("formulaTestCase101");
        formulaTestCase.newFormulaTestInputValue().setIdentifier("foo1");
        formulaTestCase.newFormulaTestInputValue().setIdentifier("foo2");
        Element xmlElement = formulaTestCase.toXml(getTestDocument());

        IFormulaTestCase newFormulaTestCase = configElement.newFormulaTestCase();
        newFormulaTestCase.initFromXml(xmlElement);
        assertEquals("101", newFormulaTestCase.getExpectedResult());
        assertEquals("formulaTestCase101", newFormulaTestCase.getName());
        assertEquals(2, newFormulaTestCase.getFormulaTestInputValues().length);
        assertNotNull(newFormulaTestCase.getFormulaTestInputValue("foo1"));
        assertNotNull(newFormulaTestCase.getFormulaTestInputValue("foo2"));
    }
    
    public void testNewAndDeleteFormulaTestInputValue(){
        IFormulaTestInputValue v1 = formulaTestCase.newFormulaTestInputValue();
        IFormulaTestInputValue v2 = formulaTestCase.newFormulaTestInputValue();
        v1.setIdentifier("foo1");
        v2.setIdentifier("foo2");
        assertEquals(2, formulaTestCase.getFormulaTestInputValues().length);
        assertEquals(v1, formulaTestCase.getFormulaTestInputValue("foo1"));
        assertEquals(v2, formulaTestCase.getFormulaTestInputValue("foo2"));
        
        v1.delete();
        v2.delete();
        assertEquals(0, formulaTestCase.getFormulaTestInputValues().length);
    }
    
    public void testExecute() throws Exception{
        IPolicyCmptType pcTypeInput = newPolicyCmptType(ipsProject, "policyCmptTypeInput");
        IAttribute attributeInput = pcTypeInput.newAttribute();
        attributeInput.setName("attributeInput");
        attributeInput.setAttributeType(AttributeType.CHANGEABLE);
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());
        
        IPolicyCmptType pcType = newPolicyCmptType(ipsProject, "policyCmptType1");
        IAttribute attribute = pcType.newAttribute();
        attribute.setName("attribute1");
        attribute.setAttributeType(AttributeType.COMPUTED);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        Parameter[] params = new Parameter[3];
        params[0] = new Parameter(0, "param1", Datatype.INTEGER.getQualifiedName());
        params[1] = new Parameter(1, "param2", Datatype.INTEGER.getQualifiedName());
        params[2] = new Parameter(2, "policyInputX", pcTypeInput.getQualifiedName());
        
        attribute.setFormulaParameters(params);
        
        ((IProductCmptGeneration)configElement.getParent()).getProductCmpt().setPolicyCmptType(pcType.getQualifiedName());
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setPcTypeAttribute(attribute.getName());
        configElement.setValue("param1 * param2 * 2 * policyInputX.attributeInput");
        
        // sets the input for the formula test
        // param1
        IFormulaTestInputValue formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("param1");
        formulaTestInputValue.setValue("10");
        // param2
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("param2");
        formulaTestInputValue.setValue("3");
        // policyInput
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.attributeInput");
        formulaTestInputValue.setValue("10");
        
        Object result = formulaTestCase.execute();
        assertEquals(new Integer(600), result);
    }

    public void testAddOrDeleteFormulaTestInputValues(){
        IFormulaTestInputValue value1 = formulaTestCase.newFormulaTestInputValue();
        value1.setIdentifier("value1");
        IFormulaTestInputValue value2 = formulaTestCase.newFormulaTestInputValue();
        value2.setIdentifier("value2");
        IFormulaTestInputValue value3 = formulaTestCase.newFormulaTestInputValue();
        value3.setIdentifier("value3");
        
        String[] newValues = new String[]{"value1","value3","value2"};
        assertTrue(formulaTestCase.addOrDeleteFormulaTestInputValues(newValues));
        IFormulaTestInputValue[] valuesNew = formulaTestCase.getFormulaTestInputValues();
        assertEquals(value1, valuesNew[0]);
        assertEquals(value3, valuesNew[1]);
        assertEquals(value2, valuesNew[2]);
        assertEquals(3, newValues.length);
        assertFalse(formulaTestCase.addOrDeleteFormulaTestInputValues(newValues));
        
        newValues = new String[]{"value1", "value4", "value2", "value5"};
        assertTrue(formulaTestCase.addOrDeleteFormulaTestInputValues(newValues));
        valuesNew = formulaTestCase.getFormulaTestInputValues();
        assertEquals(value1, valuesNew[0]);
        assertEquals("value4", valuesNew[1].getIdentifier());
        assertEquals(value2, valuesNew[2]);
        assertEquals("value5", valuesNew[3].getIdentifier());
        assertEquals(4, newValues.length);
        assertFalse(formulaTestCase.addOrDeleteFormulaTestInputValues(newValues));
    }    
    
    public void testIsFormulaTestCaseEmpty(){
        assertTrue(formulaTestCase.isFormulaTestCaseEmpty());
        IFormulaTestInputValue value1 = formulaTestCase.newFormulaTestInputValue();
        value1.setIdentifier("param1");
        IFormulaTestInputValue value2 = formulaTestCase.newFormulaTestInputValue();
        value1.setIdentifier("param2");
        // check if empty, because all params have no value
        assertTrue(formulaTestCase.isFormulaTestCaseEmpty());
        
        value1.setValue("1");
        assertFalse(formulaTestCase.isFormulaTestCaseEmpty());
        value2.setValue("2");
        assertFalse(formulaTestCase.isFormulaTestCaseEmpty());
    }
    
    public void testGenerateUniqueNameForFormulaTestCase(){
        formulaTestCase.setName("Test");
        assertEquals("Test", formulaTestCase.generateUniqueNameForFormulaTestCase("Test"));
        
        IFormulaTestCase ftc1 = configElement.newFormulaTestCase();
        ftc1.setName(ftc1.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (2)", ftc1.getName());
        
        IFormulaTestCase ftcNew = configElement.newFormulaTestCase();
        ftcNew.setName(ftcNew.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (3)", ftcNew.getName());
        
        ftcNew = configElement.newFormulaTestCase();
        ftcNew.setName(ftcNew.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (4)", ftcNew.getName());
        
        ftc1.setName("X");
        ftcNew = configElement.newFormulaTestCase();
        ftcNew.setName(ftcNew.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (2)", ftcNew.getName());
        
        ftcNew = configElement.newFormulaTestCase();
        ftcNew.setName(ftcNew.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (5)", ftcNew.getName());
        
    }
    
    public void testValidate_DuplicateName() throws CoreException{
        formulaTestCase.setName("Test");
        MessageList ml = formulaTestCase.validate();
        assertNull(ml.getMessageByCode(IFormulaTestCase.MSGCODE_DUPLICATE_NAME));
        
        IFormulaTestCase ftc = configElement.newFormulaTestCase();
        ftc.setName(formulaTestCase.getName());
        ml = formulaTestCase.validate();
        assertNotNull(ml.getMessageByCode(IFormulaTestCase.MSGCODE_DUPLICATE_NAME));
        
        ftc.setName(ftc.generateUniqueNameForFormulaTestCase(formulaTestCase.getName()));
        ml = formulaTestCase.validate();
        assertNull(ml.getMessageByCode(IFormulaTestCase.MSGCODE_DUPLICATE_NAME));
    }
}
