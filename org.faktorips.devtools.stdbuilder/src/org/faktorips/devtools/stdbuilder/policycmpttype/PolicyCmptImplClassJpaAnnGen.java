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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.internal.model.pctype.PersistentTypeInfo;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.PersistentType;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * A generator for JPA annotations of <code>IPolicyCmptType</code>s.
 * <p/>
 * Each persistent policy component type needs at least an <code>@Entity</code> annotation. The
 * information which annotations to generate is pulled from the class {@link PersistentTypeInfo}
 * which is part of persistent {@link IPolicyCmptType}s.
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassJpaAnnGen extends AbstractAnnotationGenerator {

    public PolicyCmptImplClassJpaAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    private final static String ANNOTATION_ENTITY = "@Entity";
    private final static String ANNOTATION_MAPPED_SUPERCLASS = "@MappedSuperclass";
    private final static String ANNOTATION_TABLE = "@Table";
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

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        IPolicyCmptType pcType = (IPolicyCmptType)ipsElement;

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

        if (StringUtils.isNotEmpty(tableName)) {
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
        SearchTableNameInSuperTypes searchTableNameInSuperTypes = new SearchTableNameInSuperTypes();
        try {
            searchTableNameInSuperTypes.start(persistenceTypeInfo.getPolicyCmptType());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return searchTableNameInSuperTypes.tableName;
    }

    private class SearchTableNameInSuperTypes extends PolicyCmptTypeHierarchyVisitor {
        private String tableName = null;

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            String tableName = currentType.getPersistenceTypeInfo().getTableName();
            if (StringUtils.isNotEmpty(tableName)) {
                this.tableName = tableName;
                return false;
            }
            return true;
        }
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

    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        if (!(ipsElement instanceof IPolicyCmptType)) {
            return false;
        }
        return ((IPolicyCmptType)ipsElement).isPersistentEnabled();
    }
}
