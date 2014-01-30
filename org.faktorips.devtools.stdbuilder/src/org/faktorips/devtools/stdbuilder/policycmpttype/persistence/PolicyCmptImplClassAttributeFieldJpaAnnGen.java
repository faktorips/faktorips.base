/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.persistence.IPersistenceProvider;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;

/**
 * This class generates JPA annotations for fields derived from policy component type attributes.
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

            List<String> attributesToAppend = new ArrayList<String>();
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

        }

        return fragment;
    }

    private ValueDatatype getDatatype(IPolicyCmptTypeAttribute attribute) {
        try {
            return attribute.findDatatype(attribute.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
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
     * <code>
     * Converter(name = "gender", converterClass = example.Gender) 
     * Convert("gender")
     * <code>
     */
    private void createConverterAnnotation(JavaCodeFragment fragment, IPersistentAttributeInfo jpaAttributeInfo) {
        if (StringUtils.isEmpty(jpaAttributeInfo.getConverterQualifiedClassName())) {
            return;
        }
        IIpsArtefactBuilderSet builderSet = getBuilderSet(jpaAttributeInfo.getIpsProject());
        if (builderSet instanceof StandardBuilderSet) {
            IPersistenceProvider persistenceProviderImpl = ((StandardBuilderSet)builderSet)
                    .getPersistenceProviderImplementation();
            if (persistenceProviderImpl == null) {
                return;
            }
            if (!persistenceProviderImpl.isSupportingConverters()) {
                return;
            }

            persistenceProviderImpl.addAnnotationConverter(fragment, jpaAttributeInfo);
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

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ATTRIBUTE_FIELD;
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
