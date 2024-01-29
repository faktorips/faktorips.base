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

import java.lang.reflect.Method;
import java.util.Arrays;

import org.faktorips.runtime.model.type.PolicyAssociation;
import org.faktorips.runtime.model.type.Type;

public class PolicyAssociationCollector
        extends AssociationCollector<PolicyAssociation, PolicyAssociationCollector.PolicyAssociationDescriptor> {

    public PolicyAssociationCollector() {
        super(Arrays.<AnnotationProcessor<?, PolicyAssociationDescriptor>> asList(
                new IpsAssociationProcessor<>(),
                new IpsAssociationAdderProcessorNoCardinality<>(),
                new IpsAssociationRemoverProcessor<>()));
    }

    @Override
    protected PolicyAssociationDescriptor createDescriptor() {
        return new PolicyAssociationDescriptor();
    }

    protected static class PolicyAssociationDescriptor extends AbstractAssociationDescriptor<PolicyAssociation> {
        private Method removeMethod;

        @Override
        public PolicyAssociation createValid(Type type) {
            return new PolicyAssociation(type, getAnnotatedElement(), getAddMethod(), removeMethod);
        }

        @Override
        public void setRemoveMethod(Method removeMethod) {
            this.removeMethod = removeMethod;
        }
    }

}
