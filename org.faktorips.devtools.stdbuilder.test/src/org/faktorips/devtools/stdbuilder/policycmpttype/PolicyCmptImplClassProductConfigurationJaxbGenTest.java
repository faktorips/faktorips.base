/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
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
                + System.getProperty("line.separator") + "@XmlAttribute(name = \"product-component.id\")"
                + System.getProperty("line.separator");
        assertEquals(testSsourcecode, codeFragment.getSourcecode());
        ImportDeclaration importDeclaration = codeFragment.getImportDeclaration();
        assertTrue(importDeclaration.getImports().contains("org.faktorips.runtime.jaxb.ProductConfigurationXmlAdapter"));

    }

    @Test
    public void testGetAnnotatedJavaElementType() throws Exception {
        assertEquals(AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_PRODUCTCONFIGURATION_FIELD,
                jaxbGen.getAnnotatedJavaElementType());
    }

    @Test
    public void testIsGenerateAnnotationFor() throws Exception {
        assertTrue(jaxbGen.isGenerateAnnotationFor(null));
    }
}
