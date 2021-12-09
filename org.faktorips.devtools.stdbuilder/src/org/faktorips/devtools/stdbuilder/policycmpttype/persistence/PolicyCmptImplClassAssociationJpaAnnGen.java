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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo.RelationshipType;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;

/**
 * This class generates JPA annotations for associations of policy component types.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD
 * 
 * @author Roman Grutza
 */
public class PolicyCmptImplClassAssociationJpaAnnGen extends AbstractJpaAnnotationGenerator {

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

    private static Map<RelationshipType, String> importForRelationshipType = new HashMap<>(4);
    private static Map<RelationshipType, String> annotationForRelationshipType = new HashMap<>(
            4);

    static {
        importForRelationshipType.put(RelationshipType.ONE_TO_MANY, IMPORT_ONE_TO_MANY);
        importForRelationshipType.put(RelationshipType.ONE_TO_ONE, IMPORT_ONE_TO_ONE);
        importForRelationshipType.put(RelationshipType.MANY_TO_MANY, IMPORT_MANY_TO_MANY);
        importForRelationshipType.put(RelationshipType.MANY_TO_ONE, IMPORT_MANY_TO_ONE);
        annotationForRelationshipType.put(RelationshipType.ONE_TO_MANY, ANNOTATION_ONE_TO_MANY);
        annotationForRelationshipType.put(RelationshipType.ONE_TO_ONE, ANNOTATION_ONE_TO_ONE);
        annotationForRelationshipType.put(RelationshipType.MANY_TO_MANY, ANNOTATION_MANY_TO_MANY);
        annotationForRelationshipType.put(RelationshipType.MANY_TO_ONE, ANNOTATION_MANY_TO_ONE);
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        if (generatorModelNode instanceof XPolicyAssociation) {
            XPolicyAssociation xPolicyAssociation = (XPolicyAssociation)generatorModelNode;

            IPolicyCmptTypeAssociation association = xPolicyAssociation.getAssociation();

            try {
                IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
                if (!persistenceAssociatonInfo.isValid(association.getIpsProject())) {
                    return fragment;
                }

                XPolicyAssociation xInverseAssociation = null;
                if (xPolicyAssociation.hasInverseAssociation()) {
                    xInverseAssociation = xPolicyAssociation.getInverseAssociation();
                }

                IIpsArtefactBuilderSet ipsArtefactBuilderSet = xPolicyAssociation.getIpsProject()
                        .getIpsArtefactBuilderSet();
                IPersistenceProvider persistenceProvider = ipsArtefactBuilderSet.getPersistenceProvider();
                if (persistenceProvider == null) {
                    return fragment;
                }

                // add import and annotation depending on the relationship type (e.g. oneToMany)
                RelationshipType relationShip = RelationshipType.UNKNOWN;
                if (xInverseAssociation != null) {
                    relationShip = persistenceAssociatonInfo
                            .evalBidirectionalRelationShipType(xInverseAssociation.getAssociation());
                } else {
                    relationShip = persistenceAssociatonInfo.evalUnidirectionalRelationShipType();
                }
                if (relationShip == RelationshipType.UNKNOWN) {
                    throw new RuntimeException("Error evaluation the relationship type!");
                }

                fragment.addImport(importForRelationshipType.get(relationShip));
                fragment.append(annotationForRelationshipType.get(relationShip));

                // add attributes to relationship annotation
                List<String> attributesToAppend = new ArrayList<>();
                if (xInverseAssociation != null) {
                    addAnnotationAttributeMappedBy(relationShip, attributesToAppend, association, xInverseAssociation);
                }
                addAnnotationAttributeCascadeType(fragment, attributesToAppend, association);
                addAnnotationAttributeFetch(fragment, attributesToAppend, association);
                addAnnotationAttributesTargetEntity(attributesToAppend, xPolicyAssociation);
                addAnnotationAttributeOrphanRemoval(persistenceProvider, attributesToAppend, association);
                appendAllAttributes(fragment, attributesToAppend);

                // evaluate further attributes depending on the relationship type
                addAnnotationFor(persistenceProvider, fragment, association);

                // add special annotation in case of join table needed
                addAnnotationJoinTable(fragment, association);
                addAnnotationIndex(persistenceProvider, fragment, persistenceAssociatonInfo);
            } catch (CoreException e) {
                StdBuilderPlugin.log(e);
            }

        }
        return fragment;
    }

    private void addAnnotationAttributeOrphanRemoval(IPersistenceProvider persistenceProvider,
            List<String> attributesToAppend,
            IPolicyCmptTypeAssociation association) {
        // note that depending on the JPA implementation (not JPA
        // 2.0) the orphan removal feature could be set as attribute or separate annotation
        if (persistenceProvider != null && persistenceProvider.isSupportingOrphanRemoval()) {
            IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
            if (persistenceAssociatonInfo.isOrphanRemoval()) {
                String attributeOrphanRemoval = persistenceProvider.getRelationshipAnnotationAttributeOrphanRemoval();
                if (!StringUtils.isEmpty(attributeOrphanRemoval)) {
                    attributesToAppend.add(attributeOrphanRemoval);
                }
            }
        }
    }

    private void addAnnotationFor(IPersistenceProvider persistenceProviderImpl,
            JavaCodeFragment fragment,
            IPolicyCmptTypeAssociation association) {
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();

        // add orphan removal annotation, note that depending on the JPA implementation (not JPA
        // 2.0) the orphan removal feature could be set as attribute or separate annotation
        if (persistenceProviderImpl != null && persistenceProviderImpl.isSupportingOrphanRemoval()
                && persistenceAssociatonInfo.isOrphanRemoval()) {
            persistenceProviderImpl.addAnnotationOrphanRemoval(fragment);
        }

        if (StringUtils.isNotEmpty(persistenceAssociatonInfo.getJoinColumnName())) {
            appendJoinColumn(fragment, persistenceAssociatonInfo.getJoinColumnName(),
                    persistenceAssociatonInfo.isJoinColumnNullable());
        }
    }

    private void addAnnotationJoinTable(JavaCodeFragment fragment, IPolicyCmptTypeAssociation association)
            throws CoreRuntimeException {
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
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
     * <p>
     * XX=@JoinColumn(name = "columnName") <br>
     * with XX=(joinColumns|inverseJoinColumns) depending on inverse parameter
     */
    private boolean appendJoinColumns(JavaCodeFragment fragment, String columnName, boolean inverse) {
        if (StringUtils.isEmpty(columnName)) {
            return false;
        }
        String lhs = inverse ? "inverseJoinColumns = " : "joinColumns = ";
        fragment.append(lhs);
        appendJoinColumn(fragment, columnName, false);
        return true;
    }

    /**
     * Appends a String with the following structure to the given fragment:
     * <p>
     * XX=@JoinColumn(name = "columnName"[, nullable = false])
     */
    private void appendJoinColumn(JavaCodeFragment fragment, String columnName, boolean nullable) {
        fragment.addImport(IMPORT_JOIN_COLUMN);
        fragment.append(ANNOTATION_JOIN_COLUMN).append('(');
        appendName(fragment, columnName);
        if (!nullable) {
            fragment.append(", nullable = false");
        }
        fragment.append(")");
    }

    private void appendAllAttributes(JavaCodeFragment fragment, List<String> attributesToAppend) {
        fragment.append('(');
        for (Iterator<String> iterator = attributesToAppend.iterator(); iterator.hasNext();) {
            String attributeToAppend = iterator.next();
            fragment.append(attributeToAppend);
            if (iterator.hasNext()) {
                fragment.append(",");
            }
        }
        fragment.append(')');
    }

    private void addAnnotationAttributesTargetEntity(List<String> attributesToAppend,
            XPolicyAssociation xPolicyAssociation) {
        attributesToAppend.add("targetEntity = " + xPolicyAssociation.getTargetClassName() + ".class");
    }

    private void addAnnotationAttributeCascadeType(JavaCodeFragment fragment,
            List<String> attributesToAppend,
            IPolicyCmptTypeAssociation association) {
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
        List<String> cascadeTypes = getCascadeTypes(persistenceAssociatonInfo);
        if (cascadeTypes.size() > 0) {
            fragment.addImport(IMPORT_CASCADE_TYPE);
        }
        if (persistenceAssociatonInfo.isCascadeTypeMerge() && persistenceAssociatonInfo.isCascadeTypeRemove()
                && persistenceAssociatonInfo.isCascadeTypePersist()
                && persistenceAssociatonInfo.isCascadeTypeRefresh()) {
            attributesToAppend.add("cascade=CascadeType.ALL");
            return;
        }
        if (cascadeTypes.size() == 0) {
            return;
        }
        String cascadeTypesAsString = "cascade={";
        for (Iterator<String> iterator = cascadeTypes.iterator(); iterator.hasNext();) {
            cascadeTypesAsString += iterator.next();
            if (iterator.hasNext()) {
                cascadeTypesAsString += ",";
            }
        }
        cascadeTypesAsString += "}";
        attributesToAppend.add(cascadeTypesAsString);
    }

    private List<String> getCascadeTypes(IPersistentAssociationInfo persistenceAssociatonInfo) {
        List<String> cascadeTypes;
        cascadeTypes = new ArrayList<>();
        if (persistenceAssociatonInfo.isCascadeTypeMerge()) {
            cascadeTypes.add("CascadeType.MERGE");
        }
        if (persistenceAssociatonInfo.isCascadeTypeRemove()) {
            cascadeTypes.add("CascadeType.REMOVE");
        }
        if (persistenceAssociatonInfo.isCascadeTypePersist()) {
            cascadeTypes.add("CascadeType.PERSIST");
        }
        if (persistenceAssociatonInfo.isCascadeTypeRefresh()) {
            cascadeTypes.add("CascadeType.REFRESH");
        }
        return cascadeTypes;
    }

    private void addAnnotationAttributeFetch(JavaCodeFragment fragment,
            List<String> attributesToAppend,
            IPolicyCmptTypeAssociation association) {
        fragment.addImport(IMPORT_FETCH_TYPE);
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
        // note that the FetchType enumeration must be equal to the FetchType enumeration in JPA
        attributesToAppend.add("fetch=FetchType." + persistenceAssociatonInfo.getFetchType().toString());
    }

    /*
     * If the relationship is bidirectional, then set the mappedBy attribute to the name of the
     * field of the inverse side. Note that if the relationship is unidirectional then a further
     * table will be used to hold all associations to the target.
     */
    private void addAnnotationAttributeMappedBy(RelationshipType relationShip,
            List<String> attributesToAppend,
            IPolicyCmptTypeAssociation association,
            XPolicyAssociation xInverseAssociation) {
        IPolicyCmptTypeAssociation inverseAssociation = xInverseAssociation.getAssociation();
        if (inverseAssociation == null) {
            // inverse generator not exist,
            // maybe this is an unidirectional association
            return;
        }

        if (isOwnerOfRelationship(association, inverseAssociation)) {
            // the owned by must be defined on the inverse side of the owner side,
            // otherwise the joined table annotation will be ignored
            return;
        }

        // many-to-one side is the owning side, so a join column is defined on that side
        // the mappedBy attribute is not necessary
        if (relationShip == RelationshipType.MANY_TO_ONE) {
            return;
        }

        attributesToAppend.add("mappedBy=\"" + xInverseAssociation.getFieldName() + "\"");
    }

    public boolean isOwnerOfRelationship(IPolicyCmptTypeAssociation association,
            IPolicyCmptTypeAssociation inverseAssociation) {
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

        if (StringUtils.isNotEmpty(persistenceAssociatonInfo.getJoinColumnName())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isGenerateAnnotationForInternal(IIpsElement ipsElement) {
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
        return isTargetPolicyCmptTypePersistenceEnabled(pcTypeAssociation);
    }

    /*
     * Returns <code>true</code> if the persistent is enabled on the target type otherwise
     * <code>false</code>
     */
    static boolean isTargetPolicyCmptTypePersistenceEnabled(IPolicyCmptTypeAssociation pcTypeAssociation) {
        IPolicyCmptType targetPolicyCmptType = pcTypeAssociation
                .findTargetPolicyCmptType(pcTypeAssociation.getIpsProject());
        return targetPolicyCmptType.isPersistentEnabled();
    }

    private void addAnnotationIndex(IPersistenceProvider persistenceProvider,
            JavaCodeFragment fragment,
            IPersistentAssociationInfo persistenceAssociatonInfo) {
        if (persistenceProvider.isSupportingIndex()) {
            fragment.append(persistenceProvider.getIndexAnnotations(persistenceAssociatonInfo));
        }
    }

}
