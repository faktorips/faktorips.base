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

import org.faktorips.runtime.modeltype.IPolicyAssociationModel;
import org.faktorips.runtime.modeltype.internal.ModelType;
import org.faktorips.runtime.modeltype.internal.PolicyAssociationModel;

public class PolicyAssociationModelCollector extends
        AssociationCollector<IPolicyAssociationModel, PolicyAssociationModelCollector.PolicyAssociationDescriptor> {

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

    static class PolicyAssociationDescriptor extends AbstractAssociationDescriptor<IPolicyAssociationModel> {

        @Override
        public IPolicyAssociationModel createValid(ModelType modelType) {
            return new PolicyAssociationModel(modelType, getAnnotatedElement());
        }

    }

}
