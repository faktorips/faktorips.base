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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IPersistenceProvider;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenPolicyCmptTypeAttribute;

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

        IPolicyCmptTypeAttribute attribute = (IPolicyCmptTypeAttribute)ipsElement;
        ValueDatatype datatype = getDatatype(attribute);
        IPersistentAttributeInfo jpaAttributeInfo = attribute.getPersistenceAttributeInfo();

        String tableColumnName = jpaAttributeInfo.getTableColumnName();
        ITableColumnNamingStrategy tableColumnNamingStrategy = ipsElement.getIpsProject()
                .getTableColumnNamingStrategy();
        tableColumnName = tableColumnNamingStrategy.getTableColumnName(tableColumnName);

        boolean isNullable = jpaAttributeInfo.getTableColumnNullable();
        boolean isUnique = jpaAttributeInfo.getTableColumnUnique();

        fragment.addImport(IMPORT_COLUMN);
        fragment.append(ANNOTATION_COLUMN);

        List<String> attributesToAppend = new ArrayList<String>();
        attributesToAppend.add("name=\"" + tableColumnName + "\"");

        if (StringUtils.isEmpty(jpaAttributeInfo.getSqlColumnDefinition())) {
            attributesToAppend.add("nullable = " + isNullable);
            attributesToAppend.add("unique = " + isUnique);
            addDatatypeDendingJpaAttributes(attributesToAppend, attribute, jpaAttributeInfo, datatype);
        } else {
            // sql column definition overwrites nullable (not null), unique, scale, precision and
            // length
            attributesToAppend.add("columnDefinition=\"" + jpaAttributeInfo.getSqlColumnDefinition() + "\"");
        }

        fragment.append("(");
        for (Iterator<String> iterator = attributesToAppend.iterator(); iterator.hasNext();) {
            String attr = iterator.next();
            fragment.append(attr);
            if (iterator.hasNext()) {
                fragment.append(", ");
            }
        }
        fragment.append(')').appendln();
        createTemporalAnnotationIfTemporalDatatype(fragment, jpaAttributeInfo, datatype);
        createConverterAnnotation(fragment, jpaAttributeInfo);

        return fragment;
    }

    private ValueDatatype getDatatype(IPolicyCmptTypeAttribute attribute) {
        GenPolicyCmptTypeAttribute generator;
        try {
            generator = getStandardBuilderSet().getGenerator(attribute.getPolicyCmptType()).getGenerator(attribute);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return generator.getDatatype();
    }

    private void addDatatypeDendingJpaAttributes(List<String> attributesToAppend,
            IPolicyCmptTypeAttribute attribute,
            IPersistentAttributeInfo persAttrInfo,
            ValueDatatype datatype) {
        if (PersistenceUtil.isSupportingDecimalPlaces(datatype)) {
            attributesToAppend.add("scale = " + persAttrInfo.getTableColumnScale());
            attributesToAppend.add("precision = " + persAttrInfo.getTableColumnPrecision());
        }
        if (PersistenceUtil.isSupportingLenght(datatype)) {
            attributesToAppend.add("length = " + persAttrInfo.getTableColumnSize());
        }
    }

    /**
     * <code>
     * Converter(name = "gender", converterClass = example.Gender) 
     * Convert("gender")
     * <code>
     * 
     * @param datatype
     */
    private void createConverterAnnotation(JavaCodeFragment fragment, IPersistentAttributeInfo jpaAttributeInfo) {
        if (StringUtils.isEmpty(jpaAttributeInfo.getConverterQualifiedClassName())) {
            return;
        }

        IPersistenceProvider persistenceProviderImpl = getStandardBuilderSet().getPersistenceProviderImplementation();
        if (persistenceProviderImpl == null) {
            return;
        }
        if (!persistenceProviderImpl.isSupportingConverter()) {
            return;
        }

        persistenceProviderImpl.addAnnotationConverter(fragment, jpaAttributeInfo);
    }

    private void createTemporalAnnotationIfTemporalDatatype(JavaCodeFragment fragment,
            IPersistentAttributeInfo jpaAttributeInfo,
            ValueDatatype datatype) {

        if (PersistenceUtil.isSupportingTemporalType(datatype)) {
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
