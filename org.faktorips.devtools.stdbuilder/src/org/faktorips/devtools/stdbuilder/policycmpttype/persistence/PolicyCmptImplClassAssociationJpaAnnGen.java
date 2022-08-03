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
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.IPersistenceProvider.PersistenceAnnotation;
import org.faktorips.devtools.model.builder.IPersistenceProvider.PersistenceEnum;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo.RelationshipType;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.StringUtil;

/**
 * This class generates JPA annotations for associations of policy component types.
 * 
 * @see AnnotatedJavaElementType#POLICY_CMPT_IMPL_CLASS_ASSOCIATION_FIELD
 */
public class PolicyCmptImplClassAssociationJpaAnnGen extends AbstractJpaAnnotationGenerator {

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode generatorModelNode) {
        JavaCodeFragmentBuilder fragmentBuilder = new JavaCodeFragmentBuilder();
        if (generatorModelNode instanceof XPolicyAssociation) {
            XPolicyAssociation xPolicyAssociation = (XPolicyAssociation)generatorModelNode;

            IPolicyCmptTypeAssociation association = xPolicyAssociation.getAssociation();

            try {
                IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
                if (!persistenceAssociatonInfo.isValid(association.getIpsProject())) {
                    return fragmentBuilder.getFragment();
                }

                XPolicyAssociation xInverseAssociation = null;
                if (xPolicyAssociation.hasInverseAssociation()) {
                    xInverseAssociation = xPolicyAssociation.getInverseAssociation();
                }

                IIpsArtefactBuilderSet ipsArtefactBuilderSet = xPolicyAssociation.getIpsProject()
                        .getIpsArtefactBuilderSet();
                IPersistenceProvider persistenceProvider = ipsArtefactBuilderSet.getPersistenceProvider();
                if (persistenceProvider == null) {
                    return fragmentBuilder.getFragment();
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

                // add attributes to relationship annotation
                List<String> attributesToAppend = new ArrayList<>();
                if (xInverseAssociation != null) {
                    addAnnotationAttributeMappedBy(relationShip, attributesToAppend, association, xInverseAssociation);
                }
                addAnnotationAttributeCascadeType(persistenceProvider, fragmentBuilder, attributesToAppend,
                        association);
                addAnnotationAttributeFetch(persistenceProvider, fragmentBuilder, attributesToAppend, association);
                addAnnotationAttributesTargetEntity(attributesToAppend, xPolicyAssociation);
                addAnnotationAttributeOrphanRemoval(persistenceProvider, attributesToAppend, association);

                JavaCodeFragment params = new JavaCodeFragment();
                params.appendJoined(attributesToAppend);
                String annotationForRelationshipType = getAnnotationForRelationshipType(relationShip,
                        persistenceProvider);
                fragmentBuilder.annotationLn(annotationForRelationshipType, params);

                // evaluate further attributes depending on the relationship type
                addAnnotationFor(persistenceProvider, fragmentBuilder, association);

                // add special annotation in case of join table needed
                addAnnotationJoinTable(persistenceProvider, fragmentBuilder, association);
                addAnnotationIndex(persistenceProvider, fragmentBuilder, persistenceAssociatonInfo);
            } catch (IpsException e) {
                StdBuilderPlugin.log(e);
            }

        }
        return fragmentBuilder.getFragment();
    }

    private String getAnnotationForRelationshipType(RelationshipType relationShip,
            IPersistenceProvider persistenceProvider) {
        switch (relationShip) {
            case MANY_TO_MANY:
                return persistenceProvider.getQualifiedName(PersistenceAnnotation.ManyToMany);
            case MANY_TO_ONE:
                return persistenceProvider.getQualifiedName(PersistenceAnnotation.ManyToOne);
            case ONE_TO_MANY:
                return persistenceProvider.getQualifiedName(PersistenceAnnotation.OneToMany);
            case ONE_TO_ONE:
                return persistenceProvider.getQualifiedName(PersistenceAnnotation.OneToOne);

            default:
                return null;
        }

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

    private void addAnnotationFor(IPersistenceProvider persistenceProvider,
            JavaCodeFragmentBuilder fragmentBuilder,
            IPolicyCmptTypeAssociation association) {
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();

        // add orphan removal annotation, note that depending on the JPA implementation (not JPA
        // 2.0) the orphan removal feature could be set as attribute or separate annotation
        if (persistenceProvider != null && persistenceProvider.isSupportingOrphanRemoval()
                && persistenceAssociatonInfo.isOrphanRemoval()) {
            persistenceProvider.addAnnotationOrphanRemoval(fragmentBuilder);
        }

        if (StringUtils.isNotEmpty(persistenceAssociatonInfo.getJoinColumnName())) {
            appendJoinColumn(persistenceProvider, fragmentBuilder, persistenceAssociatonInfo.getJoinColumnName(),
                    persistenceAssociatonInfo.isJoinColumnNullable());
        }
    }

    private void addAnnotationJoinTable(IPersistenceProvider persistenceProvider,
            JavaCodeFragmentBuilder fragmentBuilder,
            IPolicyCmptTypeAssociation association) {
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
        if (!persistenceAssociatonInfo.isJoinTableRequired()
                || StringUtils.isBlank(persistenceAssociatonInfo.getJoinTableName())) {
            return;
        }

        JavaCodeFragmentBuilder params = new JavaCodeFragmentBuilder();
        appendName(params.getFragment(), persistenceAssociatonInfo.getJoinTableName());

        if (!StringUtils.isEmpty(persistenceAssociatonInfo.getSourceColumnName())
                || !StringUtils.isEmpty(persistenceAssociatonInfo.getTargetColumnName())) {
            params.append(", ");
        }
        appendJoinColumns(persistenceProvider, params, persistenceAssociatonInfo.getSourceColumnName(), false);
        if (!StringUtils.isEmpty(persistenceAssociatonInfo.getSourceColumnName())) {
            params.append(", ");
        }
        appendJoinColumns(persistenceProvider, params, persistenceAssociatonInfo.getTargetColumnName(), true);

        fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.JoinTable),
                params.getFragment());
    }

    /**
     * Appends a String with the following structure to the given fragment:
     * <p>
     * XX=@JoinColumn(name = "columnName") <br>
     * with XX=(joinColumns|inverseJoinColumns) depending on inverse parameter
     */
    private boolean appendJoinColumns(IPersistenceProvider persistenceProvider,
            JavaCodeFragmentBuilder fragmentBuilder,
            String columnName,
            boolean inverse) {
        if (StringUtils.isEmpty(columnName)) {
            return false;
        }
        String lhs = inverse ? "inverseJoinColumns = " : "joinColumns = ";
        fragmentBuilder.append(lhs);
        appendJoinColumn(persistenceProvider, fragmentBuilder, columnName, false);
        return true;
    }

    /**
     * Appends a String with the following structure to the given fragment:
     * <p>
     * XX=@JoinColumn(name = "columnName"[, nullable = false])
     */
    private void appendJoinColumn(IPersistenceProvider persistenceProvider,
            JavaCodeFragmentBuilder fragmentBuilder,
            String columnName,
            boolean nullable) {
        JavaCodeFragment params = new JavaCodeFragment();
        appendName(params, columnName);
        if (!nullable) {
            params.append(", nullable = false");
        }

        fragmentBuilder.annotationLn(persistenceProvider.getQualifiedName(PersistenceAnnotation.JoinColumn), params);
    }

    private void addAnnotationAttributesTargetEntity(List<String> attributesToAppend,
            XPolicyAssociation xPolicyAssociation) {
        attributesToAppend.add("targetEntity = " + xPolicyAssociation.getTargetClassName() + ".class");
    }

    private void addAnnotationAttributeCascadeType(IPersistenceProvider persistenceProvider,
            JavaCodeFragmentBuilder fragmentBuilder,
            List<String> attributesToAppend,
            IPolicyCmptTypeAssociation association) {
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
        List<String> cascadeTypes = getCascadeTypes(persistenceAssociatonInfo);
        if (cascadeTypes.size() == 0) {
            return;
        }
        String enumCascadeType = persistenceProvider.getQualifiedName(PersistenceEnum.CascadeType);
        fragmentBuilder.addImport(enumCascadeType);
        String cascadeType = StringUtil.unqualifiedName(enumCascadeType);
        if (persistenceAssociatonInfo.isCascadeTypeMerge() && persistenceAssociatonInfo.isCascadeTypeRemove()
                && persistenceAssociatonInfo.isCascadeTypePersist()
                && persistenceAssociatonInfo.isCascadeTypeRefresh()) {
            attributesToAppend.add("cascade=" + cascadeType + ".ALL");
            return;
        }
        String cascadeTypesAsString = "cascade={";
        cascadeTypesAsString += IpsStringUtils.join(cascadeTypes, t -> cascadeType + '.' + t, ",");
        cascadeTypesAsString += "}";
        attributesToAppend.add(cascadeTypesAsString);
    }

    private List<String> getCascadeTypes(IPersistentAssociationInfo persistenceAssociatonInfo) {
        List<String> cascadeTypes;
        cascadeTypes = new ArrayList<>();
        if (persistenceAssociatonInfo.isCascadeTypeMerge()) {
            cascadeTypes.add("MERGE");
        }
        if (persistenceAssociatonInfo.isCascadeTypeRemove()) {
            cascadeTypes.add("REMOVE");
        }
        if (persistenceAssociatonInfo.isCascadeTypePersist()) {
            cascadeTypes.add("PERSIST");
        }
        if (persistenceAssociatonInfo.isCascadeTypeRefresh()) {
            cascadeTypes.add("REFRESH");
        }
        return cascadeTypes;
    }

    private void addAnnotationAttributeFetch(IPersistenceProvider persistenceProvider,
            JavaCodeFragmentBuilder fragmentBuilder,
            List<String> attributesToAppend,
            IPolicyCmptTypeAssociation association) {
        String enumFetchType = persistenceProvider.getQualifiedName(PersistenceEnum.FetchType);
        fragmentBuilder.addImport(enumFetchType);
        IPersistentAssociationInfo persistenceAssociatonInfo = association.getPersistenceAssociatonInfo();
        // note that the FetchType enumeration must be equal to the FetchType enumeration in JPA
        attributesToAppend.add("fetch=" + StringUtil.unqualifiedName(enumFetchType) + '.'
                + persistenceAssociatonInfo.getFetchType().toString());
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
        // many-to-one side is the owning side, so a join column is defined on that side
        // the mappedBy attribute is not necessary
        if ((inverseAssociation == null) || isOwnerOfRelationship(association, inverseAssociation)
                || (relationShip == RelationshipType.MANY_TO_ONE)) {
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
        if ((inverseAssociation.getMaxCardinality() > 1)
                || StringUtils.isNotEmpty(persistenceAssociatonInfo.getJoinColumnName())) {
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
        if (!pcTypeAssociation.getPolicyCmptType().isPersistentEnabled()
                || pcTypeAssociation.getPersistenceAssociatonInfo().isTransient()) {
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
            JavaCodeFragmentBuilder fragmentBuilder,
            IPersistentAssociationInfo persistenceAssociatonInfo) {
        if (persistenceProvider.isSupportingIndex()) {
            fragmentBuilder.append(persistenceProvider.getIndexAnnotations(persistenceAssociatonInfo));
        }
    }

}
