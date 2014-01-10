/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptGenerationCuBuilderTest extends AbstractStdBuilderTest {

    private static final String PRODUCT_CMPT_NAME = "Product";

    private ProductCmptGenerationCuBuilder builder;

    private IProductCmpt productCmpt;

    private IProductCmptGeneration firstGeneration;

    private IProductCmptGeneration secondGeneration;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        productCmpt = newProductCmpt(ipsProject, PRODUCT_CMPT_NAME);
        firstGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        GregorianCalendar firstDate = new GregorianCalendar();
        firstDate.set(2010, 2, 10);
        firstGeneration.setValidFrom(firstDate);
        secondGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        GregorianCalendar secondDate = new GregorianCalendar();
        firstDate.set(2010, 2, 20);
        secondGeneration.setValidFrom(secondDate);

        ProductCmptBuilder productCmptBuilder = new ProductCmptBuilder(builderSet);
        builder = new ProductCmptGenerationCuBuilder(builderSet, productCmptBuilder);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(productCmpt);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass("___20100320")));
    }

    private IType getGeneratedJavaClass(String generationNamePart) {
        return getGeneratedJavaClass(productCmpt, true, PRODUCT_CMPT_NAME + generationNamePart);
    }

}
