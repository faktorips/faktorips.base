/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.AnnotationGeneratorFactory;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.PolicyCmptImplClassJaxbAnnGen;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class PolicyCmptImplClassJaxbAnnGenFactory implements AnnotationGeneratorFactory {

    public PolicyCmptImplClassJaxbAnnGenFactory() {
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) throws CoreException {
        switch (type) {
            case POLICY_CMPT_IMPL_CLASS:
                return new PolicyCmptImplClassJaxbAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ASSOCIATION:
                return new PolicyCmptImplClassAssociationJaxbAnnGen();
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD:
                return new PolicyCmptImplClassAttributeFieldJaxbGen();
            default:
                return null;
        }
    }

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        // need this complex way because the builderSet you could get from IpsProject is not
        // initialized when this method is called
        IIpsProjectProperties properties = ipsProject.getReadOnlyProperties();
        IIpsArtefactBuilderSetInfo builderSetInfo = ipsProject.getIpsModel().getIpsArtefactBuilderSetInfo(
                properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfig builderSetConfig = properties.getBuilderSetConfig().create(ipsProject,
                builderSetInfo);
        return builderSetConfig.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT);
    }

}
