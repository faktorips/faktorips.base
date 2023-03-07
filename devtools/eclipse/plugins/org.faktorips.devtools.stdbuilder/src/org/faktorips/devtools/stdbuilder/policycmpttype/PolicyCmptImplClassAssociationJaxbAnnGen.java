/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.JaxbAnnGenFactory.JaxbAnnotation;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;

/**
 * Generates JAXB annotations for policy component type associations
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD
 */
public class PolicyCmptImplClassAssociationJaxbAnnGen extends AbstractJaxbAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        if (generatorModelNode instanceof XPolicyAssociation xPolicyAssociation) {
            IPolicyCmptTypeAssociation association = xPolicyAssociation.getAssociation();

            String fieldName = xPolicyAssociation.getFieldName();
            String targetImplClassName = xPolicyAssociation.getTargetQualifiedClassName();

            if (association.is1To1()) {
                // toOne
                if (association.isCompositionDetailToMaster()) {
                    builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlAttribute, generatorModelNode), "name",
                            fieldName + ".id");
                } else {
                    builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlElement, generatorModelNode),
                            "name=\"" + association.getName()
                                    + "\", type=" + targetImplClassName + ".class");
                }
                if (!association.isCompositionMasterToDetail()) {
                    builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlIDREF, generatorModelNode));
                }
            } else {
                // toMany
                if (!association.isCompositionDetailToMaster()) {
                    builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlElement, generatorModelNode),
                            "name=\"" + association.getName() + "\", type=" + targetImplClassName + ".class");
                    if (!association.isCompositionMasterToDetail()) {
                        // normally this must be an association
                        builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlIDREF, generatorModelNode));
                    }
                }
                builder.annotationLn(getQualifiedName(JaxbAnnotation.XmlElementWrapper, generatorModelNode), "name",
                        association.getTargetRolePlural());
            }
        }
        return builder.getFragment();
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return true;
    }
}
