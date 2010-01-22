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
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

// TODO: javadoc
public class PolicyCmptImplClassAssociationJpaAnnGen extends AbstractAnnotationGenerator {

    private static final String IMPORT_ONE_TO_ONE = "javax.persistence.OneToOne";
    private static final String ANNOTATION_ONE_TO_ONE = "@OneToOne";

    public PolicyCmptImplClassAssociationJpaAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        IPolicyCmptTypeAssociation pcTypeAssociation = (IPolicyCmptTypeAssociation)ipsElement;
        IPersistentAssociationInfo associatonInfo = pcTypeAssociation.getPersistenceAssociatonInfo();

        try {
            if (pcTypeAssociation.isAssoziation()) {
                createAnnotationForAssociation(fragment, pcTypeAssociation, associatonInfo);
            }
            if (pcTypeAssociation.isComposition()) {
                createAnnotationForComposition(fragment, pcTypeAssociation, associatonInfo);
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        return fragment;
    }

    private void createAnnotationForAssociation(JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation pcTypeAssociation,
            IPersistentAssociationInfo associatonInfo) {

    }

    private void createAnnotationForComposition(JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation pcTypeAssociation,
            IPersistentAssociationInfo associatonInfo) throws CoreException {

        if (pcTypeAssociation.is1To1()) {
            // TODO: root class in hierarchy of target?
            IIpsProject ipsProject = pcTypeAssociation.getIpsProject();
            IPolicyCmptType targetPcType = pcTypeAssociation.findTargetPolicyCmptType(ipsProject);

            String targetQName = getQualifiedImplClassName(targetPcType);

            fragment.addImport(IMPORT_ONE_TO_ONE);
            fragment.addImport(targetQName);

            fragment.append(ANNOTATION_ONE_TO_ONE);
            fragment.append('(').append("targetEntity = ");

            fragment.appendClassName(targetQName).append(".class").appendln(')');
        }
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ASSOCIATION;
    }

}
