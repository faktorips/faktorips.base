/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.junit.Before;
import org.junit.Test;

public class PolicyCmptImplClassProductConfigurationJaxbGenTest {

    private PolicyCmptImplClassProductConfigurationJaxbGen jaxbGen;

    @Before
    public void setUp() {
        jaxbGen = new PolicyCmptImplClassProductConfigurationJaxbGen();
    }

    @Test
    public void testCreateAnnotation() throws Exception {
        JavaCodeFragment codeFragment = jaxbGen.createAnnotation(null);
        assertNotNull(codeFragment);
        String testSsourcecode = "@XmlJavaTypeAdapter(value = ProductConfigurationXmlAdapter.class)"
                + System.lineSeparator() + "@XmlAttribute(name = \"product-component.id\")"
                + System.lineSeparator();
        assertEquals(testSsourcecode, codeFragment.getSourcecode());
        ImportDeclaration importDeclaration = codeFragment.getImportDeclaration();
        assertTrue(
                importDeclaration.getImports().contains("org.faktorips.runtime.jaxb.ProductConfigurationXmlAdapter"));

    }

    @Test
    public void testIsGenerateAnnotationFor() throws Exception {
        assertTrue(jaxbGen.isGenerateAnnotationFor(null));
    }
}
