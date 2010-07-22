/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.AnnotationGeneratorFactory;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class PolicyCmptImplClassJaxbAnnGenFactory implements AnnotationGeneratorFactory {
    private final StandardBuilderSet standardBuilderSet;

    public PolicyCmptImplClassJaxbAnnGenFactory(StandardBuilderSet standardBuilderSet) {
        this.standardBuilderSet = standardBuilderSet;
    }

    @Override
    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) throws CoreException {
        switch (type) {
            case POLICY_CMPT_IMPL_CLASS_ASSOCIATION:
                return new PolicyCmptImplClassAssociationJaxbAnnGen(standardBuilderSet);
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD:
                return new PolicyCmptImplClassAttributeFieldJaxbGen(standardBuilderSet);
            default:
                return null;
        }
    }

    @Override
    public boolean isRequiredFor(IIpsProject ipsProject) {
        return standardBuilderSet.isGenerateJaxbSupport();
    }

}
