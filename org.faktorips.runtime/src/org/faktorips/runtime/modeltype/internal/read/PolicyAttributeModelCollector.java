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
import org.faktorips.runtime.modeltype.IPolicyAttributeModel;
import org.faktorips.runtime.modeltype.internal.ModelType;
import org.faktorips.runtime.modeltype.internal.PolicyModelConstantAttribute;
import org.faktorips.runtime.modeltype.internal.PolicyModel;
import org.faktorips.runtime.modeltype.internal.PolicyAttributeModel;

public class PolicyAttributeModelCollector extends
AttributeCollector<IPolicyAttributeModel, PolicyAttributeModelCollector.PolicyAttributeDescriptor> {

    @SuppressWarnings("unchecked")
    // Compiler does not like generics and varargs
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6227971
    public PolicyAttributeModelCollector() {
        super(Arrays.asList(new IpsAttributeProcessor<PolicyAttributeDescriptor>(),
                new IpsAttributeSetterProcessor<PolicyAttributeDescriptor>()));
    }

    @Override
    protected PolicyAttributeDescriptor createDescriptor() {
        return new PolicyAttributeDescriptor();
    }

    static class PolicyAttributeDescriptor extends AbstractAttributeDescriptor<IPolicyAttributeModel> {

        @Override
        protected IPolicyAttributeModel createValid(ModelType modelType) {
            if (getAnnotatedElement() instanceof Field) {
                boolean changingOverTime = isChangingOverTime();
                return new PolicyModelConstantAttribute(modelType, (Field)getAnnotatedElement(), changingOverTime);
            } else {
                return new PolicyAttributeModel((PolicyModel)modelType, (Method)getAnnotatedElement(),
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
