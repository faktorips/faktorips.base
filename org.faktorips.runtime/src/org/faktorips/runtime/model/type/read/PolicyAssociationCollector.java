/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.type.read;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.faktorips.runtime.model.type.PolicyAssociation;
import org.faktorips.runtime.model.type.Type;

public class PolicyAssociationCollector
        extends AssociationCollector<PolicyAssociation, PolicyAssociationCollector.PolicyAssociationDescriptor> {

    @SuppressWarnings("unchecked")
    // Compiler does not like generics and varargs
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6227971
    public PolicyAssociationCollector() {
        super(Arrays.<AnnotationProcessor<?, PolicyAssociationDescriptor>> asList(
                new IpsAssociationProcessor<PolicyAssociationCollector.PolicyAssociationDescriptor>(),
                new IpsAssociationAdderProcessor<PolicyAssociationCollector.PolicyAssociationDescriptor>(),
                new IpsAssociationRemoverProcessor<PolicyAssociationCollector.PolicyAssociationDescriptor>()));
    }

    @Override
    protected PolicyAssociationDescriptor createDescriptor() {
        return new PolicyAssociationDescriptor();
    }

    protected static class PolicyAssociationDescriptor extends AbstractAssociationDescriptor<PolicyAssociation> {

        private Method addMethod;
        private Method removeMethod;

        @Override
        public PolicyAssociation createValid(Type type) {
            return new PolicyAssociation(type, getAnnotatedElement(), addMethod, removeMethod);
        }

        public void setAddMethod(Method adderMethod) {
            this.addMethod = adderMethod;
        }

        public void setRemoveMethod(Method removeMethod) {
            this.removeMethod = removeMethod;
        }
    }

}
