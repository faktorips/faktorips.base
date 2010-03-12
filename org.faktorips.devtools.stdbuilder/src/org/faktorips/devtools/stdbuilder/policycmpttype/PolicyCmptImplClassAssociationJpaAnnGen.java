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
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;
import org.faktorips.devtools.stdbuilder.type.GenType;

/**
 * This class generates JPA annotations for associations of policy component types.
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassAssociationJpaAnnGen extends AbstractAnnotationGenerator {

    // JPA imports
    private static final String IMPORT_ONE_TO_MANY = "javax.persistence.OneToMany";
    private static final String IMPORT_JOIN_TABLE = "javax.persistence.JoinTable";
    private static final String IMPORT_MANY_TO_MANY = "javax.persistence.ManyToMany";
    private static final String IMPORT_ONE_TO_ONE = "javax.persistence.OneToOne";
    private static final String IMPORT_JOIN_COLUMN = "javax.persistence.JoinColumn";
    private static final String IMPORT_TRANSIENT = "javax.persistence.Transient";
    private static final String IMPORT_CASCADE_TYPE = "javax.persistence.CascadeType";
    private static final String IMPORT_FETCH_TYPE = "javax.persistence.FetchType";

    private static final String ANNOTATION_ONE_TO_ONE = "@OneToOne";
    private static final String ANNOTATION_ONE_TO_MANY = "@OneToMany";
    private static final String ANNOTATION_JOIN_TABLE = "@JoinTable";
    private static final String ANNOTATION_MANY_TO_MANY = "@ManyToMany";
    private static final String ANNOTATION_JOIN_COLUMN = "@JoinColumn";
    private static final String ANNOTATION_TRANSIENT = "@Transient";

    // EclipseLink imports
    private static final String IMPORT_PRIVATE_OWNED = "org.eclipse.persistence.annotations.PrivateOwned";
    private static final String ANNOTATION_PRIVATE_OWNED = "@PrivateOwned";

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
            } else if (pcTypeAssociation.isComposition()) {
                if (pcTypeAssociation.getMaxCardinality() > 1) {
                    createAnnotationForCompositionOneToMany(fragment, pcTypeAssociation, associatonInfo);
                } else {
                    createAnnotationForCompositionOneToOne(fragment, pcTypeAssociation, associatonInfo);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        return fragment;
    }

    private void createAnnotationForCompositionManyToOne(JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation pcTypeAssociation,
            IPersistentAssociationInfo associatonInfo) {
        // TODO Auto-generated method stub

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

        fragment.append(ANNOTATION_MANY_TO_MANY).append('(');
        fragment.append("targetEntity = " + targetQName).append(".class");
        fragment.append(", cascade = CascadeType.ALL").appendln(')');

        fragment.addImport(IMPORT_MANY_TO_MANY);

        addAnnotationJoinTable(fragment, persistenceAssociatonInfo);
    }

    private void addAnnotationJoinTable(JavaCodeFragment fragment, IPersistentAssociationInfo persistenceAssociatonInfo) {
        if (StringUtils.isBlank(persistenceAssociatonInfo.getJoinTableName())) {
            return;
        }
        fragment.addImport(IMPORT_JOIN_TABLE);
        fragment.append(ANNOTATION_JOIN_TABLE).append('(');
        appendName(fragment, persistenceAssociatonInfo.getJoinTableName());

        appendJoinColumns(fragment, persistenceAssociatonInfo.getSourceColumnName(), false);
        appendJoinColumns(fragment, persistenceAssociatonInfo.getTargetColumnName(), true);

        fragment.appendln(')');
    }

    /**
     * Appends a String with the following structure to the given fragment:
     * <p/>
     * XX=@JoinColumn(name = "columnName") <br/>
     * with XX=(joinColumns|inverseJoinColumns) depending on inverse parameter
     */
    private void appendJoinColumns(JavaCodeFragment fragment, String columnName, boolean inverse) {
        if (StringUtils.isBlank(columnName)) {
            return;
        }
        String lhs = inverse ? "inverseJoinColumns = " : "joinColumns = ";
        fragment.addImport(IMPORT_JOIN_COLUMN);
        fragment.append(lhs).append(ANNOTATION_JOIN_COLUMN).append('(');
        appendName(fragment, columnName).append(")");
    }

    private void createAnnotationForCompositionOneToOne(JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation pcTypeAssociation,
            IPersistentAssociationInfo associatonInfo) throws CoreException {
        fragment.addImport(IMPORT_ONE_TO_ONE);
        fragment.append(ANNOTATION_ONE_TO_ONE);

        List<String> attributesToAppend = new ArrayList<String>();
        addAnnotationAttributeMappedBy(fragment, attributesToAppend, pcTypeAssociation);
        addAnnotationAttributeCascadeAllAndEager(fragment, attributesToAppend, pcTypeAssociation);
        appendAllAttributes(fragment, attributesToAppend);
        if (pcTypeAssociation.isCompositionMasterToDetail()) {
            fragment.addImport(IMPORT_PRIVATE_OWNED);
            fragment.appendln(ANNOTATION_PRIVATE_OWNED);
        }
    }

    public GenAssociation getGenAssociation(IPolicyCmptTypeAssociation pcTypeAssociation) throws CoreException {
        return getStandardBuilderSet().getGenerator(pcTypeAssociation.getPolicyCmptType()).getGenerator(
                pcTypeAssociation);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * "@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="policy")"
     * "@ANNOTATION_PRIVATE_OWNED"
     * </pre>
     */
    private void createAnnotationForCompositionOneToMany(JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation pcTypeAssociation,
            IPersistentAssociationInfo associatonInfo) throws CoreException {
        fragment.addImport(IMPORT_ONE_TO_MANY);
        fragment.append(ANNOTATION_ONE_TO_MANY);

        List<String> attributesToAppend = new ArrayList<String>();
        addAnnotationAttributeMappedBy(fragment, attributesToAppend, pcTypeAssociation);
        addAnnotationAttributeCascadeAllAndEager(fragment, attributesToAppend, pcTypeAssociation);
        appendAllAttributes(fragment, attributesToAppend);

        addAnnotationJoinTable(fragment, associatonInfo);

        fragment.addImport(IMPORT_PRIVATE_OWNED);
        fragment.appendln(ANNOTATION_PRIVATE_OWNED);
    }

    private void appendAllAttributes(JavaCodeFragment fragment, List<String> attributesToAppend) {
        fragment.append('(');
        for (Iterator<String> iterator = attributesToAppend.iterator(); iterator.hasNext();) {
            fragment.append(iterator.next());
            if (iterator.hasNext()) {
                fragment.append(",");
            }
        }
        fragment.append(')');
    }

    private boolean addAnnotationAttributeCascadeAllAndEager(JavaCodeFragment fragment,
            List<String> attributesToAppend,
            IPolicyCmptTypeAssociation pcTypeAssociation) {
        fragment.addImport(IMPORT_CASCADE_TYPE);
        fragment.addImport(IMPORT_FETCH_TYPE);
        attributesToAppend.add("cascade=CascadeType.ALL");
        // note that the FetchType enumeration must be equal to the FetchType enumeration in JPA
        attributesToAppend.add("fetch=FetchType."
                + pcTypeAssociation.getPersistenceAssociatonInfo().getFetchType().toString());
        return true;
    }

    /*
     * If the relationship is bidirectional, then set the mappedBy attribute to the name of the
     * field of the inverse side. Note that if the relationship is unidirectional then a further
     * table will be used to hold all associations to the target.
     */
    private boolean addAnnotationAttributeMappedBy(JavaCodeFragment fragment,
            List<String> attributesToAppend,
            IPolicyCmptTypeAssociation pcTypeAssociation) throws CoreException {
        if (StringUtils.isEmpty(pcTypeAssociation.getInverseAssociation())) {
            // no inverse specified
            // this is an unidirectional association
            return false;
        }
        GenAssociation generatorInverseAssociation = getGenAssociation(pcTypeAssociation)
                .getGeneratorForInverseAssociation();
        if (generatorInverseAssociation == null) {
            // inverse generator not found, maybe a problem in the generator
            return false;
        }

        attributesToAppend.add("mappedBy=\"" + generatorInverseAssociation.getFieldNameForAssociation() + "\"");
        return true;
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

    public boolean isGenerateAnnotationFor(IIpsElement ipsElement) {
        if (!(ipsElement instanceof IPolicyCmptTypeAssociation)) {
            return false;
        }
        IPolicyCmptTypeAssociation pcTypeAssociation = (IPolicyCmptTypeAssociation)ipsElement;
        if (!pcTypeAssociation.getPolicyCmptType().isPersistentEnabled()) {
            return false;
        }
        if (pcTypeAssociation.getPersistenceAssociatonInfo().isTransient()) {
            return false;
        }
        return isTargetPolicyCmptTypePersistenceEnabled(this, pcTypeAssociation);
    }

    /*
     * Returns <code>true</code> if the persistent is enabled on the target type otherwise
     * <code>false</code>
     */
    static boolean isTargetPolicyCmptTypePersistenceEnabled(IAnnotationGenerator generator,
            IPolicyCmptTypeAssociation pcTypeAssociation) {
        GenAssociation pcTypeAssociationGenerator;
        try {
            pcTypeAssociationGenerator = generator.getStandardBuilderSet().getGenerator(
                    pcTypeAssociation.getPolicyCmptType()).getGenerator(pcTypeAssociation);
            return pcTypeAssociationGenerator.getTargetPolicyCmptType().isPersistentEnabled();
        } catch (CoreException e) {
            // in some cases the getGenerator method could throw a CoreException e.g. if the
            // generator not exists and a new one are created lazily the generator validates the
            // IpsElement and if there was a core exception during validation
            IpsPlugin.log(e);
        }
        return false;
    }
}
