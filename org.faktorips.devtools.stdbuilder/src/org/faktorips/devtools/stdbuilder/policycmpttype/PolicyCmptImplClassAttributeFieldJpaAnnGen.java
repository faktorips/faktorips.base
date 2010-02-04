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
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * This class generates JPA annotations for fields derived from policy component type attributes.
 * 
 * @author Roman Grutza
 */
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

        String tableColumnName = jpaAttributeInfo.getTableColumnName();
        ITableColumnNamingStrategy tableColumnNamingStrategy = ipsElement.getIpsProject()
                .getTableColumnNamingStrategy();
        tableColumnName = tableColumnNamingStrategy.getTableColumnName(tableColumnName);

        boolean isNullable = jpaAttributeInfo.getTableColumnNullable();

        fragment.addImport(IMPORT_COLUMN);
        fragment.append(ANNOTATION_COLUMN);
        fragment.append("(name = \"").append(tableColumnName).append('"');
        fragment.append(", nullable = ").append(isNullable);
        createSecondaryTableAttributeIfMixedInheritance(fragment, jpaAttributeInfo);
        fragment.append(')').appendln();

        return fragment;
    }

    private void createSecondaryTableAttributeIfMixedInheritance(JavaCodeFragment fragment,
            IPersistentAttributeInfo jpaAttributeInfo) {
        IPolicyCmptType pcType = jpaAttributeInfo.getPolicyComponentTypeAttribute().getPolicyCmptType();
        IPersistentTypeInfo persistenceTypeInfo = pcType.getPersistenceTypeInfo();
        InheritanceStrategy inhStrategy = persistenceTypeInfo.getInheritanceStrategy();

        if (inhStrategy == InheritanceStrategy.MIXED) {
            String secondaryTableName = persistenceTypeInfo.getSecondaryTableName();
            ITableColumnNamingStrategy tableColumnNamingStrategy = pcType.getIpsProject()
                    .getTableColumnNamingStrategy();
            secondaryTableName = tableColumnNamingStrategy.getTableColumnName(secondaryTableName);

            fragment.append(", table = \"").append(secondaryTableName).append('"');
        }
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD;
    }

}
