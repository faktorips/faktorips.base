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
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.IPersistenceProvider.PersistenceAnnotation;
import org.faktorips.devtools.model.builder.IPersistenceProvider.PersistenceEnum;
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
 */
public class PolicyCmptImplClassAttributeFieldJpaAnnGen extends AbstractJpaAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder fragmentBuilder = new JavaCodeFragmentBuilder();
        if (generatorModelNode instanceof XPolicyAttribute) {
            XPolicyAttribute xPolicyAttribute = (XPolicyAttribute)generatorModelNode;

            IPolicyCmptTypeAttribute attribute = xPolicyAttribute.getAttribute();
            ValueDatatype datatype = getDatatype(attribute);
            IPersistentAttributeInfo jpaAttributeInfo = attribute.getPersistenceAttributeInfo();

            String tableColumnName = jpaAttributeInfo.getTableColumnName();

            boolean isNullable = jpaAttributeInfo.getTableColumnNullable();
            boolean isUnique = jpaAttributeInfo.getTableColumnUnique();

            List<String> attributesToAppend = new ArrayList<>();
            attributesToAppend.add("name=\"" + tableColumnName + "\"");

            if (StringUtils.isEmpty(jpaAttributeInfo.getSqlColumnDefinition())) {
                attributesToAppend.add("nullable = " + isNullable);
                attributesToAppend.add("unique = " + isUnique);
                addDatatypeDendingJpaAttributes(attributesToAppend, jpaAttributeInfo, datatype);
            } else {
                // sql column definition overwrites nullable (not null), unique, scale, precision
                // and length
                attributesToAppend.add("columnDefinition=\"" + jpaAttributeInfo.getSqlColumnDefinition() + "\"");
            }

            IPersistenceProvider persistenceProvider = getPersistenceProvider(generatorModelNode.getIpsProject());
            JavaCodeFragment params = new JavaCodeFragment();
            params.appendJoined(attributesToAppend);
            fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.Column), params);

            createTemporalAnnotationIfTemporalDatatype(fragmentBuilder, jpaAttributeInfo, datatype,
                    persistenceProvider);
            createConverterAnnotation(fragmentBuilder.getFragment(), jpaAttributeInfo);
            createIndexAnnotation(fragmentBuilder.getFragment(), jpaAttributeInfo);
        }

        return fragmentBuilder.getFragment();
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
        IPersistenceProvider persistenceProviderImpl = getPersistenceProvider(jpaAttributeInfo.getIpsProject());
        if (persistenceProviderImpl != null && persistenceProviderImpl.isSupportingConverters()) {
            fragment.append(persistenceProviderImpl.getConverterAnnotations(jpaAttributeInfo));
        }
    }

    private void createTemporalAnnotationIfTemporalDatatype(JavaCodeFragmentBuilder fragmentBuilder,
            IPersistentAttributeInfo jpaAttributeInfo,
            ValueDatatype datatype,
            IPersistenceProvider persistenceProvider) {

        if (PersistenceUtil.isSupportingTemporalType(datatype)) {
            JavaCodeFragment params = new JavaCodeFragment();
            params.appendClassName(persistenceProvider.getQualifiedName(PersistenceEnum.TemporalType));
            params.append('.');
            params.append(jpaAttributeInfo.getTemporalMapping().toJpaTemporalType());

            fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.Temporal), params);
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
