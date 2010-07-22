/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;

public class PolicyCmptImplClassAttributeFieldJaxbGen extends AbstractAnnotationGenerator {

    public PolicyCmptImplClassAttributeFieldJaxbGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    @Override
    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)ipsElement;
        GenPolicyCmptTypeAttribute genAttribute;
        try {
            GenPolicyCmptType genPolicyCmptType = (GenPolicyCmptType)getStandardBuilderSet().getGenerator(
                    attribute.getType());
            genAttribute = genPolicyCmptType.getGenerator(attribute);
        } catch (CoreException e) {
            throw new RuntimeException();
        }

        String annotationParam = "name=\"" + attribute.getName() + "\"";
        if (!genAttribute.getDatatype().isPrimitive()) {
            annotationParam += ",nillable=true";
        }
        builder.annotationLn("javax.xml.bind.annotation.XmlElement", annotationParam);
        return builder.getFragment();
    }

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD;
    }

    @Override
    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        return true;
    }

}
