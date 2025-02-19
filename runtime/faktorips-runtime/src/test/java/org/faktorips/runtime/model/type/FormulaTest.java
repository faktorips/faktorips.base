/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ValidationContext;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsDocumented;
import org.faktorips.runtime.model.annotation.IpsFormula;
import org.faktorips.runtime.model.annotation.IpsFormulas;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FormulaTest {

    private static ProductCmptType productCmptType = IpsModel.getProductCmptType(Product.class);

    @Mock
    private IRuntimeRepository repository;

    @Test
    public void testValidate_RequiredNotSet() {
        var messageList = new MessageList();
        var product = new Product();
        var requiredFormula = productCmptType.getFormula("requiredFormula");

        requiredFormula.validate(messageList, new ValidationContext(), product, null);

        assertThat(messageList.size(), is(1));
        var message = messageList.getMessage(0);
        assertThat(message.getCode(), is(Formula.MSGCODE_REQUIRED_FORMULA_IS_EMPTY));
        assertThat(messageList.getMessage(0).getText(), containsString("Label of required formula"));
        assertThat(messageList.getMessage(0).getText(), containsString("formula.test.product.runtimeId"));
    }

    @Test
    public void testValidate_RequiredSet() {
        var messageList = new MessageList();
        var product = new Product();
        var requiredFormula = productCmptType.getFormula("requiredFormula");
        requiredFormula.setFormulaText(product, null, "42");

        requiredFormula.validate(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidate_OptionalNotSet() {
        var messageList = new MessageList();
        var product = new Product();
        var optionalFormula = productCmptType.getFormula("formula");

        optionalFormula.validate(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testValidate_OptionalSet() {
        var messageList = new MessageList();
        var product = new Product();
        var optionalFormula = productCmptType.getFormula("formula");
        optionalFormula.setFormulaText(product, null, "23");

        optionalFormula.validate(messageList, new ValidationContext(), product, null);

        assertThat(messageList.isEmpty(), is(true));
    }

    @Test
    public void testGetAndSetFormulaText() {
        Product product = new Product();
        Formula formula = productCmptType.getFormula("formula");
        String formulaText = formula.getFormulaText(product, null);
        assertNull(formulaText);
        formula.setFormulaText(product, null, "111");
        formulaText = formula.getFormulaText(product, null);
        assertThat(formulaText, is("111"));
    }

    @Test
    public void testGetAndSetFormulaText_Gen() {
        Product product = new Product();
        ProductGen productGen = new ProductGen(product);
        when(repository.getLatestProductComponentGeneration(product)).thenReturn(productGen);
        Formula formulaGen = productCmptType.getFormula("formulaGen");
        String formulaGenText = formulaGen.getFormulaText(product, null);
        assertNull(formulaGenText);
        formulaGen.setFormulaText(product, null, "111");
        formulaGenText = formulaGen.getFormulaText(product, null);
        assertThat(formulaGenText, is("111"));
    }

    @Test
    public void testGetAndSetFormulaText_Child() {
        ChildProduct product = new ChildProduct();
        Formula formula = productCmptType.getFormula("formula");
        String formulaText = formula.getFormulaText(product, null);
        assertNull(formulaText);
        formula.setFormulaText(product, null, "111");
        formulaText = formula.getFormulaText(product, null);
        assertThat(formulaText, is("111"));
    }

    @Test
    public void testSetFormulaText_withGeneration_changingOverTime() {
        Product product = new Product();
        ProductGen gen = new ProductGen(product);
        when(repository.getLatestProductComponentGeneration(product)).thenReturn(gen);

        Formula formulaGen = productCmptType.getFormula("formulaGen");
        assertNull(formulaGen.getFormulaText(product, null));

        formulaGen.setFormulaText(gen, "GenText");

        assertThat(formulaGen.getFormulaText(product, null), is("GenText"));
    }

    @Test
    public void testSetFormulaText_withGeneration_nonChanging() {
        Product product = new Product();
        ProductGen gen = new ProductGen(product);

        Formula formula = productCmptType.getFormula("formula");
        assertNull(formula.getFormulaText(product, null));

        formula.setFormulaText(gen, "NewText");

        assertThat(formula.getFormulaText(product, null), is("NewText"));
    }

    @Test
    public void testGetDocumentation() {
        Formula formula = productCmptType.getFormula("formula");
        assertThat(formula.getDescription(Locale.GERMAN), is("Description of formula"));
        assertThat(formula.getLabel(Locale.GERMAN), is("Label of formula"));
    }

    public void testIsRequired() {
        // required formula
        var requiredFormula = productCmptType.getFormula("requiredFormula");
        assertThat(requiredFormula.isRequired(), is(true));

        // optional formula
        var optionalFormula = productCmptType.getFormula("formula");
        assertThat(optionalFormula.isRequired(), is(false));
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsFormulas({ "formula", "requiredFormula", "formulaGen" })
    @IpsChangingOverTime(ProductGen.class)
    @IpsDocumented(bundleName = "org.faktorips.runtime.model.formula.test", defaultLocale = "de")
    private class Product extends ProductComponent {

        public Product() {
            super(repository, "formula.test.product.runtimeId", "kindId", "versionId");
        }

        @IpsFormula(name = "formula")
        public Integer computeFormula(String param) throws FormulaExecutionException {
            return (Integer)getFormulaEvaluator().evaluate("computeFormula", param);
        }

        @IpsFormula(name = "requiredFormula", required = true)
        public Integer computeRequiredFormula(String param) throws FormulaExecutionException {
            return (Integer)getFormulaEvaluator().evaluate("computeRequiredFormula", param);
        }

        @Override
        public IConfigurableModelObject createPolicyComponent() {
            // not used
            return null;
        }

        @Override
        public boolean isChangingOverTime() {
            return true;
        }
    }

    private class ProductGen extends ProductComponentGeneration {

        public ProductGen(Product product) {
            super(product);
        }

        @IpsFormula(name = "formulaGen")
        public String computeFormulaGen() {
            return (String)getFormulaEvaluator().evaluate("computeFormulaGen");
        }
    }

    @IpsProductCmptType(name = "MyChildProduct")
    @IpsFormulas({ "formulaChild" })
    private class ChildProduct extends Product {

        @IpsFormula(name = "formulaChild")
        public Integer computeFormulaChild(String param) throws FormulaExecutionException {
            return (Integer)getFormulaEvaluator().evaluate("computeFormulaChild", param);
        }
    }
}
