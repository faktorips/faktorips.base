/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.policycmpt;

import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.java.annotations.AbstractTypeDeclClassAnnGen;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.XType;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XValidationRule;
import org.faktorips.runtime.model.annotation.IpsConfiguredBy;
import org.faktorips.runtime.model.annotation.IpsPolicyCmptType;
import org.faktorips.runtime.model.annotation.IpsValidationRules;

public class PolicyCmptDeclClassAnnGen extends AbstractTypeDeclClassAnnGen {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {

        JavaCodeFragment annotation = super.createAnnotation(modelNode);
        annotation.append(createAnnConfiguredBy((XPolicyCmptClass)modelNode));
        annotation.append(createAnnValidationRules((XPolicyCmptClass)modelNode));
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

    protected JavaCodeFragment createAnnValidationRules(XPolicyCmptClass type) {
        Set<XValidationRule> validationRules = type.getValidationRules();
        Class<?> annotationClass = IpsValidationRules.class;
        return createAnnotationWithNodes(annotationClass, validationRules);
    }

}
