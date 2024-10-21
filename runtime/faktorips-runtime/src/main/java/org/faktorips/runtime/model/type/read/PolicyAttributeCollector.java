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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.faktorips.runtime.model.annotation.IpsConfiguredAttribute;
import org.faktorips.runtime.model.type.ConstantPolicyAttribute;
import org.faktorips.runtime.model.type.DefaultPolicyAttribute;
import org.faktorips.runtime.model.type.PolicyAttribute;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.Type;

public class PolicyAttributeCollector
        extends AttributeCollector<PolicyAttribute, PolicyAttributeCollector.PolicyAttributeDescriptor> {

    public PolicyAttributeCollector() {
        super(List.of(new IpsAttributeProcessor<>(), new IpsAttributeSetterProcessor<>()));
    }

    @Override
    protected PolicyAttributeDescriptor createDescriptor() {
        return new PolicyAttributeDescriptor();
    }

    protected static class PolicyAttributeDescriptor extends AbstractAttributeDescriptor<PolicyAttribute> {

        @Override
        protected PolicyAttribute createValid(Type type) {
            if (getAnnotatedElement() instanceof Field field) {
                boolean changingOverTime = isChangingOverTime();
                return new ConstantPolicyAttribute(type, field, changingOverTime);
            } else {
                return new DefaultPolicyAttribute((PolicyCmptType)type, (Method)getAnnotatedElement(),
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
