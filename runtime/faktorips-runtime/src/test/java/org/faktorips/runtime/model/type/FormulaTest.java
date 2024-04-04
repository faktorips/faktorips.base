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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.IConfigurableModelObject;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.internal.ProductComponent;
import org.faktorips.runtime.internal.ProductComponentGeneration;
import org.faktorips.runtime.model.IpsModel;
import org.faktorips.runtime.model.annotation.IpsChangingOverTime;
import org.faktorips.runtime.model.annotation.IpsFormula;
import org.faktorips.runtime.model.annotation.IpsFormulas;
import org.faktorips.runtime.model.annotation.IpsProductCmptType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FormulaTest {

    @Mock
    private IRuntimeRepository repository;

    @Test
    public void testGetAndSetFormulaText() {
        Product product = new Product();
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
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
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
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
        ProductCmptType productCmptType = IpsModel.getProductCmptType(product);
        Formula formula = productCmptType.getFormula("formula");
        String formulaText = formula.getFormulaText(product, null);
        assertNull(formulaText);
        formula.setFormulaText(product, null, "111");
        formulaText = formula.getFormulaText(product, null);
        assertThat(formulaText, is("111"));
    }

    @IpsProductCmptType(name = "MyProduct")
    @IpsFormulas({ "formula", "formulaGen" })
    @IpsChangingOverTime(ProductGen.class)
    private class Product extends ProductComponent {

        public Product() {
            super(repository, "id", "kindId", "versionId");
        }

        @IpsFormula(name = "formula")
        public Integer computeFormula(String param) throws FormulaExecutionException {
            return (Integer)getFormulaEvaluator().evaluate("computeFormula", param);
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
