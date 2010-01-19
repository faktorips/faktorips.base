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
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.AnnotationGenerator;

public class PolicyCmptImplClassJpaAnnotationGenerator implements AnnotationGenerator {

    public final static String ANNOTATION_ENTITY = "@Entity";
    public final static String ANNOTATION_TABLE = "@Table";
    public final static String ANNOTATION_SECONDARY_TABLE = "@SecondaryTable";

    private static final String IMPORT_ENTITY = "javax.persistence.Entity";
    private static final String IMPORT_TABLE = "javax.persistence.Table";
    private static final String IMPORT_SECONDARY_TABLE = "javax.persistence.SecondaryTable";

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        IPolicyCmptType pcType = (IPolicyCmptType)ipsElement;

        IPersistentTypeInfo persistenceTypeInfo = pcType.getPersistenceTypeInfo();
        InheritanceStrategy inheritanceStrategy = persistenceTypeInfo.getInheritanceStrategy();

        fragment.append(ANNOTATION_ENTITY);
        fragment.addImport(IMPORT_ENTITY);
        if (inheritanceStrategy != InheritanceStrategy.MIXED) {
            fragment.append(ANNOTATION_TABLE + "(name = \"" + persistenceTypeInfo.getTableName() + "\")");
            fragment.addImport(IMPORT_TABLE);

        } else {
            fragment.append(ANNOTATION_SECONDARY_TABLE + "(name = \"" + persistenceTypeInfo.getSecondaryTableName()
                    + "\")");
            fragment.addImport(IMPORT_SECONDARY_TABLE);
        }

        return fragment;
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS;
    }

}
