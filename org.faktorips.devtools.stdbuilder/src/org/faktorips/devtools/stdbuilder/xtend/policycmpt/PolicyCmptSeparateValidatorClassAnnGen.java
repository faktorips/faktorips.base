/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.policycmpt;

import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XValidationRule;
import org.faktorips.runtime.model.annotation.IpsValidationRules;

public class PolicyCmptSeparateValidatorClassAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        return createAnnValidationRules((XPolicyCmptClass)modelNode);
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XPolicyCmptClass;
    }

    protected JavaCodeFragment createAnnValidationRules(XPolicyCmptClass type) {
        Set<XValidationRule> validationRules = type.getValidationRules();
        Class<?> annotationClass = IpsValidationRules.class;
        return createAnnotationWithNodes(annotationClass, validationRules);
    }

    protected JavaCodeFragment createAnnotationWithNodes(Class<?> annotationClass,
            Set<? extends AbstractGeneratorModelNode> nodes) {
        if (nodes.size() > 0) {
            ArrayList<String> nodeNames = new ArrayList<>();
            for (AbstractGeneratorModelNode node : nodes) {
                nodeNames.add(node.getName());
            }
            return new JavaCodeFragmentBuilder().annotationLn(annotationClass,
                    "{\"" + StringUtils.join(nodeNames, "\", \"") + "\"}").getFragment();
        } else {
            return new JavaCodeFragment();
        }
    }

}
