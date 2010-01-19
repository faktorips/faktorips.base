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
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.AnnotationGenerator;

public class PolicyCmptImplClassAttributeSetterJpaAnnotationGenerator implements AnnotationGenerator {

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();

        // TODO: flesh
        return fragment;
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_SETTER;
    }

}
