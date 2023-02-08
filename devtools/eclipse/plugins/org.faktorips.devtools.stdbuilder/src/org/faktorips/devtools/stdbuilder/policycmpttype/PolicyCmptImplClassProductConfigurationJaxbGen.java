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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.JaxbAnnGenFactory.IpsXmlAdapters;
import org.faktorips.devtools.stdbuilder.JaxbAnnGenFactory.JaxbAnnotation;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;

/**
 * Generates JAXB annotations for the product configuration field of policy component type
 * implementations.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_PRODUCTCONFIGURATION_FIELD
 */
public class PolicyCmptImplClassProductConfigurationJaxbGen extends AbstractJaxbAnnotationGenerator {

    public PolicyCmptImplClassProductConfigurationJaxbGen() {
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlJavaTypeAdapter, generatorModelNode),
                "value = ProductConfigurationXmlAdapter.class");
        builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlAttribute, generatorModelNode),
                "name = \"product-component.id\"");
        builder.addImport(getQualifiedName(IpsXmlAdapters.ProductConfigurationXmlAdapter, generatorModelNode));
        return builder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return true;
    }

}
