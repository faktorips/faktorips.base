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
    private static final String ANNOTATION_TEMPORAL = "@Temporal";

    private static final String IMPORT_COLUMN = "javax.persistence.Column";
    private static final String IMPORT_TEMPORAL = "javax.persistence.Temporal";
    private static final String IMPORT_TEMPORAL_TYPE = "javax.persistence.TemporalType";

    private static final String ATTRIBUTE_TEMPORAL_TYPE = "TemporalType";

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
        createSecondaryTableAnnotationIfMixedInheritance(fragment, jpaAttributeInfo);
        fragment.append(')').appendln();
        createTemporalAnnotationIfTemporalDatatype(fragment, jpaAttributeInfo);

        return fragment;
    }

    private void createSecondaryTableAnnotationIfMixedInheritance(JavaCodeFragment fragment,
            IPersistentAttributeInfo jpaAttributeInfo) {
        IPolicyCmptType pcType = jpaAttributeInfo.getPolicyComponentTypeAttribute().getPolicyCmptType();
        IPersistentTypeInfo persistenceTypeInfo = pcType.getPersistenceTypeInfo();
        InheritanceStrategy inhStrategy = persistenceTypeInfo.getInheritanceStrategy();

        // if (inhStrategy == InheritanceStrategy.MIXED) {
        // String secondaryTableName = persistenceTypeInfo.getSecondaryTableName();
        // ITableColumnNamingStrategy tableColumnNamingStrategy = pcType.getIpsProject()
        // .getTableColumnNamingStrategy();
        // secondaryTableName = tableColumnNamingStrategy.getTableColumnName(secondaryTableName);
        //
        // fragment.append(", table = \"").append(secondaryTableName).append('"');
        // }
    }

    private void createTemporalAnnotationIfTemporalDatatype(JavaCodeFragment fragment,
            IPersistentAttributeInfo jpaAttributeInfo) {

        if (jpaAttributeInfo.isTemporalAttribute()) {
            fragment.addImport(IMPORT_TEMPORAL);
            fragment.addImport(IMPORT_TEMPORAL_TYPE);
            fragment.append(ANNOTATION_TEMPORAL);

            fragment.append('(').append(ATTRIBUTE_TEMPORAL_TYPE).append('.');
            fragment.append(jpaAttributeInfo.getTemporalMapping().toJpaTemporalType());
            fragment.append(')');
        }
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD;
    }

    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        if (!(ipsElement instanceof IPolicyCmptTypeAttribute)) {
            return false;
        }
        if (!((IPolicyCmptTypeAttribute)ipsElement).getPolicyCmptType().isPersistentEnabled()) {
            return false;
        }
        // the attribute must not be marked as transient
        return !((IPolicyCmptTypeAttribute)ipsElement).getPersistenceAttributeInfo().isTransient();
    }
}
