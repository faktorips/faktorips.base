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

package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
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

        ProductCmptBuilder productCmptBuilder = new ProductCmptBuilder(builderSet, DefaultBuilderSet.KIND_PRODUCT_CMPT);
        builder = new ProductCmptGenerationCuBuilder(builderSet, DefaultBuilderSet.KIND_PRODUCT_CMPT_GENERATION,
                productCmptBuilder);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(productCmpt);
        assertTrue(generatedJavaElements.contains(getGeneratedJavaClass("___20100320")));
    }

    private IType getGeneratedJavaClass(String generationNamePart) {
        return getGeneratedJavaType(productCmpt, true, builder.getKindId(), PRODUCT_CMPT_NAME + generationNamePart);
    }

}
