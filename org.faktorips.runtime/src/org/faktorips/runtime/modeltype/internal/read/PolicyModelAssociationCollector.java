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

import java.util.Arrays;

import org.faktorips.runtime.modeltype.IPolicyModelAssociation;
import org.faktorips.runtime.modeltype.internal.ModelType;
import org.faktorips.runtime.modeltype.internal.PolicyModelAssociation;

public class PolicyModelAssociationCollector extends
        AssociationCollector<IPolicyModelAssociation, PolicyModelAssociationCollector.PolicyAssociationDescriptor> {

    @SuppressWarnings("unchecked")
    // Compiler does not like generics and varargs
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6227971
    public PolicyModelAssociationCollector() {
        super(
                Arrays.<AnnotationProcessor<?, PolicyAssociationDescriptor>> asList(new IpsAssociationProcessor<PolicyModelAssociationCollector.PolicyAssociationDescriptor>()));
    }

    @Override
    protected PolicyAssociationDescriptor createDescriptor() {
        return new PolicyAssociationDescriptor();
    }

    static class PolicyAssociationDescriptor extends AbstractAssociationDescriptor<IPolicyModelAssociation> {

        @Override
        public IPolicyModelAssociation createValid(ModelType modelType) {
            return new PolicyModelAssociation(modelType, getAnnotatedElement());
        }

    }

}
