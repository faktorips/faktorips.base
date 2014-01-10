/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;

public class PolicyCmptImplClassJaxbAnnGen extends AbstractAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        if (generatorModelNode instanceof XPolicyCmptClass) {
            XPolicyCmptClass xPolicyCmptClass = (XPolicyCmptClass)generatorModelNode;

            String unqualifiedName = xPolicyCmptClass.getImplClassName();
            codeBuilder.annotationLn("javax.xml.bind.annotation.XmlRootElement", "name", unqualifiedName);
        }
        return codeBuilder.getFragment();
    }

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS;
    }

    @Override
    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        return true;
    }

}
