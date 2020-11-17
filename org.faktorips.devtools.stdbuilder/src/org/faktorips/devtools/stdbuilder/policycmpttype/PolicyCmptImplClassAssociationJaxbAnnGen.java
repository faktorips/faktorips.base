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
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;

/**
 * Generates JAXB annotations for policy component type associations
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD
 */
public class PolicyCmptImplClassAssociationJaxbAnnGen extends AbstractAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        if (modelNode instanceof XPolicyAssociation) {
            XPolicyAssociation xPolicyAssociation = (XPolicyAssociation)modelNode;
            IPolicyCmptTypeAssociation association = xPolicyAssociation.getAssociation();

            String fieldName = xPolicyAssociation.getFieldName();
            String targetImplClassName = xPolicyAssociation.getTargetQualifiedClassName();

            if (association.is1To1()) {
                // toOne
                if (association.isCompositionDetailToMaster()) {
                    builder.annotationLn("javax.xml.bind.annotation.XmlAttribute", "name", fieldName + ".id");
                } else {
                    builder.annotationLn("javax.xml.bind.annotation.XmlElement", "name=\"" + association.getName()
                            + "\", type=" + targetImplClassName + ".class");
                }
                if (!association.isCompositionMasterToDetail()) {
                    builder.annotationLn("javax.xml.bind.annotation.XmlIDREF");
                }
            } else {
                // toMany
                if (!association.isCompositionDetailToMaster()) {
                    builder.annotationLn("javax.xml.bind.annotation.XmlElement", "name=\"" + association.getName()
                            + "\", type=" + targetImplClassName + ".class");
                    if (!association.isCompositionMasterToDetail()) {
                        // normally this must be an association
                        builder.annotationLn("javax.xml.bind.annotation.XmlIDREF");
                    }
                }
                builder.annotationLn("javax.xml.bind.annotation.XmlElementWrapper", "name",
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
