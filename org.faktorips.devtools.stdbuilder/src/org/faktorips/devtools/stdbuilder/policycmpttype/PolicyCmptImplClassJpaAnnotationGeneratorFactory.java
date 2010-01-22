/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.AnnotationGeneratorFactory;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class PolicyCmptImplClassJpaAnnotationGeneratorFactory implements AnnotationGeneratorFactory {

    private final StandardBuilderSet standardBuilderSet;

    public PolicyCmptImplClassJpaAnnotationGeneratorFactory(StandardBuilderSet standardBuilderSet) {
        this.standardBuilderSet = standardBuilderSet;
    }

    public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) throws CoreException {
        switch (type) {
            case POLICY_CMPT_IMPL_CLASS:
                return new PolicyCmptImplClassJpaAnnGen(standardBuilderSet);
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD:
                return new PolicyCmptImplClassAttributeFieldJpaAnnGen(standardBuilderSet);
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_GETTER:
                return new PolicyCmptImplClassAttributeGetterJpaAnnGen(standardBuilderSet);
            case POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_SETTER:
                return new PolicyCmptImplClassAttributeSetterJpaAnnGen(standardBuilderSet);
            case POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD:
                return new PolicyCmptImplClassTransientFieldJpaAnnGen(standardBuilderSet);
            case POLICY_CMPT_IMPL_CLASS_ASSOCIATION:
                return new PolicyCmptImplClassAssociationJpaAnnGen(standardBuilderSet);
            default:
                throw new CoreException(new IpsStatus("Could not find an annotation generator for " + type));
        }
    }

    public boolean isRequiredFor(IIpsProject ipsProject) throws CoreException {
        return ipsProject.isPersistenceSupportEnabled();
    }
}
