/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.validation;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XValidationRule;
import org.faktorips.runtime.model.annotation.IpsConfiguredValidationRule;
import org.faktorips.runtime.model.annotation.IpsValidationRule;

public class ValidationRuleAnnGen implements IAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        XValidationRule xValidationRule = (XValidationRule)modelNode;
        String name = xValidationRule.getName();
        String msgCode = xValidationRule.getConstantNameMessageCode();
        String severityJavaCode = xValidationRule.getSeverityConstant();

        JavaCodeFragmentBuilder annotationLn = new JavaCodeFragmentBuilder().annotationLn(IpsValidationRule.class,
                "name = \"" + name + "\", msgCode = " + msgCode + ", severity = " + severityJavaCode);

        if (xValidationRule.isConfigured()) {
            boolean changingOverTime = xValidationRule.isChangingOverTime();
            boolean defaultActivated = xValidationRule.getValidationRule().isActivatedByDefault();

            annotationLn.annotationLn(IpsConfiguredValidationRule.class,
                    "changingOverTime = " + changingOverTime + ", defaultActivated = " + defaultActivated);
        }
        return annotationLn.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode instanceof XValidationRule;
    }
}
