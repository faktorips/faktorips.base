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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.TestEnumType;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.model.ipsobject.ILabel;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IFormulaTestCase;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Jan Ortmann
 */
public class FormulaTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IFormula formula;
    private IProductCmptTypeMethod formulaSignature;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);

        formulaSignature = productCmptType.newProductCmptTypeMethod();
        formulaSignature.setDatatype(Datatype.STRING.getQualifiedName());
        formulaSignature.setName("calculatePremium");
        formulaSignature.setFormulaSignatureDefinition(true);
        formulaSignature.setFormulaName("Premium Calculation");
        formulaSignature.newParameter(Datatype.INTEGER.getQualifiedName(), "param1");
        formulaSignature.newParameter(Datatype.BOOLEAN.getQualifiedName(), "param2");
        formulaSignature.newParameter(policyCmptType.getQualifiedName(), "policy");

        productCmpt = newProductCmpt(productCmptType, "ProductA");
        IProductCmptGeneration gen = productCmpt.getProductCmptGeneration(0);
        formula = gen.newFormula();
        formula.setFormulaSignature(formulaSignature.getFormulaName());
    }

    @Test
    public void testValidate_MissingExpression() throws CoreException {
        formula.setExpression("");
        MessageList ml = formula.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IFormula.MSGCODE_EXPRESSION_IS_EMPTY));

        formula.setExpression("1");
        ml = formula.validate(ipsProject);
        assertNull(ml.getMessageByCode(IFormula.MSGCODE_EXPRESSION_IS_EMPTY));
    }

    @Test
    public void testValidate_SyntaxErrorInFormula() throws CoreException {
        formula.setExpression("42EUR12");
        MessageList list = formula.validate(ipsProject);
        assertEquals(1, list.size());
        Message msg = list.getMessage(0);
        assertEquals(ExprCompiler.SYNTAX_ERROR, msg.getCode());
        assertEquals(formula, msg.getInvalidObjectProperties()[0].getObject());
        assertEquals(IFormula.PROPERTY_EXPRESSION, msg.getInvalidObjectProperties()[0].getProperty());
    }

    @Test
    public void testValidate_UnknownDatatypeFormula() throws CoreException {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setFormulaName("CalcPremium");
        method.setFormulaSignatureDefinition(true);
        method.setDatatype("");

        formula.setExpression("1");
        formula.setFormulaSignature("CalcPremium");

        MessageList ml = formula.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IFormula.MSGCODE_UNKNOWN_DATATYPE_FORMULA));

        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        ml = formula.validate(ipsProject);
        assertNull(ml.getMessageByCode(IFormula.MSGCODE_UNKNOWN_DATATYPE_FORMULA));
    }

    @Test
    public void testValidate_WrongFormulaDatatype() throws CoreException {
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setFormulaName("CalcPremium");
        method.setFormulaSignatureDefinition(true);
        method.setDatatype(Datatype.INTEGER.getQualifiedName());

        formula.setExpression("\"abc\"");
        formula.setFormulaSignature("CalcPremium");

        MessageList ml = formula.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IFormula.MSGCODE_WRONG_FORMULA_DATATYPE));

        formula.setExpression("1");
        ml = formula.validate(ipsProject);
        assertNull(ml.getMessageByCode(IFormula.MSGCODE_WRONG_FORMULA_DATATYPE));

        // test implicit conversion
        method.setDatatype(Datatype.DECIMAL.getQualifiedName());
        ml = formula.validate(ipsProject);
        assertNull(ml.getMessageByCode(IFormula.MSGCODE_WRONG_FORMULA_DATATYPE));
    }

    @Test
    public void testFindFormulaSignature() throws CoreException {
        assertSame(formulaSignature, formula.findFormulaSignature(ipsProject));

        formula.setFormulaSignature("Unknown");
        assertNull(formula.findFormulaSignature(ipsProject));

        formula.setFormulaSignature("");
        assertNull(formula.findFormulaSignature(ipsProject));
    }

    @Test
    public void testInitFromXml() {
        Element el = getTestDocument().getDocumentElement();
        formula.initFromXml(el);
        assertEquals("PremiumCalculation", formula.getFormulaSignature());
        assertEquals("a + b", formula.getExpression());
        assertEquals(1, formula.getFormulaTestCases().length);
    }

    @Test
    public void testGetEnumDatatypesAllowedInFormula() throws Exception {
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
        EnumDatatype testType = (EnumDatatype)ipsProject.findDatatype("TestEnumType");
        assertNotNull(testType);

        // missing policy component type attribute
        EnumDatatype[] enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length);

        // enum type is the return value of the formula
        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setFormulaSignatureDefinition(true);
        method.setFormulaName("CalcPremium");
        method.setDatatype(testType.getQualifiedName());

        formula.setFormulaSignature("CalcPremium");
        enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(1, enumtypes.length);
        assertEquals(testType, enumtypes[0]);

        // enum type defined as "direct" parameter
        method.setDatatype("Integer");
        enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length);

        IParameter param = method.newParameter(testType.getQualifiedName(), "param1");
        enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(1, enumtypes.length);
        assertEquals(testType, enumtypes[0]);

        // reset to 0
        param.setDatatype(Datatype.STRING.getQualifiedName());

        // enum type used as datatype of policy component type attribute
        IPolicyCmptType policyType = newPolicyCmptType(ipsProject, "Coverage");
        method.newParameter(policyType.getQualifiedName(), "coverage");
        enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length); // 0 because policeType hasn't got an attribute so far

        IPolicyCmptTypeAttribute policyAttr = policyType.newPolicyCmptTypeAttribute();
        policyAttr.setDatatype("Integer");
        enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length); // 0 because policeType hasn't got an attribute with an
        // enum type so far

        IPolicyCmptTypeAttribute policyAttr2 = policyType.newPolicyCmptTypeAttribute();
        policyAttr2.setDatatype(testType.getQualifiedName());

        enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(1, enumtypes.length);
        assertEquals(testType, enumtypes[0]);

        // enums accesible via tables (because they are used as column datatypes)
        policyAttr2.setDatatype("String");
        enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length);
    }

    @Test
    public void testGetEnumDatatypesAllowedInFormulaWithProductCmptTypeAttributes() throws Exception {
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });
        EnumDatatype testType = (EnumDatatype)ipsProject.findDatatype("TestEnumType");
        assertNotNull(testType);

        EnumDatatype[] enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(0, enumtypes.length);

        IAttribute attribute = productCmptType.newAttribute();
        attribute.setDatatype(testType.getQualifiedName());
        attribute.setName("a");

        enumtypes = formula.getEnumDatatypesAllowedInFormula();
        assertEquals(1, enumtypes.length);
        assertEquals(testType, enumtypes[0]);
    }

    @Test
    public void testGetParameterIdentifiersUsedInFormula() throws Exception {
        newDefinedEnumDatatype(ipsProject, new Class[] { TestEnumType.class });

        IPolicyCmptTypeAttribute attributeInput = policyCmptType.newPolicyCmptTypeAttribute();
        attributeInput.setName("attributeInput1");
        attributeInput.setDatatype(Datatype.INTEGER.getQualifiedName());
        attributeInput = policyCmptType.newPolicyCmptTypeAttribute();
        attributeInput.setName("attributeInput2");
        attributeInput.setAttributeType(AttributeType.CHANGEABLE);
        attributeInput.setDatatype(Datatype.STRING.getQualifiedName());

        IProductCmptTypeMethod method = productCmptType.newProductCmptTypeMethod();
        method.setFormulaName("attribute1");
        method.setFormulaSignatureDefinition(true);
        method.setDatatype(Datatype.INTEGER.getQualifiedName());
        IParameter param0 = method.newParameter(Datatype.INTEGER.getQualifiedName(), "param1");
        IParameter param1 = method.newParameter(Datatype.INTEGER.getQualifiedName(), "param2");
        IParameter param2 = method.newParameter(policyCmptType.getQualifiedName(), "policyInputX");

        formula.setFormulaSignature(method.getFormulaName());
        formula.setExpression("param1 + param2 + policyInputX.attributeInput1");

        List<String> identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        assertEquals(3, identifierInFormula.size());
        assertTrue(identifierInFormula.contains("param1"));
        assertTrue(identifierInFormula.contains("param2"));
        assertTrue(identifierInFormula.contains("policyInputX.attributeInput1"));

        formula.setExpression("param1+param2*2+policyInputX.attributeInput1");
        identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        assertEquals(3, identifierInFormula.size());
        assertTrue(identifierInFormula.contains("param1"));
        assertTrue(identifierInFormula.contains("param2"));
        assertTrue(identifierInFormula.contains("policyInputX.attributeInput1"));

        formula.setExpression("param1x+Xparam2*2+policyInputX.attributeInput1");
        identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        // check wrong number of identifiers
        assertFalse(identifierInFormula.size() == 3);

        // check with empty formula
        formula.setExpression(null);
        identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        assertEquals(0, identifierInFormula.size());

        // check with WENN formula (implicit cast e.g. Integer)
        formula.setExpression("WENN(policyInputX.attributeInput2 = \"1\"; 1; 10)");
        identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        assertEquals(1, identifierInFormula.size());
        assertTrue(identifierInFormula.contains("policyInputX.attributeInput2"));

        // check with WENN formula (binary operation exact match?)
        formula.setExpression("WENN(policyInputX.attributeInput1 = 1;1;10)");
        identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        assertEquals(1, identifierInFormula.size());
        assertTrue(identifierInFormula.contains("policyInputX.attributeInput1"));

        param0.delete();
        param1.delete();
        param2.delete();
        param0 = method.newParameter("TestEnumType", "testParam");

        // check with WENN formula and operation with implicit casting
        // (e.g. the first argument of a formula is an enum type)
        formula.setExpression("WENN(testParam = TestEnumType.1;1;10)");
        identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        assertEquals(1, identifierInFormula.size());
        assertTrue(identifierInFormula.contains("testParam"));

        // check table access formula that matches implicit conversions
        // e.g. TableUsage.value1(key1, 1), key1 is Integer

        ITableStructure tableStructure = (ITableStructure)newIpsObject(ipsProject, IpsObjectType.TABLE_STRUCTURE,
                "Table");
        IColumn col = tableStructure.newColumn();
        col.setDatatype("Integer");
        col.setName("column1");
        col = tableStructure.newColumn();
        col.setDatatype("Integer");
        col.setName("column2");
        col = tableStructure.newColumn();
        col.setDatatype("Integer");
        col.setName("column3");
        IUniqueKey key = tableStructure.newUniqueKey();
        key.addKeyItem("column1");
        key.addKeyItem("column2");

        ITableContents tableContents = (ITableContents)newIpsObject(ipsProject, IpsObjectType.TABLE_CONTENTS,
                "TableContents");
        tableContents.setTableStructure(tableStructure.getQualifiedName());

        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setRoleName("Table");
        structureUsage.addTableStructure(tableStructure.getQualifiedName());
        productCmpt.fixAllDifferencesToModel(ipsProject);

        ITableContentUsage tableContentUsage = ((IProductCmptGeneration)formula.getParent()).newTableContentUsage();
        tableContentUsage.setStructureUsage("Table");
        tableContentUsage.setTableContentName("TableContents");

        param0.delete();
        method.newParameter("Integer", "testParam");

        formula.setExpression("Table.column3(testParam;1)");

        identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        assertEquals(1, identifierInFormula.size());
        assertTrue(identifierInFormula.contains("testParam"));

        // attributes of the product component type can be used within the formula withinout
        // specifing them
        // as an parameter and the shouldn't appear in the identifier list of the formula
        IAttribute attr = productCmptType.newAttribute();
        attr.setName("hello");
        attr.setDatatype(Datatype.INTEGER.getName());
        formula.setExpression("hello");
        ExprCompiler compiler = formula.newExprCompiler(ipsProject);
        CompilationResult result = compiler.compile(formula.getExpression());
        assertTrue(result.successfull());
        identifierInFormula = Arrays.asList(formula.getParameterIdentifiersUsedInFormula(ipsProject));
        assertEquals(0, identifierInFormula.size());
    }

    @Test
    public void testMoveFormulaTestCases() {
        IFormulaTestCase ftc0 = formula.newFormulaTestCase();
        IFormulaTestCase ftc1 = formula.newFormulaTestCase();
        IFormulaTestCase ftc2 = formula.newFormulaTestCase();
        IFormulaTestCase ftc3 = formula.newFormulaTestCase();
        formula.moveFormulaTestCases(new int[] { 1, 3 }, true);
        IFormulaTestCase[] ftcs = formula.getFormulaTestCases();
        assertEquals(ftc1, ftcs[0]);
        assertEquals(ftc0, ftcs[1]);
        assertEquals(ftc3, ftcs[2]);
        assertEquals(ftc2, ftcs[3]);

        formula.moveFormulaTestCases(new int[] { 0, 2 }, false);
        ftcs = formula.getFormulaTestCases();
        assertEquals(ftc0, ftcs[0]);
        assertEquals(ftc1, ftcs[1]);
        assertEquals(ftc2, ftcs[2]);
        assertEquals(ftc3, ftcs[3]);
    }

    @Test
    public void testFormulaNewDeleteTest() throws Exception {
        // tests for creating and deleting of the formula test case
        assertEquals(0, formula.getFormulaTestCases().length);

        IFormulaTestCase formulaTest1 = formula.newFormulaTestCase();
        IFormulaTestCase formulaTest2 = formula.newFormulaTestCase();
        formulaTest1.setName("formulaTest1");
        formulaTest2.setName("formulaTest2");

        assertEquals(formulaTest1, formula.getFormulaTestCase(formulaTest1.getName()));
        assertEquals(formulaTest2, formula.getFormulaTestCase(formulaTest2.getName()));
        assertEquals(2, formula.getFormulaTestCases().length);

        formulaTest1.delete();
        formulaTest2.delete();
        assertEquals(0, formula.getFormulaTestCases().length);
        assertNull(formula.getFormulaTestCase(formulaTest1.getName()));
    }

    @Test
    public void testGetExprCompiler() throws CoreException {
        assertNotNull(formula.newExprCompiler(ipsProject));
    }

    @Test
    public void testSetFormulaSignature() {
        testPropertyAccessReadWrite(IFormula.class, IFormula.PROPERTY_FORMULA_SIGNATURE_NAME, formula, "ComputePremium");
    }

    @Test
    public void testSetExpression() {
        testPropertyAccessReadWrite(IFormula.class, IFormula.PROPERTY_EXPRESSION, formula, "a + b");
    }

    @Test
    public void testGetCaption() throws CoreException {
        ILabel label = formulaSignature.getLabel(Locale.GERMAN);
        label.setValue("foo");
        assertEquals("foo", formula.getCaption(Locale.GERMAN));
    }

    @Test
    public void testGetCaptionNotExistent() throws CoreException {
        assertNull(formula.getCaption(Locale.TAIWAN));
    }

    @Test
    public void testGetCaptionNullPointer() throws CoreException {
        try {
            formulaSignature.getCaption(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetLastResortCaption() {
        formula.setFormulaSignature("blub");
        assertEquals("Blub", formula.getLastResortCaption());
    }

}
