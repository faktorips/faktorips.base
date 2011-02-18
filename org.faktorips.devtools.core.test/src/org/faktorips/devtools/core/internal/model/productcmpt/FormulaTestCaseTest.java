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
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FormulaTestCaseTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptType productCmptType;
    private IFormula formula;
    private IFormulaTestCase formulaTestCase;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        IPolicyCmptTypeAttribute attributeInput = policyCmptType.newPolicyCmptTypeAttribute();
        attributeInput.setName("attributeInput");
        attributeInput.setAttributeType(AttributeType.CHANGEABLE);
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());

        productCmpt = newProductCmpt(productCmptType, "productCmpt");
        IProductCmptGeneration generation = productCmpt.getProductCmptGeneration(0);
        formula = generation.newFormula();
        formulaTestCase = formula.newFormulaTestCase();
        formula.setExpression("1");
    }

    @Test
    public void testExecuteFormulaOnlyParam() throws Exception {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("PremiumCalculation");
        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        method.newParameter(Datatype.INTEGER.getQualifiedName(), "param1");
        method.newParameter(Datatype.INTEGER.getQualifiedName(), "param2");
        method.newParameter(policyCmptType.getQualifiedName(), "policy");

        formula.setFormulaSignature(method.getFormulaName());
        formula.setExpression("param1 * param2 * 2 * policy.attributeInput");

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
        formulaTestInputValue.setIdentifier("policy.attributeInput");
        formulaTestInputValue.setValue("10");

        Object result = formulaTestCase.execute(ipsProject);
        assertEquals(new Integer(600), result);
    }

    @Test
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        formulaTestCase.initFromXml(doc.getDocumentElement());

        assertEquals("formulaTestCase", formulaTestCase.getName());
        assertEquals("4711", formulaTestCase.getExpectedResult());
        assertEquals(2, formulaTestCase.getFormulaTestInputValues().length);
    }

    @Test
    public void testToXmlDocument() {
        IFormulaTestCase formulaTestCase = formula.newFormulaTestCase();
        formulaTestCase.setExpectedResult("101");
        formulaTestCase.setName("formulaTestCase101");
        formulaTestCase.newFormulaTestInputValue().setIdentifier("foo1");
        formulaTestCase.newFormulaTestInputValue().setIdentifier("foo2");
        Element xmlElement = formulaTestCase.toXml(getTestDocument());

        IFormulaTestCase newFormulaTestCase = formula.newFormulaTestCase();
        newFormulaTestCase.initFromXml(xmlElement);
        assertEquals("101", newFormulaTestCase.getExpectedResult());
        assertEquals("formulaTestCase101", newFormulaTestCase.getName());
        assertEquals(2, newFormulaTestCase.getFormulaTestInputValues().length);
        assertNotNull(newFormulaTestCase.getFormulaTestInputValue("foo1"));
        assertNotNull(newFormulaTestCase.getFormulaTestInputValue("foo2"));
    }

    @Test
    public void testNewAndDeleteFormulaTestInputValue() {
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

    @Test
    public void testAddOrDeleteFormulaTestInputValues() {
        IFormulaTestInputValue value1 = formulaTestCase.newFormulaTestInputValue();
        value1.setIdentifier("value1");
        IFormulaTestInputValue value2 = formulaTestCase.newFormulaTestInputValue();
        value2.setIdentifier("value2");
        IFormulaTestInputValue value3 = formulaTestCase.newFormulaTestInputValue();
        value3.setIdentifier("value3");

        String[] newValues = new String[] { "value1", "value3", "value2" };
        assertTrue(formulaTestCase.addOrDeleteFormulaTestInputValues(newValues, ipsProject));
        IFormulaTestInputValue[] valuesNew = formulaTestCase.getFormulaTestInputValues();
        assertEquals(value1, valuesNew[0]);
        assertEquals(value3, valuesNew[1]);
        assertEquals(value2, valuesNew[2]);
        assertEquals(3, newValues.length);
        assertFalse(formulaTestCase.addOrDeleteFormulaTestInputValues(newValues, ipsProject));

        newValues = new String[] { "value1", "value4", "value2", "value5" };
        assertTrue(formulaTestCase.addOrDeleteFormulaTestInputValues(newValues, ipsProject));
        valuesNew = formulaTestCase.getFormulaTestInputValues();
        assertEquals(value1, valuesNew[0]);
        assertEquals("value4", valuesNew[1].getIdentifier());
        assertEquals(value2, valuesNew[2]);
        assertEquals("value5", valuesNew[3].getIdentifier());
        assertEquals(4, newValues.length);
        assertFalse(formulaTestCase.addOrDeleteFormulaTestInputValues(newValues, ipsProject));
    }

    @Test
    public void testIsFormulaTestCaseEmpty() {
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

    @Test
    public void testGenerateUniqueNameForFormulaTestCase() {
        formulaTestCase.setName("Test");
        assertEquals("Test", formulaTestCase.generateUniqueNameForFormulaTestCase("Test"));

        IFormulaTestCase ftc1 = formula.newFormulaTestCase();
        ftc1.setName(ftc1.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (2)", ftc1.getName());

        IFormulaTestCase ftcNew = formula.newFormulaTestCase();
        ftcNew.setName(ftcNew.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (3)", ftcNew.getName());

        ftcNew = formula.newFormulaTestCase();
        ftcNew.setName(ftcNew.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (4)", ftcNew.getName());

        ftc1.setName("X");
        ftcNew = formula.newFormulaTestCase();
        ftcNew.setName(ftcNew.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (2)", ftcNew.getName());

        ftcNew = formula.newFormulaTestCase();
        ftcNew.setName(ftcNew.generateUniqueNameForFormulaTestCase("Test"));
        assertEquals("Test (5)", ftcNew.getName());

    }

    @Test
    public void testValidate_DuplicateName() throws CoreException {
        formulaTestCase.setName("Test");
        MessageList ml = formulaTestCase.validate(formulaTestCase.getIpsProject());
        assertNull(ml.getMessageByCode(IFormulaTestCase.MSGCODE_DUPLICATE_NAME));

        IFormulaTestCase ftc = formula.newFormulaTestCase();
        ftc.setName(formulaTestCase.getName());
        ml = formulaTestCase.validate(null);
        assertNotNull(ml.getMessageByCode(IFormulaTestCase.MSGCODE_DUPLICATE_NAME));

        ftc.setName(ftc.generateUniqueNameForFormulaTestCase(formulaTestCase.getName()));
        ml = formulaTestCase.validate(null);
        assertNull(ml.getMessageByCode(IFormulaTestCase.MSGCODE_DUPLICATE_NAME));
    }

}
