/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.type.read;

import java.util.Arrays;

import org.faktorips.runtime.model.type.PolicyAssociation;
import org.faktorips.runtime.model.type.Type;

public class PolicyAssociationModelCollector extends
AssociationCollector<PolicyAssociation, PolicyAssociationModelCollector.PolicyAssociationDescriptor> {

    @SuppressWarnings("unchecked")
    // Compiler does not like generics and varargs
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6227971
    public PolicyAssociationModelCollector() {
        super(
                Arrays.<AnnotationProcessor<?, PolicyAssociationDescriptor>> asList(new IpsAssociationProcessor<PolicyAssociationModelCollector.PolicyAssociationDescriptor>()));
    }

    @Override
    protected PolicyAssociationDescriptor createDescriptor() {
        return new PolicyAssociationDescriptor();
    }

    static class PolicyAssociationDescriptor extends AbstractAssociationDescriptor<PolicyAssociation> {

        @Override
        public PolicyAssociation createValid(Type type) {
            return new PolicyAssociation(type, getAnnotatedElement());
        }

    }

}
