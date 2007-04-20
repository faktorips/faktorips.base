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

public class FormulaTestInputValueTest extends AbstractIpsPluginTest {
    private IIpsProject ipsProject;
    private IConfigElement configElement;
    private IFormulaTestCase formulaTestCase;
    private IFormulaTestInputValue formulaTestInputValue;

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
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        formulaTestInputValue.initFromXml((Element)doc.getDocumentElement());
        
        assertEquals("formulaTestInputValue1", formulaTestInputValue.getIdentifier());
        assertEquals("4711", formulaTestInputValue.getValue());
    }
    
    public void testToXmlDocument() {
        formulaTestInputValue.setIdentifier("foo1");
        formulaTestInputValue.setValue("value1");
        Element xmlElement = formulaTestInputValue.toXml(getTestDocument());

        IFormulaTestInputValue newFormulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        newFormulaTestInputValue.initFromXml(xmlElement);
        
        assertEquals("foo1", formulaTestInputValue.getIdentifier());
        assertEquals("value1", formulaTestInputValue.getValue());
    }
    
    public void testFindFormulaParameter() throws Exception{
        IPolicyCmptType pcType = newPolicyCmptType(ipsProject, "policyCmptType1");
        IAttribute attribute = pcType.newAttribute();
        attribute.setName("attribute1");
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setName("premium");
        Parameter[] params = new Parameter[]{new Parameter(0, "police", pcType.getQualifiedName())};
        attribute.setFormulaParameters(params);
        
        ((IProductCmptGeneration)configElement.getParent()).getProductCmpt().setPolicyCmptType(pcType.getQualifiedName());
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setPcTypeAttribute(attribute.getName());
        formulaTestInputValue.setIdentifier("police");
        
        Parameter parameterFound = formulaTestInputValue.findFormulaParameter();
        assertEquals(params[0], parameterFound);
    }

    public void testFindDatatypeOfFormulaParameter() throws CoreException{
        IPolicyCmptType pcTypeInput = newPolicyCmptType(ipsProject, "policyCmptTypeInput");
        IAttribute attributeInput = pcTypeInput.newAttribute();
        attributeInput.setName("attributeInput");
        attributeInput.setAttributeType(AttributeType.CHANGEABLE);
        attributeInput.setDatatype(Datatype.STRING.getQualifiedName());
        
        IPolicyCmptType pcType = newPolicyCmptType(ipsProject, "policyCmptType1");
        IAttribute attribute = pcType.newAttribute();
        attribute.setName("attribute1");
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        Parameter[] params = new Parameter[3];
        params[0] = new Parameter(0, "param1", Datatype.INTEGER.getQualifiedName());
        params[1] = new Parameter(1, "param2", Datatype.BOOLEAN.getQualifiedName());
        params[2] = new Parameter(2, "policyInputX", pcTypeInput.getQualifiedName());
        
        attribute.setFormulaParameters(params);
        
        ((IProductCmptGeneration)configElement.getParent()).getProductCmpt().setPolicyCmptType(pcType.getQualifiedName());
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setPcTypeAttribute(attribute.getName());
        configElement.setValue("param1 * param2 * 2 * policyInputX.attributeInput");
        
        // param1
        IFormulaTestInputValue formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("param1");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.INTEGER, formulaTestInputValue.findDatatypeOfFormulaParameter());
        
        // check attribute in supertype
        IPolicyCmptType superType = newPolicyCmptType(ipsProject, "base");
        pcTypeInput.setSupertype(superType.getQualifiedName());
        IAttribute attrInSuperType = superType.newAttribute();
        attrInSuperType.setName("super");
        attrInSuperType.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.super");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.INTEGER, formulaTestInputValue.findDatatypeOfFormulaParameter());
        
        // param2
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("param2");
        formulaTestInputValue.setValue("3");
        assertEquals(Datatype.BOOLEAN, formulaTestInputValue.findDatatypeOfFormulaParameter());
        // policyInput
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.attributeInput");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.STRING, formulaTestInputValue.findDatatypeOfFormulaParameter());
        
        // check if inconsistence doesn't cause an exception
        //   a) datatype of the related attribute wasn't found
        attributeInput.setDatatype("UnknowDatatype");
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.attributeInput");
        formulaTestInputValue.setValue("10");
        assertEquals(null, formulaTestInputValue.findDatatypeOfFormulaParameter());
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());

        //   b) wrong identifier
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("unknown_policyInputX.attributeInput");
        formulaTestInputValue.setValue("10");
        assertEquals(null, formulaTestInputValue.findDatatypeOfFormulaParameter());
        attributeInput.setName("attributeInput");
        
        //   c) related attribute not found (wrong identifier)
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.unknown_attributeInput");
        formulaTestInputValue.setValue("none");
        assertEquals(null, formulaTestInputValue.findDatatypeOfFormulaParameter());
    }

    public void testValidate_formulaParameterNotFound() throws CoreException{
        IPolicyCmptType pcType = newPolicyCmptType(ipsProject, "policyCmptType1");
        IAttribute attribute = pcType.newAttribute();
        attribute.setName("attribute1");
        attribute.setProductRelevant(true);

        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        attribute.setDatatype(Datatype.STRING.getQualifiedName());
        Parameter[] params = new Parameter[3];
        params[0] = new Parameter(0, "param1", Datatype.INTEGER.getQualifiedName());
        params[1] = new Parameter(1, "param2", Datatype.BOOLEAN.getQualifiedName());
        params[2] = new Parameter(2, "policyInputX", pcType.getQualifiedName());
        attribute.setFormulaParameters(params);
        
        ((IProductCmptGeneration)configElement.getParent()).getProductCmpt().setPolicyCmptType(pcType.getQualifiedName());
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setPcTypeAttribute(attribute.getName());
        configElement.setValue("param1 * param2 * 2 * policyInputX.attributeInput");

        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.attribute1");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.STRING, formulaTestInputValue.findDatatypeOfFormulaParameter());
        
        // param1
        formulaTestInputValue.setIdentifier("param1");
        MessageList ml = formulaTestInputValue.validate();
        assertNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_FORMULA_PARAMETER_NOT_FOUND));

        formulaTestInputValue.setIdentifier("xyz");
        ml = formulaTestInputValue.validate();
        assertNotNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_FORMULA_PARAMETER_NOT_FOUND));
    }
    
    public void testValidate_relatedAttributeNotFound_datatypeOfRelatedAttributeNotFound() throws CoreException{      
        IPolicyCmptType pcType = newPolicyCmptType(ipsProject, "policyCmptType1");
        IAttribute attribute = pcType.newAttribute();
        attribute.setName("attribute1");
        attribute.setAttributeType(AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        Parameter[] params = new Parameter[3];
        params[0] = new Parameter(0, "param1", Datatype.INTEGER.getQualifiedName());
        params[1] = new Parameter(1, "param2", Datatype.BOOLEAN.getQualifiedName());
        params[2] = new Parameter(2, "policyInputX", pcType.getQualifiedName());
        
        attribute.setFormulaParameters(params);
        
        ((IProductCmptGeneration)configElement.getParent()).getProductCmpt().setPolicyCmptType(pcType.getQualifiedName());
        configElement.setType(ConfigElementType.FORMULA);
        configElement.setPcTypeAttribute(attribute.getName());
        configElement.setValue("param1 * param2 * 2 * policyInputX.attributeInput");
        
        // param1
        IFormulaTestInputValue formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("param1");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.INTEGER, formulaTestInputValue.findDatatypeOfFormulaParameter());
        // param2
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("param2");
        formulaTestInputValue.setValue("3");
        assertEquals(Datatype.BOOLEAN, formulaTestInputValue.findDatatypeOfFormulaParameter());
        // policyInput
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.attribute1");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.INTEGER, formulaTestInputValue.findDatatypeOfFormulaParameter());
        
        // check validate
        // a) datatype of the related attribute wasn't found
        attribute.setDatatype("UnknowDatatype");
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyCmptType1.attribute1");
        formulaTestInputValue.setValue("10");
        MessageList ml = formulaTestInputValue.validate();
        assertNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_DATATYPE_OF_RELATED_ATTRIBUTE_NOT_FOUND));
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        //   b) related attribute not found (wrong identifier)
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyCmptType1.unknown_attributeInput");
        formulaTestInputValue.setValue("none");
        ml = formulaTestInputValue.validate();
        assertNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_RELATED_ATTRIBUTE_NOT_FOUND));
    }    
}
