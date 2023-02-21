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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.junit.Before;
import org.junit.Test;

public class PolicyCmptImplClassProductConfigurationJaxbGenTest {

    private PolicyCmptImplClassProductConfigurationJaxbGen jaxbGen;
    private AbstractGeneratorModelNode modelNode;
    private GeneratorConfig generatorConfig;

    @Before
    public void setUp() {
        jaxbGen = new PolicyCmptImplClassProductConfigurationJaxbGen();
        modelNode = mock(AbstractGeneratorModelNode.class);
        generatorConfig = mock(GeneratorConfig.class);
        when(modelNode.getGeneratorConfig()).thenReturn(generatorConfig);
        when(generatorConfig.getJaxbSupport()).thenReturn(JaxbSupportVariant.ClassicJAXB);
    }

    @Test
    public void testCreateAnnotation_Jaxb() throws Exception {
        JavaCodeFragment codeFragment = jaxbGen.createAnnotation(modelNode);
        assertNotNull(codeFragment);
        String testSsourcecode = "@XmlJavaTypeAdapter(value = ProductConfigurationXmlAdapter.class)"
                + System.lineSeparator() + "@XmlAttribute(name = \"product-component.id\")"
                + System.lineSeparator();
        assertEquals(testSsourcecode, codeFragment.getSourcecode());
        ImportDeclaration importDeclaration = codeFragment.getImportDeclaration();
        assertThat(importDeclaration.getImports(),
                hasItems("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter",
                        "javax.xml.bind.annotation.XmlAttribute",
                        "org.faktorips.runtime.xml.javax.ProductConfigurationXmlAdapter"));

    }

    @Test
    public void testCreateAnnotation_Jakarta() throws Exception {
        when(generatorConfig.getJaxbSupport()).thenReturn(JaxbSupportVariant.JakartaXmlBinding);
        JavaCodeFragment codeFragment = jaxbGen.createAnnotation(modelNode);
        assertNotNull(codeFragment);
        String testSsourcecode = "@XmlJavaTypeAdapter(value = ProductConfigurationXmlAdapter.class)"
                + System.lineSeparator() + "@XmlAttribute(name = \"product-component.id\")"
                + System.lineSeparator();
        assertEquals(testSsourcecode, codeFragment.getSourcecode());
        ImportDeclaration importDeclaration = codeFragment.getImportDeclaration();
        assertThat(importDeclaration.getImports(),
                hasItems("jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter",
                        "jakarta.xml.bind.annotation.XmlAttribute",
                        "org.faktorips.runtime.xml.jakarta.ProductConfigurationXmlAdapter"));

    }

    @Test
    public void testIsGenerateAnnotationFor() throws Exception {
        assertTrue(jaxbGen.isGenerateAnnotationFor(null));
    }
}
