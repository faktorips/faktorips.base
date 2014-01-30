/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;

public class PolicyCmptImplClassAttributeFieldJaxbGen extends AbstractAnnotationGenerator {

    public PolicyCmptImplClassAttributeFieldJaxbGen() {
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        try {
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
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD;
    }

    @Override
    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        return true;
    }

}
