/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAttributeInfo;
import org.faktorips.devtools.model.util.PersistenceUtil;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAttribute;

/**
 * This class generates JPA annotations for fields derived from policy component type attributes.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassAttributeFieldJpaAnnGen extends AbstractJpaAnnotationGenerator {

    private static final String ANNOTATION_COLUMN = "@Column";
    private static final String ANNOTATION_TEMPORAL = "@Temporal";

    private static final String IMPORT_COLUMN = "javax.persistence.Column";
    private static final String IMPORT_TEMPORAL = "javax.persistence.Temporal";
    private static final String IMPORT_TEMPORAL_TYPE = "javax.persistence.TemporalType";

    private static final String ATTRIBUTE_TEMPORAL_TYPE = "TemporalType";

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (generatorModelNode instanceof XPolicyAttribute) {
            XPolicyAttribute xPolicyAttribute = (XPolicyAttribute)generatorModelNode;

            IPolicyCmptTypeAttribute attribute = xPolicyAttribute.getAttribute();
            ValueDatatype datatype = getDatatype(attribute);
            IPersistentAttributeInfo jpaAttributeInfo = attribute.getPersistenceAttributeInfo();

            String tableColumnName = jpaAttributeInfo.getTableColumnName();

            boolean isNullable = jpaAttributeInfo.getTableColumnNullable();
            boolean isUnique = jpaAttributeInfo.getTableColumnUnique();

            fragment.addImport(IMPORT_COLUMN);
            fragment.append(ANNOTATION_COLUMN);

            List<String> attributesToAppend = new ArrayList<>();
            attributesToAppend.add("name=\"" + tableColumnName + "\"");

            if (StringUtils.isEmpty(jpaAttributeInfo.getSqlColumnDefinition())) {
                attributesToAppend.add("nullable = " + isNullable);
                attributesToAppend.add("unique = " + isUnique);
                addDatatypeDendingJpaAttributes(attributesToAppend, jpaAttributeInfo, datatype);
            } else {
                // sql column definition overwrites nullable (not null), unique, scale, precision
                // and
                // length
                attributesToAppend.add("columnDefinition=\"" + jpaAttributeInfo.getSqlColumnDefinition() + "\"");
            }

            fragment.append("(");
            fragment.appendJoined(attributesToAppend);
            fragment.append(')').appendln();
            createTemporalAnnotationIfTemporalDatatype(fragment, jpaAttributeInfo, datatype);
            createConverterAnnotation(fragment, jpaAttributeInfo);
            createIndexAnnotation(fragment, jpaAttributeInfo);
        }

        return fragment;
    }

    private ValueDatatype getDatatype(IPolicyCmptTypeAttribute attribute) {
        return attribute.findDatatype(attribute.getIpsProject());
    }

    private void addDatatypeDendingJpaAttributes(List<String> attributesToAppend,
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
     * <code> Converter(name = "gender", converterClass = example.Gender) Convert("gender") </code>
     */
    private void createConverterAnnotation(JavaCodeFragment fragment, IPersistentAttributeInfo jpaAttributeInfo) {
        if (StringUtils.isEmpty(jpaAttributeInfo.getConverterQualifiedClassName())) {
            return;
        }
        IIpsArtefactBuilderSet builderSet = getBuilderSet(jpaAttributeInfo.getIpsProject());
        IPersistenceProvider persistenceProviderImpl = builderSet.getPersistenceProvider();
        if (persistenceProviderImpl != null && persistenceProviderImpl.isSupportingConverters()) {
            fragment.append(persistenceProviderImpl.getConverterAnnotations(jpaAttributeInfo));
        }
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

    private void createIndexAnnotation(JavaCodeFragment fragment, IPersistentAttributeInfo jpaAttributeInfo) {
        IPersistenceProvider persistenceProvider = getPersistenceProvider(jpaAttributeInfo.getIpsProject());
        if (persistenceProvider.isSupportingIndex()) {
            JavaCodeFragment indexAnnotations = persistenceProvider.getIndexAnnotations(jpaAttributeInfo);
            fragment.append(indexAnnotations);
        }
    }

    @Override
    public boolean isGenerateAnnotationForInternal(IIpsElement ipsElement) {
        if (!(ipsElement instanceof IPolicyCmptTypeAttribute)) {
            return false;
        }
        if (!((IPolicyCmptTypeAttribute)ipsElement).getPolicyCmptType().isPersistentEnabled()) {
            return false;
        }
        return !((IPolicyCmptTypeAttribute)ipsElement).getPersistenceAttributeInfo().isTransient();
    }

}
