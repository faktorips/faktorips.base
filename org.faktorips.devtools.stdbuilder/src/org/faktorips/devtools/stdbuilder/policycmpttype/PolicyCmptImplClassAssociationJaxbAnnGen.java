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
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;

public class PolicyCmptImplClassAssociationJaxbAnnGen extends AbstractAnnotationGenerator {

    public PolicyCmptImplClassAssociationJaxbAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)ipsElement;

        GenAssociation genAssociation = null;
        String fieldName = null;
        String targetImplClassName = null;
        try {
            GenPolicyCmptType genPolicyCmptType = (GenPolicyCmptType)getStandardBuilderSet().getGenerator(
                    association.getType());
            genAssociation = genPolicyCmptType.getGenerator(association);
            fieldName = genAssociation.getFieldNameForAssociation();
            targetImplClassName = genAssociation.getTargetImplClassName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
            builder.annotationLn("javax.xml.bind.annotation.XmlElementWrapper", "name", association
                    .getTargetRolePlural());
        }
        return builder.getFragment();
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ASSOCIATION;
    }

    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        return true;
    }
}
