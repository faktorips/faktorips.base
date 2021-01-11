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
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;

/**
 * Generates JAXB annotations for policy component type fields.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD
 */
public class PolicyCmptImplClassAttributeFieldJaxbGen extends AbstractAnnotationGenerator {

    public PolicyCmptImplClassAttributeFieldJaxbGen() {
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        if (generatorModelNode instanceof XPolicyAttribute) {
            XPolicyAttribute xPolicyAttribute = (XPolicyAttribute)generatorModelNode;

            IPolicyCmptTypeAttribute attribute = xPolicyAttribute.getAttribute();

            String annotationParam = "name=\"" + attribute.getName() + "\"";
            if (!attribute.findDatatype(attribute.getIpsProject()).isPrimitive()) {
                annotationParam += ",nillable=true";
            }
            builder.annotationLn("javax.xml.bind.annotation.XmlElement", annotationParam);
        }
        return builder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return true;
    }

}
