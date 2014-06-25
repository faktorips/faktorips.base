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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.runtime.jaxb.ProductConfigurationXmlAdapter;

public class PolicyCmptImplClassProductConfigurationJaxbGen extends AbstractAnnotationGenerator {

    public PolicyCmptImplClassProductConfigurationJaxbGen() {
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.annotationLn("javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter",
                "value = ProductConfigurationXmlAdapter.class");
        builder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name = \"product-component.id\"");
        builder.addImport(ProductConfigurationXmlAdapter.class);
        return builder.getFragment();
    }

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_PRODUCTCONFIGURATION_FIELD;
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return true;
    }

}
