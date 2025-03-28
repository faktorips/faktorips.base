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

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.IPersistenceProvider.PersistenceAnnotation;
import org.faktorips.devtools.model.builder.java.annotations.AnnotatedJavaElementType;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;

/**
 * This class generates the @Transient JPA annotation for fields of policy component types.
 *
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD
 */
public class PolicyCmptImplClassTransientFieldJpaAnnGen extends AbstractJpaAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        IPersistenceProvider persistenceProvider = getPersistenceProvider(generatorModelNode.getIpsProject());
        JavaCodeFragmentBuilder fragmentBuilder = new JavaCodeFragmentBuilder();
        fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.Transient));

        return fragmentBuilder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationForInternal(IIpsElement ipsElement) {
        if (ipsElement instanceof IPolicyCmptTypeAttribute attribute) {
            if (!attribute.getPolicyCmptType().isPersistentEnabled()) {
                return false;
            }
            return attribute.getPersistenceAttributeInfo().isTransient();
        } else if (ipsElement instanceof IPolicyCmptTypeAssociation association) {
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
        } else if (ipsElement instanceof IPolicyCmptType policyCmptType) {
            return policyCmptType.isPersistentEnabled();
        }
        return false;
    }
}
