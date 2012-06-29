/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAssociation;

public class PolicyCmptImplClassAssociationJaxbAnnGen extends AbstractAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        if (modelNode instanceof XPolicyAssociation) {
            XPolicyAssociation xPolicyAssociation = (XPolicyAssociation)modelNode;
            IPolicyCmptTypeAssociation association = xPolicyAssociation.getAssociation();

            String fieldName = xPolicyAssociation.getFieldName();
            String targetImplClassName = xPolicyAssociation.getTargetClassName();

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
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ASSOCIATION;
    }

    @Override
    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        return true;
    }
}
