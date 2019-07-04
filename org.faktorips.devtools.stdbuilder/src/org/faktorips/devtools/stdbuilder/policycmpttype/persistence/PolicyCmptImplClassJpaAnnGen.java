/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import org.apache.commons.lang.StringUtils;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.internal.model.pctype.PersistentTypeInfo;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;

/**
 * A generator for JPA annotations of <code>IPolicyCmptType</code>s.
 * <p/>
 * Each persistent policy component type needs at least an <code>@Entity</code> annotation. The
 * information which annotations to generate is pulled from the class {@link PersistentTypeInfo}
 * which is part of persistent {@link IPolicyCmptType}s.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassJpaAnnGen extends AbstractJpaAnnotationGenerator {

    private static final String ANNOTATION_ENTITY = "@Entity";
    private static final String ANNOTATION_MAPPED_SUPERCLASS = "@MappedSuperclass";
    private static final String ANNOTATION_TABLE = "@Table";
    private static final String ANNOTATION_DISCRIMINATOR_COLUMN = "@DiscriminatorColumn";
    private static final String ANNOTATION_DISCRIMINATOR_VALUE = "@DiscriminatorValue";
    private static final String ANNOTATION_INHERITANCE = "@Inheritance";

    private static final String IMPORT_ENTITY = "javax.persistence.Entity";
    private static final String IMPORT_MAPPED_SUPERCLASS = "javax.persistence.MappedSuperclass";
    private static final String IMPORT_TABLE = "javax.persistence.Table";
    private static final String IMPORT_DISCRIMINATOR_COLUMN = "javax.persistence.DiscriminatorColumn";
    private static final String IMPORT_DISCRIMINATOR_TYPE = "javax.persistence.DiscriminatorType";
    private static final String IMPORT_DISCRIMINATOR_VALUE = "javax.persistence.DiscriminatorValue";
    private static final String IMPORT_INHERITANCE = "javax.persistence.Inheritance";
    private static final String IMPORT_INHERITANCE_TYPE = "javax.persistence.InheritanceType";

    private static final String ATTRIBUTE_INHERITANCE_TYPE = "InheritanceType";

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (generatorModelNode instanceof XPolicyCmptClass) {
            XPolicyCmptClass xPolicyCmptClass = (XPolicyCmptClass)generatorModelNode;

            IPolicyCmptType pcType = xPolicyCmptClass.getType();

            IPersistentTypeInfo persistenceTypeInfo = pcType.getPersistenceTypeInfo();

            if (persistenceTypeInfo.getPersistentType() == PersistentType.ENTITY) {
                fragment.addImport(IMPORT_ENTITY);
                fragment.appendln(ANNOTATION_ENTITY);
                addAnnotationsForInheritanceStrategy(fragment, persistenceTypeInfo);
                addAnnotationsForDescriminator(fragment, persistenceTypeInfo);
            } else if (persistenceTypeInfo.getPersistentType() == PersistentType.MAPPED_SUPERCLASS) {
                fragment.addImport(IMPORT_MAPPED_SUPERCLASS);
                fragment.appendln(ANNOTATION_MAPPED_SUPERCLASS);
            } else {
                throw new RuntimeException("Unknown persistent type: " + persistenceTypeInfo.getPersistentType());
            }

        }
        return fragment;
    }

    private void addAnnotationsForInheritanceStrategy(JavaCodeFragment fragment, IPersistentTypeInfo persistenceTypeInfo) {
        InheritanceStrategy inhStrategy = persistenceTypeInfo.getInheritanceStrategy();
        String tableName = persistenceTypeInfo.getTableName();

        if (StringUtils.isEmpty(tableName) && persistenceTypeInfo.isUseTableDefinedInSupertype()) {
            // note that we must always add the table name annotation, otherwise a default table
            // may be generated!
            tableName = getTableNameFromSupertype(persistenceTypeInfo);
        }

        if (StringUtils.isNotEmpty(tableName) && !persistenceTypeInfo.isUseTableDefinedInSupertype()) {
            fragment.addImport(IMPORT_TABLE);
            fragment.appendln(ANNOTATION_TABLE + "(name = \"" + tableName + "\")");
        }

        // the inheritance strategy must only be add to the root entity class
        // (base entity), we suppose that the discriminator column must always
        // defined in the base entity, thus we can use this assumption to check
        // if the current type is the root
        if (!persistenceTypeInfo.isDefinesDiscriminatorColumn()) {
            return;
        }

        if (inhStrategy == InheritanceStrategy.JOINED_SUBCLASS) {
            fragment.append(ANNOTATION_INHERITANCE).append("(strategy = ");
            fragment.append(ATTRIBUTE_INHERITANCE_TYPE).append(".JOINED)");
            fragment.addImport(IMPORT_INHERITANCE);
            fragment.addImport(IMPORT_INHERITANCE_TYPE);
        } else if (inhStrategy == InheritanceStrategy.SINGLE_TABLE) {
            // note that the single table inheritance strategy is the default
            // strategy, nevertheless we add this annotation
            fragment.append(ANNOTATION_INHERITANCE).append("(strategy = ");
            fragment.append(ATTRIBUTE_INHERITANCE_TYPE).append(".SINGLE_TABLE)");
            fragment.addImport(IMPORT_INHERITANCE);
            fragment.addImport(IMPORT_INHERITANCE_TYPE);
        }
    }

    private String getTableNameFromSupertype(IPersistentTypeInfo persistenceTypeInfo) {
        SearchTableNameInSuperTypes searchTableNameInSuperTypes = new SearchTableNameInSuperTypes(
                persistenceTypeInfo.getIpsProject());
        searchTableNameInSuperTypes.start(persistenceTypeInfo.getPolicyCmptType());
        return searchTableNameInSuperTypes.tableName;
    }

    private void addAnnotationsForDescriminator(JavaCodeFragment fragment, IPersistentTypeInfo persistenceTypeInfo) {
        String discriminatorValue = persistenceTypeInfo.getDiscriminatorValue();
        if (!StringUtils.isEmpty(discriminatorValue)) {
            fragment.appendln(ANNOTATION_DISCRIMINATOR_VALUE + "(\"" + discriminatorValue + "\")");
            fragment.addImport(IMPORT_DISCRIMINATOR_VALUE);
        }

        if (!persistenceTypeInfo.isDefinesDiscriminatorColumn()) {
            return;
        }

        DiscriminatorDatatype discriminatorDatatype = persistenceTypeInfo.getDiscriminatorDatatype();
        String discriminatorColumnName = persistenceTypeInfo.getDiscriminatorColumnName();

        fragment.appendln(ANNOTATION_DISCRIMINATOR_COLUMN + "(name = \"" + discriminatorColumnName
                + "\", discriminatorType = DiscriminatorType." + discriminatorDatatype + ")");

        fragment.addImport(IMPORT_DISCRIMINATOR_COLUMN);
        fragment.addImport(IMPORT_DISCRIMINATOR_TYPE);
    }

    @Override
    public boolean isGenerateAnnotationForInternal(IIpsElement ipsElement) {
        if (!(ipsElement instanceof IPolicyCmptType)) {
            return false;
        }
        return ((IPolicyCmptType)ipsElement).isPersistentEnabled();
    }

    private class SearchTableNameInSuperTypes extends TypeHierarchyVisitor<IPolicyCmptType> {

        private String tableName = null;

        public SearchTableNameInSuperTypes(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) {
            String tableNameTemp = currentType.getPersistenceTypeInfo().getTableName();
            if (StringUtils.isNotEmpty(tableNameTemp)) {
                this.tableName = tableNameTemp;
                return false;
            }
            return true;
        }

    }

}
