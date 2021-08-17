/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptTypeMethod;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptCuBuilderTest extends AbstractStdBuilderTest {

    private static final String TEST_FORMULA = "testFormula";

    private static final String PRODUCT_CMPT_NAME = "Product";

    private static final String PRODUCT_CMPT_TYPE_NAME = "ProductType";

    private ProductCmptCuBuilder builder;

    private IProductCmpt productCmpt;

    private IProductCmptType productCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        productCmptType = newProductCmptType(ipsProject, PRODUCT_CMPT_TYPE_NAME);
        productCmpt = newProductCmpt(productCmptType, PRODUCT_CMPT_NAME);

        builder = new ProductCmptCuBuilder(builderSet);
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
        IFormula newFormula = productCmpt
                .newPropertyValue(new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"), IFormula.class);
        newFormula.setExpression("anyExpression");

        assertTrue(builder.isContainingAvailableFormula(productCmpt));
    }

    @Test
    public void testIsContainingAvailableFormula_twoFormulas() throws Exception {
        productCmpt.newPropertyValues(new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"));
        IFormula newFormula = productCmpt
                .newPropertyValue(new ProductCmptTypeMethod(mock(IProductCmptType.class), "Id"), IFormula.class);
        newFormula.setExpression("anyExpression");

        assertTrue(builder.isContainingAvailableFormula(productCmpt));
    }

    @Test
    public void testGetSuperClassQualifiedClassName() throws Exception {
        String superClassQualifiedClassName = builder.getSuperClassQualifiedClassName(productCmpt);

        assertThat(superClassQualifiedClassName, is("org.faktorips.sample.model.internal.ProductType"));
    }

    @Test
    public void testGetSuperClassQualifiedClassName_OtherProjectDifferentSettings() throws Exception {
        setGeneratorProperty(ipsProject, StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES,
                Boolean.FALSE.toString());
        builder = new ProductCmptCuBuilder(builderSet);
        IIpsProject otherProject = newIpsProject();
        IIpsObjectPath objectPath = otherProject.getIpsObjectPath();
        objectPath.newIpsProjectRefEntry(ipsProject);
        otherProject.setIpsObjectPath(objectPath);
        ProductCmpt productCmpt2 = newProductCmpt(otherProject, "Test2");
        productCmpt2.setProductCmptType(PRODUCT_CMPT_TYPE_NAME);
        productCmpt2.fixAllDifferencesToModel(otherProject);

        String superClassQualifiedClassName = builder.getSuperClassQualifiedClassName(productCmpt2);

        assertThat(superClassQualifiedClassName, is("org.faktorips.sample.model.ProductType"));
    }

    @Test
    public void testGetImplementationClass_NoFormula() throws Exception {
        String superClassQualifiedClassName = builder.getImplementationClass(productCmpt);

        assertThat(superClassQualifiedClassName, is("org.faktorips.sample.model.internal.ProductType"));
    }

    @Test
    public void testGetImplementationClass_WithFormula() throws Exception {
        IProductCmptTypeMethod formulaSignature = productCmptType.newFormulaSignature(TEST_FORMULA);
        formulaSignature.setChangingOverTime(false);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        productCmpt.getFormula(TEST_FORMULA).setExpression("abc");

        String superClassQualifiedClassName = builder.getImplementationClass(productCmpt);

        assertThat(superClassQualifiedClassName, is("org.faktorips.sample.model.internal.Product"));
    }

}
