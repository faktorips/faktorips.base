/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptTypeMethod;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IFormula;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptClassBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptCuBuilderTest extends AbstractStdBuilderTest {

    private static final String PRODUCT_CMPT_NAME = "Product";

    private static final String PRODUCT_CMPT_TYPE_NAME = "ProductType";

    private ProductCmptCuBuilder builder;

    private IProductCmpt productCmpt;

    private ProductCmptType productCmptType;

    @Mock
    private ProductCmptClassBuilder prodCmptImplBuilder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        productCmptType = newProductCmptType(ipsProject, PRODUCT_CMPT_TYPE_NAME);
        productCmpt = newProductCmpt(productCmptType, PRODUCT_CMPT_NAME);
        IIpsSrcFile ipsSrcFile = productCmptType.getIpsSrcFile();
        when(prodCmptImplBuilder.getQualifiedClassName(ipsSrcFile)).thenReturn("productIpsSrcFile");

        builder = new ProductCmptCuBuilder(builderSet);
        builder.setProductCmptImplBuilder(prodCmptImplBuilder);

    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(productCmpt);
        String qualifiedClassNameOfProductCmpt = builder.getQualifiedClassName(productCmpt);
        assertTrue(qualifiedClassNameOfProductCmpt.contains(PRODUCT_CMPT_NAME));

    }

    @Test
    public void testIsContainingAvailableFormula_noFormula() throws Exception {
        assertFalse(builder.isContainingAvailableFormula(productCmpt));
    }

    @Test
    public void testIsContainingAvailableFormula_anyEmptyFormula() throws Exception {
        productCmpt.newPropertyValues(new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"));

        assertFalse(builder.isContainingAvailableFormula(productCmpt));
    }

    @Test
    public void testIsContainingAvailableFormula_anyAvailableFormula() throws Exception {
        IFormula newFormula = productCmpt.newPropertyValue(
                new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"), IFormula.class);
        newFormula.setExpression("anyExpression");

        assertTrue(builder.isContainingAvailableFormula(productCmpt));
    }

    @Test
    public void testIsContainingAvailableFormula_twoFormulas() throws Exception {
        productCmpt.newPropertyValues(new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"));
        IFormula newFormula = productCmpt.newPropertyValue(
                new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"), IFormula.class);
        newFormula.setExpression("anyExpression");

        assertTrue(builder.isContainingAvailableFormula(productCmpt));
    }

}
