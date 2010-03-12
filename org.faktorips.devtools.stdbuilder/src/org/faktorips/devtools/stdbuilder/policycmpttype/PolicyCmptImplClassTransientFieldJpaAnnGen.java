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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * This class generates the @Transient JPA annotation for fields of policy component types.
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassTransientFieldJpaAnnGen extends AbstractAnnotationGenerator {

    private static final String IMPORT_TRANSIENT = "javax.persistence.Transient";
    private static final String ANNOTATION_TRANSIENT = "@Transient";

    public PolicyCmptImplClassTransientFieldJpaAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendln(ANNOTATION_TRANSIENT);
        fragment.addImport(IMPORT_TRANSIENT);

        return fragment;
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD;
    }

    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        if (ipsElement instanceof IPolicyCmptTypeAssociation) {
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)ipsElement;
            if (!association.getPolicyCmptType().isPersistentEnabled()) {
                return false;
            }
            if (!PolicyCmptImplClassAssociationJpaAnnGen.isTargetPolicyCmptTypePersistenceEnabled(this, association)) {
                return true;
            }

            // check only if the source is not marked as transient
            // don't care about the target, because there is a validation
            // that checks that both sides are marked as transient or not transient
            return association.getPersistenceAssociatonInfo().isTransient();
        } else if (ipsElement instanceof IPolicyCmptType) {
            return ((IPolicyCmptType)ipsElement).isPersistentEnabled();
        }
        return false;
    }
}
