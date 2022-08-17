/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.policycmpt;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.IAnnotationGeneratorFactory;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.stdbuilder.xtend.association.SimpleAssociationAnnGen;
import org.faktorips.runtime.model.annotation.IpsAssociationAdder;
import org.faktorips.runtime.model.annotation.IpsAssociationRemover;

public class PolicyCmptAssociationAnnGenFactory implements IAnnotationGeneratorFactory {

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        switch (type) {
            case POLICY_CMPT_DECL_CLASS_ASSOCIATION_GETTER:
                return new PolicyCmptAssociationGetterAnnGen();
            case POLICY_CMPT_DECL_CLASS_ASSOCIATION_SETTER_ADDER:
                return new SimpleAssociationAnnGen(XPolicyAssociation.class, IpsAssociationAdder.class);
            case POLICY_CMPT_DECL_CLASS_ASSOCIATION_REMOVER:
                return new SimpleAssociationAnnGen(XPolicyAssociation.class, IpsAssociationRemover.class);

            default:
                return null;
        }
    }

}
