/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.policycmpt.persistence;

import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGenerator;
import org.faktorips.devtools.model.builder.java.annotations.IAnnotationGeneratorFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class PolicyCmptImplClassJpaAnnGenFactory implements IAnnotationGeneratorFactory {

    public PolicyCmptImplClassJpaAnnGenFactory() {
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
        return switch (type) {
            case POLICY_CMPT_IMPL_CLASS -> new PolicyCmptImplClassJpaAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD -> new PolicyCmptImplClassAttributeFieldJpaAnnGen();
            case POLICY_CMPT_DECL_CLASS_ATTRIBUTE_GETTER -> new PolicyCmptImplClassAttributeGetterJpaAnnGen();
            case POLICY_CMPT_DECL_CLASS_ATTRIBUTE_SETTER -> new PolicyCmptImplClassAttributeSetterJpaAnnGen();
            case POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD -> new PolicyCmptImplClassTransientFieldJpaAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD -> new PolicyCmptImplClassAssociationJpaAnnGen();
            default -> null;
        };
    }

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return ipsProject.isPersistenceSupportEnabled();
    }
}
