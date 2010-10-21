/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptImplClassBuilder;

public class PolicyCmptImplClassJaxbAnnGen extends AbstractAnnotationGenerator {

    public PolicyCmptImplClassJaxbAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    @Override
    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        PolicyCmptImplClassBuilder builder = getStandardBuilderSet().getPolicyCmptImplClassBuilder();
        JavaCodeFragmentBuilder codeBuilder = new JavaCodeFragmentBuilder();
        try {
            codeBuilder.annotationLn("javax.xml.bind.annotation.XmlRootElement", "name", builder
                    .getUnqualifiedClassName());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return codeBuilder.getFragment();
    }

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS;
    }

    @Override
    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        return true;
    }

}
