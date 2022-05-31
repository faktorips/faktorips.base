/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.pctype.persistence;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.internal.pctype.Messages;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.persistence.IPersistentAssociationInfo;
import org.faktorips.devtools.model.pctype.persistence.IPersistentTypeInfo;
import org.faktorips.devtools.model.util.PersistenceUtil;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IPersistentAssociationInfo}.
 * 
 * @author Roman Grutza
 */
public class PersistentAssociationInfo extends PersistentTypePartInfo implements IPersistentAssociationInfo {

    private boolean ownerOfManyToManyAssociation = false;

    private boolean orphanRemoval;

    private boolean cascadeTypeOverwriteDefault;
    private boolean cascadeTypePersist;
    private boolean cascadeTypeMerge;
    private boolean cascadeTypeRemove;
    private boolean cascadeTypeRefresh;

    private String joinTableName = ""; //$NON-NLS-1$

    private String targetColumnName = ""; //$NON-NLS-1$

    private String sourceColumnName = ""; //$NON-NLS-1$

    private String joinColumnName = ""; //$NON-NLS-1$

    private boolean joinColumnNullable = true;

    private FetchType fetchType = FetchType.LAZY;

    private IPolicyCmptTypeAssociation policyComponentTypeAssociation;

    public PersistentAssociationInfo(IPolicyCmptTypeAssociation policyComponentTypeAssociation, String id) {
        super(policyComponentTypeAssociation, id);
        this.policyComponentTypeAssociation = policyComponentTypeAssociation;

        ITableColumnNamingStrategy tableColumnNamingStrategy = getIpsProject().getTableColumnNamingStrategy();

        sourceColumnName = tableColumnNamingStrategy.getTableColumnName(getPolicyComponentTypeAssociation().getName());
        targetColumnName = tableColumnNamingStrategy.getTableColumnName(getPolicyComponentTypeAssociation().getName());
    }

    @Override
    public FetchType getFetchType() {
        return fetchType;
    }

    @Override
    public String getJoinTableName() {
        return joinTableName;
    }

    @Override
    public String getSourceColumnName() {
        return sourceColumnName;
    }

    @Override
    public String getTargetColumnName() {
        return targetColumnName;
    }

    @Override
    public String getJoinColumnName() {
        return joinColumnName;
    }

    @Override
    public boolean isJoinColumnNullable() {
        return joinColumnNullable;
    }

    @Override
    public boolean isOwnerOfManyToManyAssociation() {
        return ownerOfManyToManyAssociation;
    }

    @Override
    public boolean isOrphanRemoval() {
        return orphanRemoval;
    }

    @Override
    public boolean isCascadeTypeOverwriteDefault() {
        return cascadeTypeOverwriteDefault;
    }

    @Override
    public void setCascadeTypeOverwriteDefault(boolean cascadeTypeOverwriteDefault) {
        if (!cascadeTypeOverwriteDefault) {
            initDefaultsCascadeTypes();
        }
        boolean oldValue = this.cascadeTypeOverwriteDefault;
        this.cascadeTypeOverwriteDefault = cascadeTypeOverwriteDefault;
        valueChanged(oldValue, cascadeTypeOverwriteDefault);
    }

    @Override
    public void initDefaultsCascadeTypes() {
        IPolicyCmptTypeAssociation invAssociation = getPolicyComponentTypeAssociation().findInverseAssociation(
                getPolicyComponentTypeAssociation().getIpsProject());
        RelationshipType relationship = null;
        if (invAssociation == null) {
            relationship = evalUnidirectionalRelationShipType();
        } else {
            relationship = evalBidirectionalRelationShipType(invAssociation);
        }

        boolean isAssociation = getPolicyComponentTypeAssociation().isAssoziation();
        boolean isChildToParentComposition = getPolicyComponentTypeAssociation().isCompositionDetailToMaster();
        boolean isManyToOne = relationship == RelationshipType.MANY_TO_ONE;

        if (isAssociation || isChildToParentComposition || isManyToOne) {
            setAllCascadeTypes(false);
        } else {
            setAllCascadeTypes(true);
        }
    }

    private void setAllCascadeTypes(boolean enabled) {
        cascadeTypeMerge = enabled;
        cascadeTypeRefresh = enabled;
        cascadeTypeRemove = enabled;
        cascadeTypePersist = enabled;
    }

    @Override
    public boolean isCascadeTypePersist() {
        return cascadeTypePersist;
    }

    @Override
    public void setCascadeTypePersist(boolean cascadeTypePersist) {
        boolean oldValue = this.cascadeTypePersist;
        this.cascadeTypePersist = cascadeTypePersist;
        valueChanged(oldValue, cascadeTypePersist);
    }

    @Override
    public boolean isCascadeTypeMerge() {
        return cascadeTypeMerge;
    }

    @Override
    public void setCascadeTypeMerge(boolean cascadeTypeMerge) {
        boolean oldValue = this.cascadeTypeMerge;
        this.cascadeTypeMerge = cascadeTypeMerge;
        valueChanged(oldValue, cascadeTypeMerge);
    }

    @Override
    public boolean isCascadeTypeRemove() {
        return cascadeTypeRemove;
    }

    @Override
    public void setCascadeTypeRemove(boolean cascadeTypeRemove) {
        boolean oldValue = this.cascadeTypeRemove;
        this.cascadeTypeRemove = cascadeTypeRemove;
        valueChanged(oldValue, cascadeTypeRemove);
    }

    @Override
    public boolean isCascadeTypeRefresh() {
        return cascadeTypeRefresh;
    }

    @Override
    public void setCascadeTypeRefresh(boolean cascadeTypeRefresh) {
        boolean oldValue = this.cascadeTypeRefresh;
        this.cascadeTypeRefresh = cascadeTypeRefresh;
        valueChanged(oldValue, cascadeTypeRefresh);
    }

    @Override
    public boolean isBidirectional() {
        return getPolicyComponentTypeAssociation().hasInverseAssociation();
    }

    @Override
    public boolean isCascading() {
        return getPolicyComponentTypeAssociation().isAssoziation()
                || getPolicyComponentTypeAssociation().isComposition();
    }

    @Override
    public boolean isJoinTableRequired() {
        return isJoinTableRequired(getPolicyComponentTypeAssociation().findInverseAssociation(
                getPolicyComponentTypeAssociation().getIpsProject()));
    }

    public boolean isJoinTableRequired(IPolicyCmptTypeAssociation inverseAssociation) {
        boolean isOneToManyAssociation = getPolicyComponentTypeAssociation().is1ToMany();
        if (isUnidirectional()) {
            /*
             * force no need of join table if unidirectional on-to-many association if we add the
             * attribute joinColumn then the column will be created on the target side without
             * corresponding field on the target side
             */
            return false;
        }

        boolean isInverseAssociationOneToMany = (inverseAssociation != null) && inverseAssociation.is1ToMany();
        boolean isManyToManyAssociation = isOneToManyAssociation && isInverseAssociationOneToMany;

        return isManyToManyAssociation;
    }

    @Override
    public boolean isOrphanRemovalRequired() {
        return getPolicyComponentTypeAssociation().isCompositionMasterToDetail();
    }

    @Override
    public boolean isUnidirectional() {
        return !getPolicyComponentTypeAssociation().hasInverseAssociation();
    }

    @Override
    public void setFetchType(FetchType fetchType) {
        ArgumentCheck.notNull(fetchType);
        FetchType oldValue = this.fetchType;
        this.fetchType = fetchType;
        valueChanged(oldValue, fetchType);
    }

    @Override
    public void setJoinTableName(String newJoinTableName) {
        ArgumentCheck.notNull(newJoinTableName);
        String oldValue = joinTableName;
        joinTableName = newJoinTableName;
        valueChanged(oldValue, joinTableName);
    }

    @Override
    public void setSourceColumnName(String newSourceColumnName) {
        ArgumentCheck.notNull(newSourceColumnName);
        String oldValue = sourceColumnName;
        sourceColumnName = newSourceColumnName;
        valueChanged(oldValue, sourceColumnName);
    }

    @Override
    public void setTargetColumnName(String newTargetColumnName) {
        ArgumentCheck.notNull(newTargetColumnName);
        String oldValue = targetColumnName;
        targetColumnName = newTargetColumnName;
        valueChanged(oldValue, targetColumnName);
    }

    @Override
    public void setJoinColumnName(String newJoinColumnName) {
        ArgumentCheck.notNull(newJoinColumnName);
        String oldValue = joinColumnName;
        joinColumnName = newJoinColumnName;
        valueChanged(oldValue, joinColumnName);
    }

    @Override
    public void setJoinColumnNullable(boolean nullable) {
        boolean oldValue = joinColumnNullable;
        joinColumnNullable = nullable;
        valueChanged(oldValue, joinColumnNullable);
    }

    @Override
    public void setOwnerOfManyToManyAssociation(boolean ownerOfManyToManyAssociation) {
        // clear other columns to hold a consistent state
        if (!ownerOfManyToManyAssociation) {
            setJoinTableName(StringUtils.EMPTY);
            setTargetColumnName(StringUtils.EMPTY);
            setSourceColumnName(StringUtils.EMPTY);
        } else {
            setJoinColumnName(StringUtils.EMPTY);
        }
        boolean oldValue = this.ownerOfManyToManyAssociation;
        this.ownerOfManyToManyAssociation = ownerOfManyToManyAssociation;
        valueChanged(oldValue, ownerOfManyToManyAssociation);
    }

    @Override
    public void setOrphanRemoval(boolean orphanRemoval) {
        boolean oldValue = this.orphanRemoval;
        this.orphanRemoval = orphanRemoval;
        valueChanged(oldValue, orphanRemoval);
    }

    /**
     * Returns <code>true</code> if the column to foreign key will be created on the target side.
     */
    public boolean isForeignKeyColumnCreatedOnTargetSide(IPolicyCmptTypeAssociation inverseAssociation) {
        return inverseAssociation == null && getPolicyComponentTypeAssociation().is1ToManyIgnoringQualifier();
    }

    @Override
    public boolean isForeignKeyColumnDefinedOnTargetSide() {
        return isForeignKeyColumnDefinedOnTargetSide(getPolicyComponentTypeAssociation().findInverseAssociation(
                getPolicyComponentTypeAssociation().getIpsProject()));
    }

    /**
     * Returns <code>true</code> if the foreign key is defined on the target side.
     */
    public boolean isForeignKeyColumnDefinedOnTargetSide(IPolicyCmptTypeAssociation inverseAssociation) {
        if (inverseAssociation == null) {
            return false;
        }

        /*
         * special case in one-to-one association the side if the join column is given on the target
         * side then the foreign key column is not necessary
         */
        if (getPolicyComponentTypeAssociation().isAssoziation() && inverseAssociation.is1To1()
                && StringUtils.isNotEmpty(inverseAssociation.getPersistenceAssociatonInfo().getJoinColumnName())) {
            return true;
        }

        return !isJoinColumnRequired(inverseAssociation);
    }

    /**
     * Returns <code>true</code> if the join column is required.
     * <table border="1">
     * <caption>Example:</caption>
     * <tr>
     * <td colspan=3><strong>bidirectional</strong></td>
     * </tr>
     * <tr>
     * <td>one-to-one</td>
     * <td>master-to-detail</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>one-to-one</td>
     * <td>detail-to-master</td>
     * <td><strong>true</strong></td>
     * </tr>
     * <tr>
     * <tr>
     * <td>one-to-one</td>
     * <td>association</td>
     * <td><strong>true</strong><em> (on one side)</em></td>
     * </tr>
     * <tr>
     * <td>one-to-many</td>
     * <td>master-to-detail</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>one-to-many</td>
     * <td>detail-to-master</td>
     * <td><em>false (but not supported)</em></td>
     * </tr>
     * <tr>
     * <td>one-to-many</td>
     * <td>association</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>many-to-one</td>
     * <td>master-to-detail</td>
     * <td><em>false (but not supported)</em></td>
     * </tr>
     * <tr>
     * <td>many-to-one</td>
     * <td>detail-to-master</td>
     * <td><strong>true</strong></td>
     * </tr>
     * <tr>
     * <td>many-to-one</td>
     * <td>association</td>
     * <td><strong>true</strong></td>
     * </tr>
     * <tr>
     * <td>many-to-many</td>
     * <td>all</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td colspan=3><strong>unidirectional</strong></td>
     * </tr>
     * <tr>
     * <td>all</td>
     * <td>all</td>
     * <td><strong>true</strong></td>
     * </tr>
     * </table>
     * <p>
     * Note that if this relationship side the owner of a many-to-many association then return
     * always <code>false</code>. If the associated policy component type association is a derived
     * union or the inverse of a derived union association then return <code>false</code>.
     */
    public boolean isJoinColumnRequired(IPolicyCmptTypeAssociation inverseAssociation) {
        if (isJoinColumnImpossible(inverseAssociation)) {
            return false;
        }
        if (isUnidirectional()) {
            return true;
        }
        if (isBidirectional() && inverseAssociation == null) {
            // error inverse not found
            return true;
        }
        RelationshipType relType = evalBidirectionalRelationShipType(inverseAssociation);
        if (relType == RelationshipType.ONE_TO_ONE) {
            return isJoinColumnRequiredForOneToOne(inverseAssociation);
        }
        if (relType == RelationshipType.MANY_TO_MANY) {
            return false;
        }
        if (relType == RelationshipType.ONE_TO_MANY) {
            return false;
        }
        if (relType == RelationshipType.MANY_TO_ONE) {
            return true;
        }
        throw new RuntimeException("'Unsupported relationship type: " + relType.toString()); //$NON-NLS-1$
    }

    private boolean isJoinColumnImpossible(IPolicyCmptTypeAssociation inverseAssociation) {
        if (getPolicyComponentTypeAssociation().isDerivedUnion()) {
            return true;
        } else if (inverseAssociation != null && inverseAssociation.isDerivedUnion()) {
            return true;
        } else {
            return isOwnerOfManyToManyAssociation();
        }
    }

    private boolean isJoinColumnRequiredForOneToOne(IPolicyCmptTypeAssociation inverseAssociation) {
        if (getPolicyComponentTypeAssociation().isCompositionMasterToDetail()) {
            return false;
        }
        if (inverseAssociation != null && getPolicyComponentTypeAssociation().isAssoziation()
                && StringUtils.isNotEmpty(inverseAssociation.getPersistenceAssociatonInfo().getJoinColumnName())) {
            // target join column is defined on the target side
            return false;
        }
        return true;
    }

    @Override
    public RelationshipType evalUnidirectionalRelationShipType() {
        if (getPolicyComponentTypeAssociation().is1ToMany()) {
            return RelationshipType.ONE_TO_MANY;
        } else if (getPolicyComponentTypeAssociation().is1To1()) {
            return RelationshipType.ONE_TO_ONE;
        }
        return RelationshipType.UNKNOWN;
    }

    @Override
    public RelationshipType evalBidirectionalRelationShipType(IPolicyCmptTypeAssociation inverseAssociation) {
        if (inverseAssociation == null) {
            return RelationshipType.UNKNOWN;
        }
        if (getPolicyComponentTypeAssociation().is1ToMany() && inverseAssociation.is1ToMany()) {
            return RelationshipType.MANY_TO_MANY;
        }
        if (getPolicyComponentTypeAssociation().is1ToMany() && inverseAssociation.is1To1()) {
            return RelationshipType.ONE_TO_MANY;
        }
        if (getPolicyComponentTypeAssociation().is1To1() && inverseAssociation.is1ToMany()) {
            return RelationshipType.MANY_TO_ONE;
        }
        if (getPolicyComponentTypeAssociation().is1To1() && inverseAssociation.is1To1()) {
            return RelationshipType.ONE_TO_ONE;
        }
        return RelationshipType.UNKNOWN;
    }

    @Override
    public IPolicyCmptTypeAssociation getPolicyComponentTypeAssociation() {
        return policyComponentTypeAssociation;
    }

    @Override
    public void initDefaults() {
        if (getPolicyComponentTypeAssociation().is1ToManyIgnoringQualifier()) {
            fetchType = FetchType.LAZY;
        } else {
            fetchType = FetchType.EAGER;
        }

        setOrphanRemoval(isOrphanRemovalRequired());

        initDefaultsCascadeTypes();
    }

    @Override
    protected String getXmlTag() {
        return XML_TAG;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        ownerOfManyToManyAssociation = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION);
        sourceColumnName = element.getAttribute(PROPERTY_SOURCE_COLUMN_NAME);
        targetColumnName = element.getAttribute(PROPERTY_TARGET_COLUMN_NAME);
        joinTableName = element.getAttribute(PROPERTY_JOIN_TABLE_NAME);
        fetchType = FetchType.valueOf(element.getAttribute(PROPERTY_FETCH_TYPE));
        joinColumnName = element.getAttribute(PROPERTY_JOIN_COLUMN_NAME);
        // joinColumnNullable default is true
        String strJoinColumnNullable = element.getAttribute(PROPERTY_JOIN_COLUMN_NULLABLE);
        joinColumnNullable = strJoinColumnNullable == null || strJoinColumnNullable.length() == 0 ? true
                : Boolean
                        .valueOf(strJoinColumnNullable);

        orphanRemoval = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_ORPHAN_REMOVAL);

        cascadeTypeOverwriteDefault = XmlUtil.getBooleanAttributeOrFalse(element,
                PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT);
        cascadeTypePersist = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_CASCADE_TYPE_PERSIST);
        cascadeTypeRefresh = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_CASCADE_TYPE_REFRESH);
        cascadeTypeMerge = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_CASCADE_TYPE_MERGE);
        cascadeTypeRemove = XmlUtil.getBooleanAttributeOrFalse(element, PROPERTY_CASCADE_TYPE_REMOVE);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        if (ownerOfManyToManyAssociation) {
            element.setAttribute(PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION,
                    Boolean.toString(ownerOfManyToManyAssociation));
        }
        element.setAttribute(PROPERTY_SOURCE_COLUMN_NAME, sourceColumnName);
        element.setAttribute(PROPERTY_TARGET_COLUMN_NAME, targetColumnName);
        element.setAttribute(PROPERTY_JOIN_TABLE_NAME, joinTableName);
        element.setAttribute(PROPERTY_FETCH_TYPE, String.valueOf(fetchType));
        element.setAttribute(PROPERTY_JOIN_COLUMN_NAME, joinColumnName);
        element.setAttribute(PROPERTY_JOIN_COLUMN_NULLABLE, Boolean.toString(joinColumnNullable));
        if (orphanRemoval) {
            element.setAttribute(PROPERTY_ORPHAN_REMOVAL, Boolean.toString(orphanRemoval));
        }

        if (cascadeTypeOverwriteDefault) {
            element.setAttribute(PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT,
                    Boolean.toString(cascadeTypeOverwriteDefault));
        }
        if (cascadeTypeMerge) {
            element.setAttribute(PROPERTY_CASCADE_TYPE_MERGE, Boolean.toString(cascadeTypeMerge));
        }
        if (cascadeTypePersist) {
            element.setAttribute(PROPERTY_CASCADE_TYPE_PERSIST, Boolean.toString(cascadeTypePersist));
        }
        if (cascadeTypeRemove) {
            element.setAttribute(PROPERTY_CASCADE_TYPE_REMOVE, Boolean.toString(cascadeTypeRemove));
        }
        if (cascadeTypeRefresh) {
            element.setAttribute(PROPERTY_CASCADE_TYPE_REFRESH, Boolean.toString(cascadeTypeRefresh));
        }
    }

    @Override
    protected void validateThis(MessageList msgList, IIpsProject ipsProject) {
        if (!getPolicyComponentTypeAssociation().getPolicyCmptType().isPersistentEnabled()) {
            return;
        }

        IPolicyCmptTypeAssociation inverseAssociation = null;
        if (isBidirectional()) {
            inverseAssociation = getPolicyComponentTypeAssociation().findInverseAssociation(
                    getPolicyComponentTypeAssociation().getIpsProject());
        }
        validateTransientMismatch(msgList, inverseAssociation);
        if (isTransient()) {
            return;
        }
        validateJoinColumn(msgList, inverseAssociation);
        validateJoinTable(msgList, inverseAssociation);
        validateLazyFetchOnSingleValuedAssociation(msgList, ipsProject, inverseAssociation);
        validateCascadeType(msgList);
        super.validateThis(msgList, ipsProject);
    }

    private void validateLazyFetchOnSingleValuedAssociation(MessageList msgList,
            IIpsProject ipsProject,
            IPolicyCmptTypeAssociation inverseAssociation) {
        if (ipsProject.getReadOnlyProperties().getPersistenceOptions().isAllowLazyFetchForSingleValuedAssociations()) {
            return;
        }
        RelationshipType relationshipType = RelationshipType.UNKNOWN;
        if (inverseAssociation == null) {
            relationshipType = evalUnidirectionalRelationShipType();
        } else {
            relationshipType = evalBidirectionalRelationShipType(inverseAssociation);
        }
        if ((relationshipType == RelationshipType.MANY_TO_ONE || relationshipType == RelationshipType.ONE_TO_ONE)
                && FetchType.LAZY == getFetchType()) {
            msgList.add(new Message(MSGCODE_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS_NOT_ALLOWED,
                    Messages.PersistentAssociationInfo_msgLazyFetchNotSupported, Message.ERROR, this,
                    IPersistentAssociationInfo.PROPERTY_FETCH_TYPE));
        }
    }

    private void validateJoinColumn(MessageList msgList, IPolicyCmptTypeAssociation inverseAssociation) {
        if (isJoinColumnRequired(inverseAssociation)) {
            // validate must not be empty
            validateJoinColumn(msgList, false);
        } else {
            // validate must be empty
            validateJoinColumn(msgList, true);
        }
    }

    private void validateTransientMismatch(MessageList msgList, IPolicyCmptTypeAssociation inverseAssociation) {
        boolean transientMismatch = false;
        if (inverseAssociation == null) {
            // different error
            return;
        }

        if (isTransient() || !isPersistentTypeEntity(getPolicyComponentTypeAssociation())) {
            // source side is transient or not marked as entity
            if (inverseAssociation.getPersistenceAssociatonInfo().isTransient()) {
                return;
            }
            if (isPersistentTypeEntity(inverseAssociation)) {
                transientMismatch = true;
            }
        } else {
            // source side is marked as entity
            if (inverseAssociation.getPersistenceAssociatonInfo().isTransient()
                    || !isPersistentTypeEntity(inverseAssociation)) {
                transientMismatch = true;
            }
        }
        if (transientMismatch) {
            msgList.add(new Message(MSGCODE_TRANSIENT_MISMATCH,
                    Messages.PersistentAssociationInfo_msgTransientMismatch, Message.ERROR, this,
                    IPersistentAssociationInfo.PROPERTY_TRANSIENT));
        }
    }

    private boolean isPersistentTypeEntity(IPolicyCmptTypeAssociation association) {
        return association.getPolicyCmptType().getPersistenceTypeInfo()
                .getPersistentType() == IPersistentTypeInfo.PersistentType.ENTITY;
    }

    private void validateJoinTable(MessageList msgList, IPolicyCmptTypeAssociation inverseAssociation) {
        if (inverseAssociation == null) {
            // different error, skip join table validation
            return;
        }

        // if no join table is required then mark as owner is invalid
        if (!isJoinTableRequired(inverseAssociation) && isOwnerOfManyToManyAssociation()) {
            msgList.add(new Message(MSGCODE_OWNER_OF_ASSOCIATION_MUST_NOT_GIVEN,
                    Messages.PersistentAssociationInfo_msgOwningSideManyToManyNotAllowed, Message.ERROR, this,
                    IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION));
            return;
        }

        validateJoinTableDetails(msgList, inverseAssociation);

        // validate missing owner of relationship
        if (isJoinTableRequired(inverseAssociation) && !isOwnerOfManyToManyAssociation()
                && !inverseAssociation.getPersistenceAssociatonInfo().isOwnerOfManyToManyAssociation()) {
            msgList.add(new Message(MSGCODE_OWNER_OF_ASSOCIATION_MISMATCH,
                    Messages.PersistentAssociationInfo_msgOwningSideMissing, Message.ERROR, this,
                    IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION));
        } else if (isJoinTableRequired(inverseAssociation) && isOwnerOfManyToManyAssociation()
                && inverseAssociation.getPersistenceAssociatonInfo().isOwnerOfManyToManyAssociation()) {
            msgList.add(new Message(MSGCODE_OWNER_OF_ASSOCIATION_MISMATCH,
                    Messages.PersistentAssociationInfo_msgOwningSideManyToManyMarkedOnBothSides, Message.ERROR, this,
                    IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION));
        }
    }

    private void validateJoinColumn(MessageList msgList, boolean mustBeEmpty) {
        validateEmptyAndValidDatabaseIdentifier(msgList, mustBeEmpty, MSGCODE_JOIN_COLUMN_NAME_EMPTY,
                MSGCODE_JOIN_COLUMN_NAME_INVALID, IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME, joinColumnName,
                Messages.PersistentAssociationInfo_joinColumnName);
        if (!mustBeEmpty) {
            // validate max join column name length
            validateMaxColumnNameLength(msgList, joinColumnName, Messages.PersistentAssociationInfo_joinColumnName,
                    MSGCODE_JOIN_COLUMN_NAME_INVALID, PROPERTY_JOIN_COLUMN_NAME);
        }
    }

    private void validateJoinTableDetails(MessageList msgList, IPolicyCmptTypeAssociation inverseAssociation) {
        validateJoinTableDetails(msgList,
                !(isJoinTableRequired(inverseAssociation) && isOwnerOfManyToManyAssociation()));
    }

    private void validateJoinTableDetails(MessageList msgList, boolean mustBeEmpty) {
        validateEmptyAndValidDatabaseIdentifier(msgList, mustBeEmpty, MSGCODE_JOIN_TABLE_NAME_EMPTY,
                MSGCODE_JOIN_TABLE_NAME_INVALID, IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME, joinTableName,
                Messages.PersistentAssociationInfo_joinTableName);
        validateEmptyAndValidDatabaseIdentifier(msgList, mustBeEmpty, MSGCODE_SOURCE_COLUMN_NAME_EMPTY,
                MSGCODE_SOURCE_COLUMN_NAME_INVALID, IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME,
                sourceColumnName, Messages.PersistentAssociationInfo_sourceColumnName);
        validateEmptyAndValidDatabaseIdentifier(msgList, mustBeEmpty, MSGCODE_TARGET_COLUMN_NAME_EMPTY,
                MSGCODE_TARGET_COLUMN_NAME_INVALID, IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME,
                targetColumnName, Messages.PersistentAssociationInfo_tagetColumnName);

        // validate max join table columns name source and target length
        if (!mustBeEmpty) {
            validateMaxColumnNameLength(msgList, sourceColumnName, Messages.PersistentAssociationInfo_sourceColumnName,
                    MSGCODE_SOURCE_COLUMN_NAME_INVALID, PROPERTY_SOURCE_COLUMN_NAME);
            validateMaxColumnNameLength(msgList, targetColumnName, Messages.PersistentAssociationInfo_tagetColumnName,
                    MSGCODE_TARGET_COLUMN_NAME_INVALID, PROPERTY_TARGET_COLUMN_NAME);
        }

        // validate max join table name length
        if (!mustBeEmpty) {
            int maxTableNameLenght = getIpsProject().getReadOnlyProperties().getPersistenceOptions()
                    .getMaxTableNameLength();
            if (joinTableName.length() > maxTableNameLenght) {
                msgList.add(new Message(MSGCODE_JOIN_TABLE_NAME_INVALID, MessageFormat.format(
                        Messages.PersistentAssociationInfo_msgJoinTableNameExceedsMaximumLength,
                        joinTableName.length(), maxTableNameLenght), Message.ERROR, this,
                        IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME));
            }
        }
    }

    private void validateMaxColumnNameLength(MessageList msgList,
            String columnName,
            String propertyName,
            String messageCode,
            String property) {

        int maxColumnNameLenght = getIpsProject().getReadOnlyProperties().getPersistenceOptions()
                .getMaxColumnNameLenght();
        if (columnName.length() > maxColumnNameLenght) {
            msgList.add(new Message(messageCode,
                    MessageFormat.format(Messages.PersistentAssociationInfo_msgMaxLengthExceeds,
                            new Object[] { propertyName, columnName.length(), maxColumnNameLenght }),
                    Message.ERROR, this,
                    property));
        }
    }

    private void validateEmptyAndValidDatabaseIdentifier(MessageList msgList,
            boolean mustBeEmpty,
            String msgCodeEmpty,
            String msgCodeInValid,
            String property,
            String value,
            String propertyName) {

        String emptyText = null;
        if (mustBeEmpty) {
            emptyText = MessageFormat.format(Messages.PersistentAssociationInfo_msgMustBeEmpty, propertyName);
        } else {
            emptyText = MessageFormat.format(Messages.PersistentAssociationInfo_msgMustNotBeEmpty, propertyName);
        }

        if (mustBeEmpty && !StringUtils.isEmpty(value) || !mustBeEmpty && StringUtils.isEmpty(value)) {
            msgList.add(new Message(msgCodeEmpty, emptyText, Message.ERROR, this, property));
        } else if (!mustBeEmpty && !PersistenceUtil.isValidDatabaseIdentifier(value)) {
            String invalidText = MessageFormat.format(Messages.PersistentAssociationInfo_msgIsInvalid, propertyName);
            msgList.add(new Message(msgCodeInValid, invalidText, Message.ERROR, this, property));
        }
    }

    private void validateCascadeType(MessageList msgList) {
        if (getPolicyComponentTypeAssociation().isCompositionDetailToMaster()) {
            List<String> invalidProperties = new ArrayList<>();
            if (cascadeTypeMerge) {
                invalidProperties.add(PROPERTY_CASCADE_TYPE_MERGE);
            }
            if (cascadeTypePersist) {
                invalidProperties.add(PROPERTY_CASCADE_TYPE_PERSIST);
            }
            if (cascadeTypeRemove) {
                invalidProperties.add(PROPERTY_CASCADE_TYPE_REMOVE);
            }
            if (cascadeTypeRefresh) {
                invalidProperties.add(PROPERTY_CASCADE_TYPE_REFRESH);
            }
            if (!invalidProperties.isEmpty()) {
                msgList.add(new Message(MSGCODE_CHILD_TO_PARENT_CASCADE_TYPE,
                        Messages.PersistentAssociationInfo_msgChildToParentCascadeType, Message.ERROR,
                        this, invalidProperties.toArray(new String[0])));
            }
        }
    }

}
