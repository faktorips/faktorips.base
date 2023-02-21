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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.stdbuilder.JaxbAnnGenFactory.JaxbAnnotation;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.GeneratorConfig;
import org.junit.Before;
import org.junit.Test;

public class AbstractJaxbAnnotationGeneratorTest {

    private AbstractJaxbAnnotationGenerator jaxbGen;

    @Before
    public void setUp() {
        jaxbGen = new AbstractJaxbAnnotationGenerator() {

            @Override
            public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
                fail("Should not be called in this test");
                return null;
            }

            @Override
            public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode ipsElement) {
                fail("Should not be called in this test");
                return false;
            }

        };
    }

    @Test
    public void testGetQualifiedName_JAXB() {
        GeneratorConfig generatorConfig = mock(GeneratorConfig.class);
        doReturn(JaxbSupportVariant.ClassicJAXB).when(generatorConfig).getJaxbSupport();
        AbstractGeneratorModelNode generatorModelNode = mock(AbstractGeneratorModelNode.class);
        doReturn(generatorConfig).when(generatorModelNode).getGeneratorConfig();

        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlAttribute, generatorModelNode),
                is("javax.xml.bind.annotation.XmlAttribute"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlElement, generatorModelNode),
                is("javax.xml.bind.annotation.XmlElement"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlElementWrapper, generatorModelNode),
                is("javax.xml.bind.annotation.XmlElementWrapper"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlIDREF, generatorModelNode),
                is("javax.xml.bind.annotation.XmlIDREF"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlJavaTypeAdapter, generatorModelNode),
                is("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlRootElement, generatorModelNode),
                is("javax.xml.bind.annotation.XmlRootElement"));
    }

    @Test
    public void testGetQualifiedName_JakartaXmlBinding() {
        GeneratorConfig generatorConfig = mock(GeneratorConfig.class);
        doReturn(JaxbSupportVariant.JakartaXmlBinding).when(generatorConfig).getJaxbSupport();
        AbstractGeneratorModelNode generatorModelNode = mock(AbstractGeneratorModelNode.class);
        doReturn(generatorConfig).when(generatorModelNode).getGeneratorConfig();

        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlAttribute, generatorModelNode),
                is("jakarta.xml.bind.annotation.XmlAttribute"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlElement, generatorModelNode),
                is("jakarta.xml.bind.annotation.XmlElement"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlElementWrapper, generatorModelNode),
                is("jakarta.xml.bind.annotation.XmlElementWrapper"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlIDREF, generatorModelNode),
                is("jakarta.xml.bind.annotation.XmlIDREF"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlJavaTypeAdapter, generatorModelNode),
                is("jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter"));
        assertThat(jaxbGen.getQualifiedName(JaxbAnnotation.XmlRootElement, generatorModelNode),
                is("jakarta.xml.bind.annotation.XmlRootElement"));
    }

}