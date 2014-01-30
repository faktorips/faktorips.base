/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.AnnotationGeneratorFactory;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;

public class PolicyCmptImplClassJpaAnnGenFactory implements AnnotationGeneratorFactory {

    public PolicyCmptImplClassJpaAnnGenFactory() {
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) throws CoreException {
        switch (type) {
            case POLICY_CMPT_IMPL_CLASS:
                return new PolicyCmptImplClassJpaAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD:
                return new PolicyCmptImplClassAttributeFieldJpaAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_GETTER:
                return new PolicyCmptImplClassAttributeGetterJpaAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_SETTER:
                return new PolicyCmptImplClassAttributeSetterJpaAnnGen();
            case POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD:
                return new PolicyCmptImplClassTransientFieldJpaAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ASSOCIATION:
                return new PolicyCmptImplClassAssociationJpaAnnGen();
            default:
                throw new CoreException(new IpsStatus("Could not find an annotation generator for " + type));
        }
    }

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return ipsProject.isPersistenceSupportEnabled();
    }
}
