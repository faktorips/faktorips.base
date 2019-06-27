/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;

/**
 * This class generates the @Transient JPA annotation for fields of policy component types.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassTransientFieldJpaAnnGen extends AbstractJpaAnnotationGenerator {

    private static final String IMPORT_TRANSIENT = "javax.persistence.Transient";
    private static final String ANNOTATION_TRANSIENT = "@Transient";

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.appendln(ANNOTATION_TRANSIENT);
        fragment.addImport(IMPORT_TRANSIENT);

        return fragment;
    }

    @Override
    public boolean isGenerateAnnotationForInternal(IIpsElement ipsElement) {
        if (ipsElement instanceof IPolicyCmptTypeAttribute) {
            IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)ipsElement;
            if (!attribute.getPolicyCmptType().isPersistentEnabled()) {
                return false;
            }
            return attribute.getPersistenceAttributeInfo().isTransient();
        } else if (ipsElement instanceof IPolicyCmptTypeAssociation) {
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)ipsElement;
            if (!association.getPolicyCmptType().isPersistentEnabled()) {
                return false;
            }
            if (!PolicyCmptImplClassAssociationJpaAnnGen.isTargetPolicyCmptTypePersistenceEnabled(association)) {
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
