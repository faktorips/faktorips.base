/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ProductCmptInterfaceBuilderTest extends ProductCmptTypeBuilderTest {

    private ProductCmptInterfaceBuilder builder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        builder = new ProductCmptInterfaceBuilder(builderSet);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(productCmptType);
        assertTrue(generatedJavaElements.contains(javaInterface));
    }

}
