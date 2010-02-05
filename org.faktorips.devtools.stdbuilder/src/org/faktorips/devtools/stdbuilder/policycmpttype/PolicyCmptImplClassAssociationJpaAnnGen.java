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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.type.GenType;

/**
 * This class generates JPA annotations for associations of policy component types.
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassAssociationJpaAnnGen extends AbstractAnnotationGenerator {

    private static final String IMPORT_ONE_TO_MANY = "javax.persistence.OneToMany";
    private static final String IMPORT_JOIN_TABLE = "javax.persistence.JoinTable";
    private static final String IMPORT_MANY_TO_MANY = "javax.persistence.ManyToMany";
    private static final String IMPORT_ONE_TO_ONE = "javax.persistence.OneToOne";
    private static final String IMPORT_CASCADE_TYPE = "javax.persistence.CascadeType";
    private static final String IMPORT_JOIN_COLUMN = "javax.persistence.JoinColumn";
    private static final String IMPORT_TRANSIENT = "javax.persistence.Transient";

    private static final String ANNOTATION_ONE_TO_ONE = "@OneToOne";
    private static final String ANNOTATION_ONE_TO_MANY = "@OneToMany";
    private static final String ANNOTATION_JOIN_TABLE = "@JoinTable";
    private static final String ANNOTATION_MANY_TO_MANY = "@ManyToMany";
    private static final String ANNOTATION_JOIN_COLUMN = "@JoinColumn";
    private static final String ANNOTATION_TRANSIENT = "@Transient";

    public PolicyCmptImplClassAssociationJpaAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ASSOCIATION;
    }

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        IPolicyCmptTypeAssociation pcTypeAssociation = (IPolicyCmptTypeAssociation)ipsElement;
        IPersistentAssociationInfo associatonInfo = pcTypeAssociation.getPersistenceAssociatonInfo();

        try {
            if (pcTypeAssociation.isAssoziation()) {
                createAnnotationForAssociation(fragment, pcTypeAssociation, associatonInfo);
            }
            if (pcTypeAssociation.isComposition() && pcTypeAssociation.is1To1()) {
                createAnnotationForCompositionOneToOne(fragment, pcTypeAssociation, associatonInfo);
            }
            // TODO: code for 1:n compositions breaks the generated code because
            // "parentModelObject" field can only currently only be annotated @Transient
            if (pcTypeAssociation.isComposition() && pcTypeAssociation.is1ToMany()) {
                createAnnotationForCompositionOneToMany(fragment, pcTypeAssociation, associatonInfo);
            }

        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        return fragment;
    }

    private void createAnnotationForAssociation(JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation pcTypeAssociation,
            IPersistentAssociationInfo associatonInfo) throws CoreException {

        IIpsProject ipsProject = pcTypeAssociation.getIpsProject();
        IPolicyCmptType targetPcType = pcTypeAssociation.findTargetPolicyCmptType(ipsProject);
        String targetQName = getQualifiedImplClassName(targetPcType);

        IPersistentAssociationInfo persistenceAssociatonInfo = pcTypeAssociation.getPersistenceAssociatonInfo();
        fragment.addImport(IMPORT_CASCADE_TYPE);
        if (persistenceAssociatonInfo.isJoinTableRequired()) {
            createAnnotationsForAssociationManyToMany(fragment, targetQName, persistenceAssociatonInfo);

        } else if (persistenceAssociatonInfo.isUnidirectional() && pcTypeAssociation.is1ToMany()) {
            fragment.addImport(IMPORT_ONE_TO_MANY);
            fragment.append(ANNOTATION_ONE_TO_MANY).append("(");
            fragment.append("targetEntity = " + targetQName).append(".class");
            fragment.append(", cascade = CascadeType.ALL").appendln(")");
        }
    }

    private void createAnnotationsForAssociationManyToMany(JavaCodeFragment fragment,
            String targetQName,
            IPersistentAssociationInfo persistenceAssociatonInfo) {
        fragment.addImport(IMPORT_MANY_TO_MANY);
        fragment.addImport(IMPORT_JOIN_TABLE);
        fragment.addImport(IMPORT_JOIN_COLUMN);

        fragment.append(ANNOTATION_MANY_TO_MANY).append('(');
        fragment.append("targetEntity = " + targetQName).append(".class");
        fragment.append(", cascade = CascadeType.ALL").appendln(')');

        fragment.append(ANNOTATION_JOIN_TABLE).append('(');
        appendName(fragment, persistenceAssociatonInfo.getJoinTableName());

        String sourceColumnName = persistenceAssociatonInfo.getSourceColumnName();
        if (StringUtils.isNotBlank(sourceColumnName)) {
            fragment.append(", ");
            appendJoinColumns(fragment, sourceColumnName, false);
        }

        String targetColumnName = persistenceAssociatonInfo.getTargetColumnName();
        if (StringUtils.isNotBlank(targetColumnName)) {
            fragment.append(", ");
            appendJoinColumns(fragment, targetColumnName, true);
        }

        fragment.appendln(')');
    }

    /**
     * Appends a String with the following structure to the given fragment:
     * <p/>
     * XX=@JoinColumn(name = "columnName") <br/>
     * with XX=(joinColumns|inverseJoinColumns) depending on inverse parameter
     */
    private void appendJoinColumns(JavaCodeFragment fragment, String columnName, boolean inverse) {
        String lhs = inverse ? "inverseJoinColumns = " : "joinColumns = ";

        fragment.append(lhs).append(ANNOTATION_JOIN_COLUMN).append('(');
        appendName(fragment, columnName).append(")");
    }

    private void createAnnotationForCompositionOneToOne(JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation pcTypeAssociation,
            IPersistentAssociationInfo associatonInfo) throws CoreException {

        // detail to master compositions are realized using a "parentModelObject" field and thus
        // need to be annotated in a FieldGenerator class and not an AssociationGenerator
        if (pcTypeAssociation.isCompositionMasterToDetail()) {
            IIpsProject ipsProject = pcTypeAssociation.getIpsProject();
            IPolicyCmptType targetPcType = pcTypeAssociation.findTargetPolicyCmptType(ipsProject);

            String targetQName = getQualifiedImplClassName(targetPcType);
            String rootOfTargetQName = getRootOfTargetQName(targetPcType);

            fragment.addImport(IMPORT_ONE_TO_ONE);

            fragment.addImport(IMPORT_CASCADE_TYPE);

            fragment.addImport(rootOfTargetQName);

            fragment.append(ANNOTATION_ONE_TO_ONE);
            fragment.append('(').append("targetEntity = ");

            fragment.appendClassName(rootOfTargetQName).append(".class");

            fragment.append(", cascade = ").appendClassName(IMPORT_CASCADE_TYPE).append(".ALL");

            fragment.appendln(", orphanRemoval = true)");
        }
    }

    private void createAnnotationForCompositionOneToMany(JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation pcTypeAssociation,
            IPersistentAssociationInfo associatonInfo) {

        fragment.addImport(IMPORT_TRANSIENT);
        fragment.append(ANNOTATION_TRANSIENT);

        // TODO: merge with createAnnotationForCompositionOneToOne() when the code generation
        // for the "parentModelObject" field is fixed

        // change @OneToOne -> @OneToMany

        // additional to the annotations generated for the 1:1 case also add:
        // mappedBy="parentModelObject", ...
    }

    private String getRootOfTargetQName(IPolicyCmptType targetPcType) throws CoreException {
        FindRootTypeVisitor rootVisitor = new FindRootTypeVisitor(targetPcType.getIpsProject());
        rootVisitor.start(targetPcType);
        IPolicyCmptType rootType = rootVisitor.getRootType();

        return getQualifiedImplClassName(rootType);
    }

    private String getQualifiedImplClassName(IPolicyCmptType targetPcType) {
        return GenType.getQualifiedName(targetPcType, getStandardBuilderSet(), false);
    }

    private final class FindRootTypeVisitor extends TypeHierarchyVisitor {
        IPolicyCmptType rootType = null;

        private FindRootTypeVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) throws CoreException {
            IPolicyCmptType pcType = (IPolicyCmptType)currentType;
            if (pcType.hasSupertype()) {
                return true;
            }
            rootType = pcType;
            return false;
        }

        public IPolicyCmptType getRootType() {
            return rootType;
        }
    }

}
