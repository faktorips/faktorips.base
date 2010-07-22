/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

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
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo.RelationshipType;
import org.faktorips.devtools.stdbuilder.AbstractAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.persistence.IPersistenceProvider;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
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

    private static Map<RelationshipType, String> importForRelationshipType = new HashMap<RelationshipType, String>(4);
    private static Map<RelationshipType, String> annotationForRelationshipType = new HashMap<RelationshipType, String>(
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

    public PolicyCmptImplClassAssociationJpaAnnGen(StandardBuilderSet builderSet) {
        super(builderSet);
    }

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.POLICY_CMPT_IMPL_CLASS_ASSOCIATION;
    }

    @Override
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

            IPersistenceProvider persistenceProviderImpl = genAssociation.getGenType().getBuilderSet()
                    .getPersistenceProviderImplementation();

            // add import and annotation depending on the relationship type (e.g. oneToMany)
            RelationshipType relationShip = RelationshipType.UNKNOWN;
            if (inverseAssociation != null) {
                relationShip = association.getPersistenceAssociatonInfo().evalBidirectionalRelationShipType(
                        inverseAssociation);
            } else {
                relationShip = association.getPersistenceAssociatonInfo().evalUnidirectionalRelationShipType();
            }
            if (relationShip == RelationshipType.UNKNOWN) {
                throw new RuntimeException("Error evaluation the relationship type!");
            }

            fragment.addImport(importForRelationshipType.get(relationShip));
            fragment.append(annotationForRelationshipType.get(relationShip));

            // add attributes to relationship annotation
            List<String> attributesToAppend = new ArrayList<String>();
            addAnnotationAttributeMappedBy(relationShip, attributesToAppend, genAssociation, genInverseAssociation);
            addAnnotationAttributeCascadeType(fragment, attributesToAppend, genAssociation);
            addAnnotationAttributeFetch(fragment, attributesToAppend, genAssociation);
            addAnnotationAttributesTargetEntity(fragment, attributesToAppend, genAssociation);
            addAnnotationAttributeOrphanRemoval(persistenceProviderImpl, attributesToAppend);
            appendAllAttributes(fragment, attributesToAppend);

            // evaluate further attributes depending on the relationship type
            addAnnotationFor(persistenceProviderImpl, fragment, genAssociation);

            // add special annotation in case of join table needed
            addAnnotationJoinTable(fragment, genAssociation);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        return fragment;
    }

    private void addAnnotationAttributeOrphanRemoval(IPersistenceProvider persistenceProviderImpl,
            List<String> attributesToAppend) {
        // note that depending on the JPA implementation (not JPA
        // 2.0) the orphan removal feature could be set as attribute or separate annotation
        if (persistenceProviderImpl == null || !persistenceProviderImpl.isSupportingOrphanRemoval()) {
            return;
        }
        String attributeOrphanRemoval = persistenceProviderImpl.getRelationshipAnnotationAttributeOrphanRemoval();
        if (!StringUtils.isEmpty(attributeOrphanRemoval)) {
            attributesToAppend.add(attributeOrphanRemoval);
        }
    }

    private void addAnnotationFor(IPersistenceProvider persistenceProviderImpl,
            JavaCodeFragment fragment,
            GenAssociation genAssociation) {
        IPersistentAssociationInfo persistenceAssociatonInfo = genAssociation.getAssociation()
                .getPersistenceAssociatonInfo();

        // add orphan removal annotation, note that depending on the JPA implementation (not JPA
        // 2.0) the orphan removal feature could be set as attribute or separate annotation
        if (persistenceProviderImpl != null && persistenceProviderImpl.isSupportingOrphanRemoval()
                && persistenceAssociatonInfo.isOrphanRemoval()) {
            persistenceProviderImpl.addAnnotationOrphanRemoval(fragment);
        }

        if (StringUtils.isNotEmpty(persistenceAssociatonInfo.getJoinColumnName())) {
            appendJoinColumn(fragment, persistenceAssociatonInfo.getJoinColumnName());
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

    private void addAnnotationJoinTable(JavaCodeFragment fragment, GenAssociation genAssociation) throws CoreException {
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
        appendJoinColumn(fragment, columnName);
        return true;
    }

    private void appendJoinColumn(JavaCodeFragment fragment, String columnName) {
        fragment.addImport(IMPORT_JOIN_COLUMN);
        fragment.append(ANNOTATION_JOIN_COLUMN).append('(');
        appendName(fragment, columnName).append(")");
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

    private void addAnnotationAttributesTargetEntity(JavaCodeFragment fragment,
            List<String> attributesToAppend,
            GenAssociation genAssociation) throws CoreException {
        GenPolicyCmptType genTargetPolicyCmptType = getGenerator(genAssociation.getTargetPolicyCmptType());
        String targetQName = genTargetPolicyCmptType.getUnqualifiedClassName(false);
        fragment.addImport(genTargetPolicyCmptType.getQualifiedName(false));
        attributesToAppend.add("targetEntity = " + targetQName + ".class");
    }

    private void addAnnotationAttributeCascadeType(JavaCodeFragment fragment,
            List<String> attributesToAppend,
            GenAssociation genAssociation) {
        IPersistentAssociationInfo persistenceAssociatonInfo = genAssociation.getAssociation()
                .getPersistenceAssociatonInfo();
        List<String> cascadeTypes = new ArrayList<String>();
        if (persistenceAssociatonInfo.isCascadeTypeMerge() && persistenceAssociatonInfo.isCascadeTypeRemove()
                && persistenceAssociatonInfo.isCascadeTypePersist() && persistenceAssociatonInfo.isCascadeTypeRefresh()) {
            attributesToAppend.add("cascade=CascadeType.ALL");
        } else {
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
        }
        if (cascadeTypes.size() == 0) {
            return;
        }
        fragment.addImport(IMPORT_CASCADE_TYPE);
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

    private void addAnnotationAttributeFetch(JavaCodeFragment fragment,
            List<String> attributesToAppend,
            GenAssociation genAssociation) {
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
    private void addAnnotationAttributeMappedBy(RelationshipType relationShip,
            List<String> attributesToAppend,
            GenAssociation genAssociation,
            GenAssociation genInverseAssociation) throws CoreException {
        if (genInverseAssociation == null) {
            // inverse generator not exist,
            // maybe this is an unidirectional association
            return;
        }

        if (isOwnerOfRelationship(genAssociation.getAssociation(), genInverseAssociation.getAssociation())) {
            // the owned by must be defined on the inverse side of the owner side,
            // otherwise the joined table annotation will be ignored
            return;
        }

        // many-to-one side is the owning side, so a join column is defined on that side
        // the mappedBy attribute is not necessary
        if (relationShip == RelationshipType.MANY_TO_ONE) {
            return;
        }

        attributesToAppend.add("mappedBy=\"" + genInverseAssociation.getFieldNameForAssociation() + "\"");
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
