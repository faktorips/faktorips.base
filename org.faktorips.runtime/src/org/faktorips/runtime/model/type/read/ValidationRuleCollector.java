/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type.read;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.model.annotation.IpsValidationRule;
import org.faktorips.runtime.model.annotation.IpsValidationRules;
import org.faktorips.runtime.model.type.ValidationRule;

public class ValidationRuleCollector extends TypePartCollector<ValidationRule, ValidationRuleDescriptor> {

    public ValidationRuleCollector() {
        super(Arrays.<AnnotationProcessor<?, ValidationRuleDescriptor>> asList(new IpsValidationRuleProcessor()));
    }

    @Override
    protected String[] getNames(AnnotatedDeclaration annotatedDeclaration) {
        if (annotatedDeclaration.is(IpsValidationRules.class)) {
            return annotatedDeclaration.get(IpsValidationRules.class).value();
        } else {
            return NO_NAMES;
        }
    }

    @Override
    protected ValidationRuleDescriptor createDescriptor() {
        return new ValidationRuleDescriptor();
    }

    static class IpsValidationRuleProcessor extends AnnotationProcessor<IpsValidationRule, ValidationRuleDescriptor> {

        public IpsValidationRuleProcessor() {
            super(IpsValidationRule.class);
        }

        @Override
        public String getName(IpsValidationRule annotation) {
            return annotation.name();
        }

        @Override
        public void process(ValidationRuleDescriptor descriptor,
                AnnotatedDeclaration annotatedDeclaration,
                AnnotatedElement annotatedElement) {
            descriptor.setMethod((Method)annotatedElement);
        }
    }
}
