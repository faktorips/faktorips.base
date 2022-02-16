/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.lacksMessageCode;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class FormulaTest extends AbstractIpsPluginTest {

    private static final String FORMULA_NAME = "formula";
    private static final String FORMULA_ID = "formulaId";

    private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IIpsProject ipsProject;
    private IProductCmptTypeMethod signature;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsProject = newIpsProject("TestProject");
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        signature = productCmptType.newFormulaSignature(FORMULA_NAME);
    }

    @Test
    public void testGetTableContentUsages_ProductCmptGeneration() throws Exception {
        Formula formula = new Formula(generation, FORMULA_ID);
        assertEquals(0, formula.getTableContentUsages().length);

        ITableStructureUsage structureUsageGen = productCmptType.newTableStructureUsage();
        structureUsageGen.setRoleName("RateTableGen");
        ITableContentUsage contentUsageGen = generation.newTableContentUsage(structureUsageGen);

        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setChangingOverTime(false);
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage = productCmpt.newPropertyValue(structureUsageGen, ITableContentUsage.class);

        ITableContentUsage[] tableContentUsages = formula.getTableContentUsages();
        assertEquals(2, tableContentUsages.length);
        List<ITableContentUsage> asList = Arrays.asList(tableContentUsages);
        assertTrue(asList.contains(contentUsageGen));
        assertTrue(asList.contains(contentUsage));
    }

    @Test
    public void testGetTableContentUsages_ProductCmpt() throws Exception {
        Formula formula = new Formula(productCmpt, FORMULA_ID);
        assertEquals(0, formula.getTableContentUsages().length);

        ITableStructureUsage structureUsageGen = productCmptType.newTableStructureUsage();
        structureUsageGen.setRoleName("RateTable");
        ITableContentUsage contentUsageGen = generation.newTableContentUsage(structureUsageGen);

        ITableStructureUsage structureUsage = productCmptType.newTableStructureUsage();
        structureUsage.setChangingOverTime(false);
        structureUsage.setRoleName("RateTable");
        ITableContentUsage contentUsage = productCmpt.newPropertyValue(structureUsageGen, ITableContentUsage.class);

        ITableContentUsage[] tableContentUsages = formula.getTableContentUsages();
        assertEquals(1, tableContentUsages.length);
        List<ITableContentUsage> asList = Arrays.asList(tableContentUsages);
        assertFalse(asList.contains(contentUsageGen));
        assertTrue(asList.contains(contentUsage));
    }

    @Test
    public void testFindMatchingProductCmptTypeAttributes_changingOverTime() throws Exception {
        IAttribute changingAttr = productCmptType.newProductCmptTypeAttribute("test1");
        IProductCmptTypeAttribute staticAttr = productCmptType.newProductCmptTypeAttribute("test2");
        staticAttr.setChangingOverTime(false);
        Formula formula = new Formula(generation, FORMULA_ID);

        List<IAttribute> matchingProductCmptTypeAttributes = formula.findMatchingProductCmptTypeAttributes();

        assertThat(matchingProductCmptTypeAttributes, hasItems(changingAttr, staticAttr));
        assertEquals(2, matchingProductCmptTypeAttributes.size());
    }

    @Test
    public void testFindMatchingProductCmptTypeAttributes_NOTchangingOverTime() throws Exception {
        productCmptType.newProductCmptTypeAttribute("test1");
        IProductCmptTypeAttribute staticAttr = productCmptType.newProductCmptTypeAttribute("test2");
        staticAttr.setChangingOverTime(false);
        Formula formula = new Formula(productCmpt, "formula");

        List<IAttribute> matchingProductCmptTypeAttributes = formula.findMatchingProductCmptTypeAttributes();

        assertThat(matchingProductCmptTypeAttributes, hasItems((IAttribute)staticAttr));
        assertEquals(1, matchingProductCmptTypeAttributes.size());
    }

    @Test
    public void testToXml() {
        IFormula formula = generation.newFormula(signature);
        assertThat(formula.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));

        formula.setExpression("expression");

        Element el = formula.toXml(newDocument());
        Formula copy = new Formula(generation, "copy");
        copy.initFromXml(el);
        assertThat(copy.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));
        assertThat(copy.getExpression(), is("expression"));
    }

    @Test
    public void testToXml_InheritedValue() {
        IFormula templateFormula = createTemplateFormula();
        IFormula formula = generation.newFormula(signature);
        formula.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        formula.setExpression("expression");
        templateFormula.setExpression("templateExpression");

        Element el = formula.toXml(newDocument());

        Formula copy = new Formula(generation, "copy");
        copy.initFromXml(el);
        assertThat(copy.getTemplateValueStatus(), is(TemplateValueStatus.INHERITED));
        assertThat(copy.getExpression(), is("templateExpression"));
    }

    @Test
    public void testGetExpression_DefinedValue() {
        IFormula formula = generation.newFormula(signature);
        assertThat(formula.getTemplateValueStatus(), is(TemplateValueStatus.DEFINED));

        formula.setExpression("expression");
        assertThat(formula.getExpression(), is("expression"));
    }

    @Test
    public void testValidate_EmptyExpressionShouldBeAllowedForAnUndefinedValue() {
        IFormula templateFormula = createTemplateFormula();
        templateFormula.setTemplateValueStatus(TemplateValueStatus.UNDEFINED);
        templateFormula.setExpression("");

        MessageList messages = templateFormula.validate(ipsProject);
        assertThat(messages, lacksMessageCode(IExpression.MSGCODE_EXPRESSION_IS_EMPTY));
    }

    @Test
    public void testValidate_EmptyExpressionShouldNotBeAllowedForAnInheritedValue() {
        IFormula formula = generation.newFormula(signature);
        formula.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        formula.setExpression("");

        IFormula templateFormula = createTemplateFormula();
        templateFormula.setExpression("");

        assertThat(formula.getExpression(), is(""));

        MessageList messages = formula.validate(ipsProject);
        assertThat(messages, hasMessageCode(IExpression.MSGCODE_EXPRESSION_IS_EMPTY));
    }

    @Test
    public void testGetExpression_InheritedValue() {
        IFormula templateFormula = createTemplateFormula();
        IFormula formula = generation.newFormula(signature);
        formula.setTemplateValueStatus(TemplateValueStatus.INHERITED);

        formula.setExpression("expression");
        templateFormula.setExpression("templateExpression");

        assertThat(formula.getExpression(), is("templateExpression"));
    }

    @Test
    public void testGetExpression_InheritedValueButTemplateIsMissing() {
        IFormula formula = generation.newFormula(signature);
        productCmpt.setTemplate("There is no spoon");

        formula.setTemplateValueStatus(TemplateValueStatus.INHERITED);
        formula.setExpression("expression");

        assertThat(formula.getExpression(), is("expression"));
    }

    private IFormula createTemplateFormula() {
        IProductCmpt template = newProductTemplate(productCmptType, "Template");
        IProductCmptGeneration templateGen = template.getProductCmptGeneration(0);
        IFormula templateFormula = templateGen.newFormula(signature);
        productCmpt.setTemplate(template.getQualifiedName());
        return templateFormula;
    }
}
