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
import org.faktorips.devtools.core.internal.model.pctype.PersistentTypeInfo;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.DiscriminatorDatatype;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * This class generates JPA annotations for policy component types.
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
    private final static String ANNOTATION_TABLE = "@Table";
    private final static String ANNOTATION_SECONDARY_TABLE = "@SecondaryTable";
    private static final String ANNOTATION_DISCRIMINATOR_COLUMN = "@DiscriminatorColumn";
    private static final String ANNOTATION_DISCRIMINATOR_VALUE = "@DiscriminatorValue";

    private static final String IMPORT_ENTITY = "javax.persistence.Entity";
    private static final String IMPORT_TABLE = "javax.persistence.Table";
    private static final String IMPORT_SECONDARY_TABLE = "javax.persistence.SecondaryTable";
    private static final String IMPORT_DISCRIMINATOR_COLUMN = "javax.persistence.DiscriminatorColumn";
    private static final String IMPORT_DISCRIMINATOR_TYPE = "javax.persistence.DiscriminatorType";
    private static final String IMPORT_DISCRIMINATOR_VALUE = "javax.persistence.DiscriminatorValue";

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS;
    }

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        IPolicyCmptType pcType = (IPolicyCmptType)ipsElement;

        IPersistentTypeInfo persistenceTypeInfo = pcType.getPersistenceTypeInfo();

        fragment.addImport(IMPORT_ENTITY);
        fragment.appendln(ANNOTATION_ENTITY);

        addAnnotationsForInheritanceStrategy(fragment, persistenceTypeInfo);
        addAnnotationsForDescriminator(fragment, persistenceTypeInfo);

        return fragment;
    }

    private void addAnnotationsForInheritanceStrategy(JavaCodeFragment fragment, IPersistentTypeInfo persistenceTypeInfo) {

        InheritanceStrategy inheritanceStrategy = persistenceTypeInfo.getInheritanceStrategy();

        switch (inheritanceStrategy) {
            case SINGLE_TABLE:
                fragment.appendln(ANNOTATION_TABLE + "(name = \"" + persistenceTypeInfo.getTableName() + "\")");
                fragment.addImport(IMPORT_TABLE);

                break;
            case JOINED_SUBCLASS:
                fragment.appendln(ANNOTATION_TABLE + "(name = \"" + persistenceTypeInfo.getTableName() + "\")");
                fragment.addImport(IMPORT_TABLE);

                break;

            case MIXED:
                fragment.appendln(ANNOTATION_SECONDARY_TABLE + "(name = \""
                        + persistenceTypeInfo.getSecondaryTableName() + "\")");
                fragment.addImport(IMPORT_SECONDARY_TABLE);
                break;
        }
    }

    private void addAnnotationsForDescriminator(JavaCodeFragment fragment, IPersistentTypeInfo persistenceTypeInfo) {
        if (persistenceTypeInfo.getInheritanceStrategy() == InheritanceStrategy.JOINED_SUBCLASS) {
            return;
        }

        DiscriminatorDatatype discriminatorDatatype = persistenceTypeInfo.getDiscriminatorDatatype();
        String discriminatorColumnName = persistenceTypeInfo.getDiscriminatorColumnName();
        String discriminatorValue = persistenceTypeInfo.getDiscriminatorValue();

        fragment.appendln(ANNOTATION_DISCRIMINATOR_COLUMN + "(name = \"" + discriminatorColumnName
                + "\", discriminatorType = DiscriminatorType." + discriminatorDatatype + ")");
        fragment.appendln(ANNOTATION_DISCRIMINATOR_VALUE + "(\"" + discriminatorValue + "\")");

        fragment.addImport(IMPORT_DISCRIMINATOR_COLUMN);
        fragment.addImport(IMPORT_DISCRIMINATOR_TYPE);
        fragment.addImport(IMPORT_DISCRIMINATOR_VALUE);
    }
}
