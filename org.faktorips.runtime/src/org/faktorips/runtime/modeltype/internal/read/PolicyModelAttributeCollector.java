/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal.read;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.modeltype.IPolicyModelAttribute;
import org.faktorips.runtime.modeltype.internal.ModelType;
import org.faktorips.runtime.modeltype.internal.PolicyModelConstantAttribute;
import org.faktorips.runtime.modeltype.internal.PolicyModel;
import org.faktorips.runtime.modeltype.internal.PolicyModelAttribute;

public class PolicyModelAttributeCollector extends
AttributeCollector<IPolicyModelAttribute, PolicyModelAttributeCollector.PolicyAttributeDescriptor> {

    @SuppressWarnings("unchecked")
    // Compiler does not like generics and varargs
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6227971
    public PolicyModelAttributeCollector() {
        super(Arrays.asList(new IpsAttributeProcessor<PolicyAttributeDescriptor>(),
                new IpsAttributeSetterProcessor<PolicyAttributeDescriptor>()));
    }

    @Override
    protected PolicyAttributeDescriptor createDescriptor() {
        return new PolicyAttributeDescriptor();
    }

    static class PolicyAttributeDescriptor extends AbstractAttributeDescriptor<IPolicyModelAttribute> {

        @Override
        protected IPolicyModelAttribute createValid(ModelType modelType) {
            if (getAnnotatedElement() instanceof Field) {
                boolean changingOverTime = isChangingOverTime();
                return new PolicyModelConstantAttribute(modelType, (Field)getAnnotatedElement(), changingOverTime);
            } else {
                return new PolicyModelAttribute((PolicyModel)modelType, (Method)getAnnotatedElement(),
                        getSetterMethod(), isChangingOverTime());
            }
        }

        private boolean isChangingOverTime() {
            boolean changingOverTime = false;
            if (getAnnotatedElement().isAnnotationPresent(IpsConfiguredAttribute.class)) {
                changingOverTime = getAnnotatedElement().getAnnotation(IpsConfiguredAttribute.class).changingOverTime();
            }
            return changingOverTime;
        }

    }

}
