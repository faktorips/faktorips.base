/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.policycmpt;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;

public class PolicyCmptDeclClassAnnotationGenerator implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XPolicyCmptClass policy = (XPolicyCmptClass)modelNode;

        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        String ipsObjectName = policy.getIpsObjectPartContainer().getQualifiedName();
        codeFragmentBuilder.annotationLn(IpsPolicyCmptType.class, "name = \"" + ipsObjectName + "\"");

        if (policy.isConfigured()) {
            codeFragmentBuilder.annotationLn(IpsConfiguredBy.class, "value = " + policy.getProductCmptClassName()
                    + ".class");
        }

        return codeFragmentBuilder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XPolicyCmptClass;
    }

}
