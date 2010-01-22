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
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

public class PolicyCmptImplClassAttributeFieldJpaAnnGen extends AbstractAnnotationGenerator {

    private static final String ANNOTATION_COLUMN = "@Column";
    private static final String IMPORT_COLUMN = "javax.persistence.Column";

    public PolicyCmptImplClassAttributeFieldJpaAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();

        IPersistentAttributeInfo jpaAttributeInfo = ((IPolicyCmptTypeAttribute)ipsElement)
                .getPersistenceAttributeInfo();

        fragment.addImport(IMPORT_COLUMN);
        fragment.appendln(ANNOTATION_COLUMN + "(name = \"" + jpaAttributeInfo.getTableColumnName() + "\", nullable = "
                + jpaAttributeInfo.getTableColumnNullable() + ")");

        return fragment;
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD;
    }

}
