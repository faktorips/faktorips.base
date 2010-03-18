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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.association.GenAssociation;

/**
 * This class generates JPA annotations for associations of policy component types.
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassAssociationJpaAnnGen extends AbstractAnnotationGenerator {

    // JPA imports
    private static final String IMPORT_JOIN_TABLE = "javax.persistence.JoinTable";
    private static final String IMPORT_JOIN_COLUMN = "javax.persistence.JoinColumn";
    private static final String IMPORT_ONE_TO_MANY = "javax.persistence.OneToMany";
    private static final String IMPORT_ONE_TO_ONE = "javax.persistence.OneToOne";
    private static final String IMPORT_MANY_TO_MANY = "javax.persistence.ManyToMany";
    private static final String IMPORT_MANY_TO_ONE = "javax.persistence.ManyToOne";
    private static final String IMPORT_CASCADE_TYPE = "javax.persistence.CascadeType";
    private static final String IMPORT_FETCH_TYPE = "javax.persistence.FetchType";

    private static final String ANNOTATION_JOIN_TABLE = "@JoinTable";
    private static final String ANNOTATION_JOIN_COLUMN = "@JoinColumn";
    private static final String ANNOTATION_ONE_TO_MANY = "@OneToMany";
    private static final String ANNOTATION_ONE_TO_ONE = "@OneToOne";
    private static final String ANNOTATION_MANY_TO_MANY = "@ManyToMany";
    private static final String ANNOTATION_MANY_TO_ONE = "@ManyToOne";

    // EclipseLink imports
    private static final String IMPORT_PRIVATE_OWNED = "org.eclipse.persistence.annotations.PrivateOwned";
    private static final String ANNOTATION_PRIVATE_OWNED = "@PrivateOwned";

    private static enum RELATIONSHIP_TYPE {
        UNKNOWN,
        ONE_TO_MANY,
        ONE_TO_ONE,
        MANY_TO_MANY,
        MANY_TO_ONE
    }

    private static Map<RELATIONSHIP_TYPE, String> importForRelationshipType = new HashMap<RELATIONSHIP_TYPE, String>(4);
    private static Map<RELATIONSHIP_TYPE, String> annotationForRelationshipType = new HashMap<RELATIONSHIP_TYPE, String>(
            4);

    static {
        importForRelationshipType.put(RELATIONSHIP_TYPE.ONE_TO_MANY, IMPORT_ONE_TO_MANY);
        importForRelationshipType.put(RELATIONSHIP_TYPE.ONE_TO_ONE, IMPORT_ONE_TO_ONE);
        importForRelationshipType.put(RELATIONSHIP_TYPE.MANY_TO_MANY, IMPORT_MANY_TO_MANY);
        importForRelationshipType.put(RELATIONSHIP_TYPE.MANY_TO_ONE, IMPORT_MANY_TO_ONE);
        annotationForRelationshipType.put(RELATIONSHIP_TYPE.ONE_TO_MANY, ANNOTATION_ONE_TO_MANY);
        annotationForRelationshipType.put(RELATIONSHIP_TYPE.ONE_TO_ONE, ANNOTATION_ONE_TO_ONE);
        annotationForRelationshipType.put(RELATIONSHIP_TYPE.MANY_TO_MANY, ANNOTATION_MANY_TO_MANY);
        annotationForRelationshipType.put(RELATIONSHIP_TYPE.MANY_TO_ONE, ANNOTATION_MANY_TO_ONE);
    }

    public PolicyCmptImplClassAssociationJpaAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ASSOCIATION;
    }

    public JavaCodeFragment createAnnotation(IIpsElement ipsElement) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)ipsElement;

        try {
            if (!association.getPersistenceAssociatonInfo().isValid()) {
                return fragment;
            }

            // get necessary generators
            GenAssociation genAssociation = getGenerator(association);
            IPolicyCmptTypeAssociation inverseAssociation = genAssociation.getInverseAssociation();
            GenAssociation genInverseAssociation = getGenerator(inverseAssociation);

            // add import and annotation depending on the relationship type (e.g. oneToMany)
            RELATIONSHIP_TYPE relationShip = evalRelationShipType(association, inverseAssociation);
            fragment.addImport(importForRelationshipType.get(relationShip));
            fragment.append(annotationForRelationshipType.get(relationShip));

            // add attributes to relationship annotation
            List<String> attributesToAppend = new ArrayList<String>();
            addAnnotationAttributeMappedBy(relationShip, fragment, attributesToAppend, genAssociation,
                    genInverseAssociation);
            addAnnotationAttributeCascade(relationShip, fragment, attributesToAppend, genAssociation,
                    genInverseAssociation);
            addAnnotationAttributeFetch(relationShip, fragment, attributesToAppend, genAssociation,
                    genInverseAssociation);
            addAnnotationAttributesTargetEntity(fragment, attributesToAppend, genAssociation, genInverseAssociation);
            appendAllAttributes(fragment, attributesToAppend);

            // evaluate further attributes depending on the relationship type
            addAnnotationFor(relationShip, fragment, genAssociation, genInverseAssociation);

            // add special annotation in case of join table needed
            addAnnotationJoinTable(fragment, genAssociation, genInverseAssociation);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        return fragment;
    }

    private void addAnnotationFor(RELATIONSHIP_TYPE relationShip,
            JavaCodeFragment fragment,
            GenAssociation genAssociation,
            GenAssociation genInverseAssociation) {
        IPolicyCmptTypeAssociation association = genAssociation.getAssociation();
        switch (relationShip) {
            case ONE_TO_MANY:
                if (!association.isAssoziation()) {
                    fragment.addImport(IMPORT_PRIVATE_OWNED);
                    fragment.appendln(ANNOTATION_PRIVATE_OWNED);
                }
                break;
            case ONE_TO_ONE:
                break;
            case MANY_TO_ONE:
                break;
            case MANY_TO_MANY:
                break;
            default:
                throw new RuntimeException("Error unknow relationship type: " + relationShip.toString());
        }

        IPersistentAssociationInfo persistenceAssociatonInfo = genAssociation.getAssociation()
                .getPersistenceAssociatonInfo();
        if (StringUtils.isNotEmpty(persistenceAssociatonInfo.getJoinColumnName())) {
            appendJoinColumn(fragment, persistenceAssociatonInfo.getJoinColumnName(), false);
        }
    }

    private GenAssociation getGenerator(IPolicyCmptTypeAssociation pcTypeAssociation) throws CoreException {
        if (pcTypeAssociation == null) {
            return null;
        }
        return getStandardBuilderSet().getGenerator(pcTypeAssociation.getPolicyCmptType()).getGenerator(
                pcTypeAssociation);
    }

    private GenPolicyCmptType getGenerator(IPolicyCmptType policyCmptType) throws CoreException {
        return getStandardBuilderSet().getGenerator(policyCmptType);
    }

    private void addAnnotationJoinTable(JavaCodeFragment fragment,
            GenAssociation genAssociation,
            GenAssociation genInverseAssociation) throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = genAssociation.getAssociation()
                .getPersistenceAssociatonInfo();
        if (!persistenceAssociatonInfo.isJoinTableRequired()) {
            return;
        }
        if (StringUtils.isBlank(persistenceAssociatonInfo.getJoinTableName())) {
            return;
        }
        fragment.addImport(IMPORT_JOIN_TABLE);
        fragment.append(ANNOTATION_JOIN_TABLE).append('(');
        appendName(fragment, persistenceAssociatonInfo.getJoinTableName());

        if (!StringUtils.isEmpty(persistenceAssociatonInfo.getSourceColumnName())
                || !StringUtils.isEmpty(persistenceAssociatonInfo.getTargetColumnName())) {
            fragment.append(", ");
        }
        appendJoinColumns(fragment, persistenceAssociatonInfo.getSourceColumnName(), false);
        if (!StringUtils.isEmpty(persistenceAssociatonInfo.getSourceColumnName())) {
            fragment.append(", ");
        }
        appendJoinColumns(fragment, persistenceAssociatonInfo.getTargetColumnName(), true);
        fragment.appendln(')');
    }

    /**
     * Appends a String with the following structure to the given fragment:
     * <p/>
     * XX=@JoinColumn(name = "columnName") <br/>
     * with XX=(joinColumns|inverseJoinColumns) depending on inverse parameter
     */
    private boolean appendJoinColumns(JavaCodeFragment fragment, String columnName, boolean inverse) {
        if (StringUtils.isEmpty(columnName)) {
            return false;
        }
        String lhs = inverse ? "inverseJoinColumns = " : "joinColumns = ";
        fragment.append(lhs);
        appendJoinColumn(fragment, columnName, inverse);
        return true;
    }

    private void appendJoinColumn(JavaCodeFragment fragment, String columnName, boolean inverse) {
        fragment.addImport(IMPORT_JOIN_COLUMN);
        fragment.append(ANNOTATION_JOIN_COLUMN).append('(');
        appendName(fragment, columnName).append(")");
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

    private void addAnnotationAttributesTargetEntity(JavaCodeFragment fragment,
            List<String> attributesToAppend,
            GenAssociation genAssociation,
            GenAssociation genInverseAssociation) throws CoreException {
        GenPolicyCmptType genTargetPolicyCmptType = getGenerator(genAssociation.getTargetPolicyCmptType());
        String targetQName = genTargetPolicyCmptType.getUnqualifiedClassName(false);
        fragment.addImport(genTargetPolicyCmptType.getQualifiedName(false));
        attributesToAppend.add("targetEntity = " + targetQName + ".class");
    }

    private void addAnnotationAttributeCascade(RELATIONSHIP_TYPE relationShip,
            JavaCodeFragment fragment,
            List<String> attributesToAppend,
            GenAssociation genAssociation,
            GenAssociation genInverseAssociation) {
        if (relationShip == RELATIONSHIP_TYPE.MANY_TO_ONE) {
            return;
        }

        fragment.addImport(IMPORT_CASCADE_TYPE);
        attributesToAppend.add("cascade=CascadeType.ALL");
    }

    private void addAnnotationAttributeFetch(RELATIONSHIP_TYPE relationShip,
            JavaCodeFragment fragment,
            List<String> attributesToAppend,
            GenAssociation genAssociation,
            GenAssociation genInverseAssociation) {
        fragment.addImport(IMPORT_FETCH_TYPE);
        IPersistentAssociationInfo persistenceAssociatonInfo = genAssociation.getAssociation()
                .getPersistenceAssociatonInfo();
        // note that the FetchType enumeration must be equal to the FetchType enumeration in JPA
        attributesToAppend.add("fetch=FetchType." + persistenceAssociatonInfo.getFetchType().toString());
    }

    /*
     * If the relationship is bidirectional, then set the mappedBy attribute to the name of the
     * field of the inverse side. Note that if the relationship is unidirectional then a further
     * table will be used to hold all associations to the target.
     */
    private void addAnnotationAttributeMappedBy(RELATIONSHIP_TYPE relationShip,
            JavaCodeFragment fragment,
            List<String> attributesToAppend,
            GenAssociation genAssociation,
            GenAssociation genInverseAssociation) throws CoreException {
        if (genInverseAssociation == null) {
            // inverse generator not exist,
            // maybe this is an unidirectional association
            return;
        }

        if (isOwnerOfRelationship(genAssociation.getAssociation(), genInverseAssociation.getAssociation())) {
            return;
        }

        // // the many-to-one side is the owning side, so the join column is defined on that side
        // if (relationShip != RELATIONSHIP_TYPE.MANY_TO_ONE) {
        // return false;
        // }

        attributesToAppend.add("mappedBy=\"" + genInverseAssociation.getFieldNameForAssociation() + "\"");
    }

    public boolean isOwnerOfRelationship(IPolicyCmptTypeAssociation association,
            IPolicyCmptTypeAssociation inverseAssociation) throws CoreException {
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
        if (persistenceAssociatonInfo.isUnidirectional()) {
            // if no inverse is given, then the association is always the owner
            return true;
        }
        // in bidirectional associations the many-to-one side is the owning side
        // therefore we use the detail to master side as owner where the join column will be defined
        if (association.isCompositionMasterToDetail()) {
            return false;
        }
        if (association.isCompositionDetailToMaster()) {
            return true;
        }

        if (inverseAssociation == null) {
            // error in bidirectional association, inverse not exists
            return false;
        }

        boolean isManyToMany = association.getMaxCardinality() > 1 && inverseAssociation.getMaxCardinality() > 1;
        if (isManyToMany) {
            // note that no matter which side is designated as the owner
            // we define here that the side with the join table is the owner
            return StringUtils.isNotEmpty(persistenceAssociatonInfo.getJoinTableName());
        }

        // no many-to-many association, the owner is the many-to-one side
        if (inverseAssociation.getMaxCardinality() > 1) {
            return true;
        }

        // TODO Joerg JPA wer ist der Owner bei Assoziationen 1:1
        return false;
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

    private RELATIONSHIP_TYPE evalRelationShipType(IPolicyCmptTypeAssociation association,
            IPolicyCmptTypeAssociation inverseAssociation) {
        int sourceCardinality = association.getMaxCardinality();
        int targetCardinality = inverseAssociation == null ? 0 : inverseAssociation.getMaxCardinality();

        if (sourceCardinality > 1 && !(targetCardinality > 1)) {
            return RELATIONSHIP_TYPE.ONE_TO_MANY;
        }
        if (sourceCardinality > 1 && targetCardinality > 1) {
            return RELATIONSHIP_TYPE.MANY_TO_MANY;
        }
        if (sourceCardinality == 1 && association.isQualified() && targetCardinality == 1) {
            // special case max 1 but is qualified, thus child's are stored in a list
            // and we need an one-to-many annotation
            return RELATIONSHIP_TYPE.ONE_TO_MANY;
        }
        if (sourceCardinality == 1 && targetCardinality == 1) {
            return RELATIONSHIP_TYPE.ONE_TO_ONE;
        }
        if (sourceCardinality == 1 && targetCardinality > 1) {
            return RELATIONSHIP_TYPE.MANY_TO_ONE;
        }
        if (sourceCardinality == 1 && targetCardinality == 0) {
            return RELATIONSHIP_TYPE.ONE_TO_ONE;
        }
        return RELATIONSHIP_TYPE.UNKNOWN;
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
