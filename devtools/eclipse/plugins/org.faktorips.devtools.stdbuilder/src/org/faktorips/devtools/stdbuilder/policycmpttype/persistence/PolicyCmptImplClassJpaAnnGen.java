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

import java.util.Objects;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.IPersistenceProvider.PersistenceAnnotation;
import org.faktorips.devtools.model.builder.IPersistenceProvider.PersistenceEnum;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo.PersistentType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A generator for JPA annotations of <code>IPolicyCmptType</code>s.
 * <p>
 * Each persistent policy component type needs at least an <code>@Entity</code> annotation. The
 * information which annotations to generate is pulled from the class {@link IPersistentTypeInfo}
 * which is part of persistent {@link IPolicyCmptType}s.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS
 */
public class PolicyCmptImplClassJpaAnnGen extends AbstractJpaAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder fragmentBuilder = new JavaCodeFragmentBuilder();
        IPersistenceProvider persistenceProvider = getPersistenceProvider(generatorModelNode.getIpsProject());
        if (generatorModelNode instanceof XPolicyCmptClass xPolicyCmptClass) {
            IPolicyCmptType pcType = xPolicyCmptClass.getType();

            IPersistentTypeInfo persistenceTypeInfo = pcType.getPersistenceTypeInfo();

            if (persistenceTypeInfo.getPersistentType() == PersistentType.ENTITY) {
                fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.Entity));

                addAnnotationsForInheritanceStrategy(persistenceProvider, fragmentBuilder, persistenceTypeInfo);
                addAnnotationsForDiscriminator(persistenceProvider, fragmentBuilder, persistenceTypeInfo);
            } else if (persistenceTypeInfo.getPersistentType() == PersistentType.MAPPED_SUPERCLASS) {
                fragmentBuilder
                        .annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.MappedSuperclass));
            } else {
                throw new RuntimeException("Unknown persistent type: " + persistenceTypeInfo.getPersistentType());
            }

        }
        return fragmentBuilder.getFragment();
    }

    private void addAnnotationsForInheritanceStrategy(IPersistenceProvider persistenceProvider,
            JavaCodeFragmentBuilder fragmentBuilder,
            IPersistentTypeInfo persistenceTypeInfo) {
        InheritanceStrategy inhStrategy = persistenceTypeInfo.getInheritanceStrategy();
        String tableName = persistenceTypeInfo.getTableName();

        if (IpsStringUtils.isEmpty(tableName) && persistenceTypeInfo.isUseTableDefinedInSupertype()) {
            // note that we must always add the table name annotation, otherwise a default table
            // may be generated!
            tableName = getTableNameFromSupertype(persistenceTypeInfo);
        }

        if (IpsStringUtils.isNotEmpty(tableName) && !persistenceTypeInfo.isUseTableDefinedInSupertype()) {
            fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.Table), "name",
                    tableName);
        }

        // the inheritance strategy must only be add to the root entity class
        // (base entity), we suppose that the discriminator column must always
        // defined in the base entity, thus we can use this assumption to check
        // if the current type is the root
        if (!persistenceTypeInfo.isDefinesDiscriminatorColumn()) {
            return;
        }

        if (inhStrategy == InheritanceStrategy.JOINED_SUBCLASS) {
            JavaCodeFragment param = new JavaCodeFragment();
            param.append("strategy = ");
            param.appendClassName(persistenceProvider.getQualifiedName(PersistenceEnum.InheritanceType));
            param.append(".JOINED");
            fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.Inheritance),
                    param);
        } else if (inhStrategy == InheritanceStrategy.SINGLE_TABLE) {
            // note that the single table inheritance strategy is the default
            // strategy, nevertheless we add this annotation
            JavaCodeFragment param = new JavaCodeFragment();
            param.append("strategy = ");
            param.appendClassName(persistenceProvider.getQualifiedName(PersistenceEnum.InheritanceType));
            param.append(".SINGLE_TABLE");
            fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.Inheritance),
                    param);
        }
    }

    private String getTableNameFromSupertype(IPersistentTypeInfo persistenceTypeInfo) {
        SearchTableNameInSuperTypes searchTableNameInSuperTypes = new SearchTableNameInSuperTypes(
                persistenceTypeInfo.getIpsProject());
        searchTableNameInSuperTypes.start(persistenceTypeInfo.getPolicyCmptType());
        return searchTableNameInSuperTypes.tableName;
    }

    private void addAnnotationsForDiscriminator(IPersistenceProvider persistenceProvider,
            JavaCodeFragmentBuilder fragmentBuilder,
            IPersistentTypeInfo persistenceTypeInfo) {
        String discriminatorValue = persistenceTypeInfo.getDiscriminatorValue();
        if (!IpsStringUtils.isEmpty(discriminatorValue)) {
            JavaCodeFragment param = new JavaCodeFragment();
            param.appendQuoted(discriminatorValue);
            fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.DiscriminatorValue),
                    param);
        }

        if (!persistenceTypeInfo.isDefinesDiscriminatorColumn()) {
            return;
        }

        DiscriminatorDatatype discriminatorDatatype = persistenceTypeInfo.getDiscriminatorDatatype();
        String discriminatorColumnName = persistenceTypeInfo.getDiscriminatorColumnName();
        Integer discriminatorColumnLength = persistenceTypeInfo.getDiscriminatorColumnLength();

        JavaCodeFragment params = new JavaCodeFragment("name = ");
        params.appendQuoted(discriminatorColumnName);
        params.append(", discriminatorType = ");
        params.appendClassName(persistenceProvider.getQualifiedName(PersistenceEnum.DiscriminatorType));
        params.append('.');
        params.append(Objects.toString(discriminatorDatatype));
        if (discriminatorColumnLength != null) {
            params.append(", length = ");
            params.append(discriminatorColumnLength);
        }
        fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.DiscriminatorColumn),
                params);
    }

    @Override
    public boolean isGenerateAnnotationForInternal(IIpsElement ipsElement) {
        if (!(ipsElement instanceof IPolicyCmptType)) {
            return false;
        }
        return ((IPolicyCmptType)ipsElement).isPersistentEnabled();
    }

    private static class SearchTableNameInSuperTypes extends TypeHierarchyVisitor<IPolicyCmptType> {

        private String tableName = null;

        public SearchTableNameInSuperTypes(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            String tableNameTemp = currentType.getPersistenceTypeInfo().getTableName();
            if (IpsStringUtils.isNotEmpty(tableNameTemp)) {
                tableName = tableNameTemp;
                return false;
            }
            return true;
        }

    }

}
