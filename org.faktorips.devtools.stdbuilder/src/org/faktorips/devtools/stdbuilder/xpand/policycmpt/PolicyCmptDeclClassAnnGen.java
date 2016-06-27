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
import org.faktorips.devtools.stdbuilder.xpand.AbstractTypeDeclClassAnnGen;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.XType;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;

public class PolicyCmptDeclClassAnnGen extends AbstractTypeDeclClassAnnGen {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {

        JavaCodeFragment annotation = super.createAnnotation(modelNode);
        annotation.append(createAnnConfiguredBy((XPolicyCmptClass)modelNode));

        return annotation;
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XPolicyCmptClass;
    }

    @Override
    protected JavaCodeFragment createAnnType(XType type) {
        XPolicyCmptClass policy = (XPolicyCmptClass)type;

        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();

        String ipsObjectName = policy.getIpsObjectPartContainer().getQualifiedName();
        codeFragmentBuilder.annotationLn(IpsPolicyCmptType.class, "name = \"" + ipsObjectName + "\"");
        return codeFragmentBuilder.getFragment();
    }

    /**
     * @return an annotation that annotates by which product this policy is configured
     * @see IpsConfiguredBy
     */
    protected JavaCodeFragment createAnnConfiguredBy(XPolicyCmptClass policy) {
        JavaCodeFragmentBuilder codeFragmentBuilder = new JavaCodeFragmentBuilder();
        if (policy.isConfigured()) {
            codeFragmentBuilder.annotationLn(IpsConfiguredBy.class, policy.getProductCmptClassName() + ".class");
        }
        return codeFragmentBuilder.getFragment();
    }

}
