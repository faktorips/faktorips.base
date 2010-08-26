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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestInputValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FormulaTestInputValueTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IFormula formula;
    private IFormulaTestCase formulaTestCase;
    private IFormulaTestInputValue formulaTestInputValue;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "productCmpt");
        IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(0);
        formula = generation.newFormula();
        formulaTestCase = formula.newFormulaTestCase();
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
    }

    public void testValidate_relatedAttributeNotFound_datatypeOfRelatedAttributeNotFound() throws CoreException {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setDatatype(Datatype.STRING.getQualifiedName());
        method.setName("calculatePremium");
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("Premium Calculation");
        method.newParameter(Datatype.INTEGER.getQualifiedName(), "param1");
        method.newParameter(Datatype.BOOLEAN.getQualifiedName(), "param2");
        IParameter param = method.newParameter(policyCmptType.getQualifiedName(), "policy");

        formula.setFormulaSignature(method.getFormulaName());
        formula.setExpression("param1 * param2 * 2 * policy.premium");

        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setAttributeType(AttributeType.CHANGEABLE);
        attribute.setName("premium");
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        formulaTestInputValue.setIdentifier("policy.premium");
        formulaTestInputValue.setValue("10");
        assertEquals(param, formulaTestInputValue.findFormulaParameter(ipsProject)); // test setup

        // ok case
        MessageList ml = formulaTestInputValue.validate(ipsProject);
        assertNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_DATATYPE_OF_RELATED_ATTRIBUTE_NOT_FOUND));

        // datatype of the referenced attribute wasn't found
        attribute.setDatatype("UnknowDatatype");
        ml = formulaTestInputValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_DATATYPE_OF_RELATED_ATTRIBUTE_NOT_FOUND));

        // related attribute not found (wrong identifier)
        formulaTestInputValue.setIdentifier("policy.unknownAttribute");
        ml = formulaTestInputValue.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_RELATED_ATTRIBUTE_NOT_FOUND));
    }

    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        formulaTestInputValue.initFromXml(doc.getDocumentElement());

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

    public void testFindFormulaParameter() throws Exception {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.setName("calculatePremium");
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("Premium Calculation");
        IParameter param = method.newParameter(policyCmptType.getQualifiedName(), "policy");

        formula.setFormulaSignature(method.getFormulaName());
        formulaTestInputValue.setIdentifier("policy");

        IParameter parameterFound = formulaTestInputValue.findFormulaParameter(ipsProject);
        assertEquals(param, parameterFound);
    }

    public void testFindDatatypeOfFormulaParameter() throws CoreException {
        IPolicyCmptType pcTypeInput = newPolicyCmptType(ipsProject, "policyCmptTypeInput");
        IPolicyCmptTypeAttribute attributeInput = pcTypeInput.newPolicyCmptTypeAttribute();
        attributeInput.setName("attributeInput");
        attributeInput.setAttributeType(AttributeType.CHANGEABLE);
        attributeInput.setDatatype(Datatype.STRING.getQualifiedName());

        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.setName("calculatePremium");
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("Premium Calculation");
        method.newParameter(Datatype.INTEGER.getQualifiedName(), "param1");
        method.newParameter(Datatype.BOOLEAN.getQualifiedName(), "param2");
        method.newParameter(pcTypeInput.getQualifiedName(), "policyInputX");

        formula.setFormulaSignature(method.getFormulaName());
        formula.setExpression("param1 * param2 * 2 * policyInputX.attributeInput");

        // param1
        IFormulaTestInputValue formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("param1");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.INTEGER, formulaTestInputValue.findDatatypeOfFormulaParameter(ipsProject));

        // param2
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("param2");
        formulaTestInputValue.setValue("3");
        assertEquals(Datatype.BOOLEAN, formulaTestInputValue.findDatatypeOfFormulaParameter(ipsProject));

        // policyInput
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.attributeInput");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.STRING, formulaTestInputValue.findDatatypeOfFormulaParameter(ipsProject));

        // check if inconsistence doesn't cause an exception
        // a) datatype of the related attribute wasn't found
        attributeInput.setDatatype("UnknowDatatype");
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.attributeInput");
        formulaTestInputValue.setValue("10");
        assertEquals(null, formulaTestInputValue.findDatatypeOfFormulaParameter(ipsProject));
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());

        // check attribute in supertype
        IPolicyCmptType superType = newPolicyCmptType(ipsProject, "BaseType");
        pcTypeInput.setSupertype(superType.getQualifiedName());
        IPolicyCmptTypeAttribute attrInSuperType = superType.newPolicyCmptTypeAttribute();
        attrInSuperType.setName("super");
        attrInSuperType.setDatatype(Datatype.INTEGER.getQualifiedName());
        attrInSuperType.setAttributeType(AttributeType.CHANGEABLE);
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.super");
        formulaTestInputValue.setValue("10");
        assertEquals(Datatype.INTEGER, formulaTestInputValue.findDatatypeOfFormulaParameter(ipsProject));

        // b) wrong identifier
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("unknown_policyInputX.attributeInput");
        formulaTestInputValue.setValue("10");
        assertEquals(null, formulaTestInputValue.findDatatypeOfFormulaParameter(ipsProject));
        attributeInput.setName("attributeInput");

        // c) related attribute not found (wrong identifier)
        formulaTestInputValue = formulaTestCase.newFormulaTestInputValue();
        formulaTestInputValue.setIdentifier("policyInputX.unknown_attributeInput");
        formulaTestInputValue.setValue("none");
        assertEquals(null, formulaTestInputValue.findDatatypeOfFormulaParameter(ipsProject));
    }

    public void testValidate_formulaParameterNotFound() throws CoreException {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setDatatype(Datatype.STRING.getQualifiedName());
        method.setName("calculatePremium");
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("Premium Calculation");
        method.newParameter(Datatype.INTEGER.getQualifiedName(), "param1");
        method.newParameter(Datatype.BOOLEAN.getQualifiedName(), "param2");
        method.newParameter(policyCmptType.getQualifiedName(), "policy");

        IPolicyCmptTypeAttribute attributeInput = policyCmptType.newPolicyCmptTypeAttribute();
        attributeInput.setName("premium");
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());

        formula.setFormulaSignature(method.getFormulaName());
        formula.setExpression("param1 * param2 * 2 * policy.premium");

        // param1
        formulaTestInputValue.setIdentifier("param1");
        MessageList ml = formulaTestInputValue.validate(formulaTestInputValue.getIpsProject());
        assertNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_FORMULA_PARAMETER_NOT_FOUND));

        formulaTestInputValue.setIdentifier("xyz");
        ml = formulaTestInputValue.validate(formulaTestInputValue.getIpsProject());
        assertNotNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_FORMULA_PARAMETER_NOT_FOUND));

        formulaTestInputValue.setIdentifier("policy123.premium");
        ml = formulaTestInputValue.validate(formulaTestInputValue.getIpsProject());
        assertNotNull(ml.getMessageByCode(IFormulaTestInputValue.MSGCODE_FORMULA_PARAMETER_NOT_FOUND));
    }

}
