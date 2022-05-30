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

import org.faktorips.runtime.model.annotation.IpsConfiguredValidationRule;
import org.faktorips.runtime.model.annotation.IpsExtensionProperties;
import org.faktorips.runtime.model.annotation.IpsValidationRule;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.Type;
import org.faktorips.runtime.model.type.ValidationRule;

public class ValidationRuleDescriptor extends PartDescriptor<ValidationRule> {

    private AnnotatedElement annotatedElement;
    private Method method;

    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    public void setAnnotatedElement(AnnotatedElement annotatedElement) {
        this.annotatedElement = annotatedElement;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public ValidationRule create(ModelElement parentElement) {
        Type type = (Type)parentElement;
        return new ValidationRule(type, method.getAnnotation(IpsValidationRule.class),
                method.getAnnotation(IpsConfiguredValidationRule.class),
                method.getAnnotation(IpsExtensionProperties.class));
    }
}
