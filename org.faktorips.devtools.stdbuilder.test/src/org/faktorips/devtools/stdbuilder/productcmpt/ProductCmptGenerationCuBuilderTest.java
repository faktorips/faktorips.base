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

import java.util.GregorianCalendar;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptGenerationCuBuilderTest extends AbstractStdBuilderTest {

    private static final String TEST_FORMULA = "testFormula";

    private static final String PRODUCT_TYPE = "ProductType";

    private static final String PRODUCT_CMPT_NAME = "Product";

    private AbstractProductCuBuilder<IProductCmptGeneration> builder;

    private IProductCmpt productCmpt;

    private IProductCmptGeneration firstGeneration;

    private IProductCmptGeneration secondGeneration;

    private ProductCmptCuBuilder cuBuilder;

    private IProductCmptType productCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        productCmptType = newProductCmptType(ipsProject, PRODUCT_TYPE);

        productCmpt = newProductCmpt(ipsProject, PRODUCT_CMPT_NAME);
        productCmpt.setProductCmptType(PRODUCT_TYPE);
        firstGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        GregorianCalendar firstDate = new GregorianCalendar();
        firstDate.set(2010, 2, 10);
        firstGeneration.setValidFrom(firstDate);
        secondGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        GregorianCalendar secondDate = new GregorianCalendar();
        firstDate.set(2010, 2, 20);
        secondGeneration.setValidFrom(secondDate);
        productCmpt.fixAllDifferencesToModel(ipsProject);

        cuBuilder = new ProductCmptCuBuilder(builderSet);
        builder = new ProductCmptGenerationCuBuilder(builderSet, cuBuilder);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(productCmpt);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass("___20100320")));
    }

    private IType getGeneratedJavaClass(String generationNamePart) {
        return getGeneratedJavaClass(productCmpt, true, PRODUCT_CMPT_NAME + generationNamePart);
    }

    @Test
    public void testIsContainingAvailableFormula_noFormula() throws Exception {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2010, 0, 1));

        assertFalse(builder.isContainingAvailableFormula(generation));
    }

    @Test
    public void testIsContainingAvailableFormula_anyEmptyFormula() throws Exception {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2010, 0, 1));
        generation.newFormula();

        assertFalse(builder.isContainingAvailableFormula(generation));
    }

    @Test
    public void testIsContainingAvailableFormula_anyAvailableFormula() throws Exception {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2010, 0, 1));
        IFormula newFormula = generation.newFormula();
        newFormula.setExpression("anyExpression");

        assertTrue(builder.isContainingAvailableFormula(generation));
    }

    @Test
    public void testIsContainingAvailableFormula_twoFormulas() throws Exception {
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt
                .newGeneration(new GregorianCalendar(2010, 0, 1));
        generation.newFormula();
        IFormula newFormula = generation.newFormula();
        newFormula.setExpression("anyExpression");

        assertTrue(builder.isContainingAvailableFormula(generation));
    }

    @Test
    public void testGetSuperClassQualifiedClassName() throws Exception {
        String superClassQualifiedClassName = builder.getSuperClassQualifiedClassName(firstGeneration);

        assertThat(superClassQualifiedClassName, is("org.faktorips.sample.model.internal.ProductTypeGen"));
    }

    @Test
    public void testGetSuperClassQualifiedClassName_OtherProjectDifferentSettings() throws Exception {
        setGeneratorProperty(ipsProject, StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES,
                Boolean.FALSE.toString());
        // need to recreate builder because builderSet has changed
        builder = new ProductCmptGenerationCuBuilder(builderSet, cuBuilder);
        IIpsProject otherProject = newIpsProject();
        IIpsObjectPath objectPath = otherProject.getIpsObjectPath();
        objectPath.newIpsProjectRefEntry(ipsProject);
        otherProject.setIpsObjectPath(objectPath);
        ProductCmpt productCmpt2 = newProductCmpt(otherProject, "Test2");
        productCmpt2.setProductCmptType(PRODUCT_TYPE);
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt2.newGeneration();
        productCmpt2.fixAllDifferencesToModel(otherProject);

        String superClassQualifiedClassName = builder.getSuperClassQualifiedClassName(generation);

        assertThat(superClassQualifiedClassName, is("org.faktorips.sample.model.ProductTypeGen"));
    }

    @Test
    public void testGetImplementationClass_NoFormula() throws Exception {
        String superClassQualifiedClassName = builder.getImplementationClass(firstGeneration);

        assertThat(superClassQualifiedClassName, is("org.faktorips.sample.model.internal.ProductTypeGen"));
    }

    @Test
    public void testGetImplementationClass_WithFormula() throws Exception {
        productCmptType.newFormulaSignature(TEST_FORMULA);
        productCmpt.fixAllDifferencesToModel(ipsProject);
        firstGeneration.getFormula(TEST_FORMULA).setExpression("abc");

        String superClassQualifiedClassName = builder.getImplementationClass(firstGeneration);

        assertThat(superClassQualifiedClassName, is("org.faktorips.sample.model.internal.Product___20100320"));
    }

}
