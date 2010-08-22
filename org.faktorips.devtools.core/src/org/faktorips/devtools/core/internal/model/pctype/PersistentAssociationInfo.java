/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.util.PersistenceUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IPersistentAssociationInfo}.
 * 
 * @author Roman Grutza
 */
public class PersistentAssociationInfo extends AtomicIpsObjectPart implements IPersistentAssociationInfo {

    private boolean transientAssociation = false;

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

    private FetchType fetchType = FetchType.LAZY;

    private IIpsObjectPart policyComponentTypeAssociation;

    // workaround (MBT#280), due to problem in jmerge (MBT#223)
    private boolean manuallyCodeFixNecessary = false;

    public PersistentAssociationInfo(IIpsObjectPart ipsObject, String id) {
        super(ipsObject, id);
        policyComponentTypeAssociation = ipsObject;

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
    public boolean isTransient() {
        return transientAssociation;
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
    public void setCascadeTypeOverwriteDefault(boolean cascadeTypeOverwriteDefault) throws CoreException {
        if (!cascadeTypeOverwriteDefault) {
            initDefaultsCascadeTypes();
        }
        boolean oldValue = this.cascadeTypeOverwriteDefault;
        this.cascadeTypeOverwriteDefault = cascadeTypeOverwriteDefault;
        checkIfManuallyCodeFixIsNecessary(oldValue, cascadeTypeOverwriteDefault);
        valueChanged(oldValue, cascadeTypeOverwriteDefault);
    }

    private void checkIfManuallyCodeFixIsNecessary(boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            manuallyCodeFixNecessary = true;
        }
    }

    @Override
    public void initDefaultsCascadeTypes() throws CoreException {
        IPolicyCmptTypeAssociation invAssociation = getPolicyComponentTypeAssociation().findInverseAssociation(
                getPolicyComponentTypeAssociation().getIpsProject());
        RelationshipType relationShip = null;
        if (invAssociation == null) {
            relationShip = evalUnidirectionalRelationShipType();
        } else {
            relationShip = evalBidirectionalRelationShipType(invAssociation);
        }
        if (getPolicyComponentTypeAssociation().isAssoziation() || relationShip == RelationshipType.MANY_TO_ONE) {
            // no cascade type if association type is association or the relationShip type is
            // many to one
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
        checkIfManuallyCodeFixIsNecessary(oldValue, cascadeTypePersist);
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
        checkIfManuallyCodeFixIsNecessary(oldValue, cascadeTypeMerge);
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
        checkIfManuallyCodeFixIsNecessary(oldValue, cascadeTypeRemove);
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
        checkIfManuallyCodeFixIsNecessary(oldValue, cascadeTypeRefresh);
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
    public boolean isJoinTableRequired() throws CoreException {
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
    public void setTransient(boolean transientAssociation) {
        boolean oldValue = this.transientAssociation;
        this.transientAssociation = transientAssociation;
        valueChanged(oldValue, transientAssociation);
    }

    @Override
    public void setOwnerOfManyToManyAssociation(boolean ownerOfManyToManyAssociation) {
        // clear other columns to hold a consistent state
        if (!ownerOfManyToManyAssociation) {
            setJoinTableName(""); //$NON-NLS-1$
            setTargetColumnName("");//$NON-NLS-1$
            setSourceColumnName("");//$NON-NLS-1$
        } else {
            setJoinColumnName("");//$NON-NLS-1$
        }
        boolean oldValue = this.ownerOfManyToManyAssociation;
        this.ownerOfManyToManyAssociation = ownerOfManyToManyAssociation;
        valueChanged(oldValue, ownerOfManyToManyAssociation);
    }

    public void setOrphanRemoval(boolean orphanRemoval) {
        boolean oldValue = this.orphanRemoval;
        this.orphanRemoval = orphanRemoval;
        checkIfManuallyCodeFixIsNecessary(oldValue, orphanRemoval);
        valueChanged(oldValue, orphanRemoval);
    }

    /**
     * Returns <code>true</code> if the column to foreign key will be created on the target side.
     */
    public boolean isForeignKeyColumnCreatedOnTargetSide(IPolicyCmptTypeAssociation inverseAssociation) {
        return inverseAssociation == null && getPolicyComponentTypeAssociation().is1ToManyIgnoringQualifier();
    }

    @Override
    public boolean isForeignKeyColumnDefinedOnTargetSide() throws CoreException {
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
     * <table border=1>
     * <tr>
     * <td colspan=3><b>bidirectional</b></td>
     * </tr>
     * <tr>
     * <td>one-to-one</td>
     * <td>master-to-detail</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>one-to-one</td>
     * <td>detail-to-master</td>
     * <td><b>true</b></td>
     * </tr>
     * <tr>
     * <tr>
     * <td>one-to-one</td>
     * <td>association</td>
     * <td><b>true</b><i> (on one side)</i></td>
     * </tr>
     * <tr>
     * <td>one-to-many</td>
     * <td>master-to-detail</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>one-to-many</td>
     * <td>detail-to-master</td>
     * <td>
     * <i>false (but not supported)</i></td>
     * </tr>
     * <tr>
     * <td>one-to-many</td>
     * <td>association</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td>many-to-one</td>
     * <td>master-to-detail</td>
     * <td><i>false (but not supported)</i></td>
     * </tr>
     * <tr>
     * <td>many-to-one</td>
     * <td>detail-to-master</td>
     * <td><b>true</b></td>
     * </tr>
     * <tr>
     * <td>many-to-one</td>
     * <td>association</td>
     * <td><b>true</b></td>
     * </tr>
     * <tr>
     * <td>many-to-many</td>
     * <td>all</td>
     * <td>false</td>
     * </tr>
     * <tr>
     * <td colspan=3><b>unidirectional</b></td>
     * </tr>
     * <tr>
     * <td>all</td>
     * <td>all</td>
     * <td><b>true</b></td>
     * </tr>
     * </table>
     * Note that if this relationship side the owner of a many-to-many association then return
     * always <code>false</code>. If the associated policy component type association is a derived
     * union or the inverse of a derived union association then return <code>false</code>.
     */
    public boolean isJoinColumnRequired(IPolicyCmptTypeAssociation inverseAssociation) {
        if (getPolicyComponentTypeAssociation().isDerivedUnion()) {
            // derived union association
            return false;
        }
        if (inverseAssociation != null && inverseAssociation.isDerivedUnion()) {
            // inverse of a derived union association
            return false;
        }
        if (isOwnerOfManyToManyAssociation()) {
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
        return (IPolicyCmptTypeAssociation)policyComponentTypeAssociation;
    }

    @Override
    public void initDefaults() throws CoreException {
        if (getPolicyComponentTypeAssociation().is1ToManyIgnoringQualifier()) {
            fetchType = FetchType.LAZY;
        } else {
            fetchType = FetchType.EAGER;
        }

        setOrphanRemoval(isOrphanRemovalRequired());

        initDefaultsCascadeTypes();
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        transientAssociation = Boolean.valueOf(element.getAttribute(PROPERTY_TRANSIENT));
        ownerOfManyToManyAssociation = Boolean
                .valueOf(element.getAttribute(PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION));
        sourceColumnName = element.getAttribute(PROPERTY_SOURCE_COLUMN_NAME);
        targetColumnName = element.getAttribute(PROPERTY_TARGET_COLUMN_NAME);
        joinTableName = element.getAttribute(PROPERTY_JOIN_TABLE_NAME);
        fetchType = FetchType.valueOf(element.getAttribute(PROPERTY_FETCH_TYPE));
        joinColumnName = element.getAttribute(PROPERTY_JOIN_COLUMN_NAME);
        orphanRemoval = Boolean.valueOf(element.getAttribute(PROPERTY_ORPHAN_REMOVAL));

        cascadeTypeOverwriteDefault = Boolean.valueOf(element.getAttribute(PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT));
        cascadeTypePersist = Boolean.valueOf(element.getAttribute(PROPERTY_CASCADE_TYPE_PERSIST));
        cascadeTypeRefresh = Boolean.valueOf(element.getAttribute(PROPERTY_CASCADE_TYPE_REFRESH));
        cascadeTypeMerge = Boolean.valueOf(element.getAttribute(PROPERTY_CASCADE_TYPE_MERGE));
        cascadeTypeRemove = Boolean.valueOf(element.getAttribute(PROPERTY_CASCADE_TYPE_REMOVE));
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TRANSIENT, "" + Boolean.toString(transientAssociation)); //$NON-NLS-1$
        element.setAttribute(PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION, ""//$NON-NLS-1$
                + Boolean.toString(ownerOfManyToManyAssociation));
        element.setAttribute(PROPERTY_SOURCE_COLUMN_NAME, "" + sourceColumnName); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TARGET_COLUMN_NAME, "" + targetColumnName);//$NON-NLS-1$
        element.setAttribute(PROPERTY_JOIN_TABLE_NAME, "" + joinTableName);//$NON-NLS-1$
        element.setAttribute(PROPERTY_FETCH_TYPE, "" + fetchType);//$NON-NLS-1$
        element.setAttribute(PROPERTY_JOIN_COLUMN_NAME, "" + joinColumnName);//$NON-NLS-1$
        element.setAttribute(PROPERTY_ORPHAN_REMOVAL, Boolean.toString(orphanRemoval));

        element.setAttribute(PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT, Boolean.toString(cascadeTypeOverwriteDefault));
        element.setAttribute(PROPERTY_CASCADE_TYPE_MERGE, Boolean.toString(cascadeTypeMerge));
        element.setAttribute(PROPERTY_CASCADE_TYPE_PERSIST, Boolean.toString(cascadeTypePersist));
        element.setAttribute(PROPERTY_CASCADE_TYPE_REMOVE, Boolean.toString(cascadeTypeRemove));
        element.setAttribute(PROPERTY_CASCADE_TYPE_REFRESH, Boolean.toString(cascadeTypeRefresh));
    }

    @Override
    protected void validateThis(MessageList msgList, IIpsProject ipsProject) throws CoreException {
        if (!getPolicyComponentTypeAssociation().getPolicyCmptType().isPersistentEnabled()) {
            return;
        }

        if (manuallyCodeFixNecessary) {
            String textManualFixNecessary = NLS.bind(
                    Messages.PersistentAssociationInfo_msgWarningManualyCodeMergeNecessary,
                    getPolicyComponentTypeAssociation().getName());
            msgList
                    .add(new Message(MSGCODE_MANUALLY_CODE_FIX_NECESSARY, textManualFixNecessary, Message.WARNING, this));
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
    }

    private void validateLazyFetchOnSingleValuedAssociation(MessageList msgList,
            IIpsProject ipsProject,
            IPolicyCmptTypeAssociation inverseAssociation) {
        if (ipsProject.getProperties().getPersistenceOptions().isAllowLazyFetchForSingleValuedAssociations()) {
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
            msgList
                    .add(new Message(
                            MSGCODE_TRANSIENT_MISMATCH,
                            Messages.PersistentAssociationInfo_msgTransientMismatch,
                            Message.ERROR, this, IPersistentAssociationInfo.PROPERTY_TRANSIENT));
            return;
        }
    }

    private boolean isPersistentTypeEntity(IPolicyCmptTypeAssociation association) {
        return association.getPolicyCmptType().getPersistenceTypeInfo().getPersistentType() == IPersistentTypeInfo.PersistentType.ENTITY;
    }

    private void validateJoinTable(MessageList msgList, IPolicyCmptTypeAssociation inverseAssociation) {
        if (inverseAssociation == null) {
            // different error, skip join table validation
            return;
        }

        // if no join table is required then mark as owner is invalid
        if (!isJoinTableRequired(inverseAssociation) && isOwnerOfManyToManyAssociation()) {
            msgList
                    .add(new Message(
                            MSGCODE_OWNER_OF_ASSOCIATION_MUST_NOT_GIVEN,
                            Messages.PersistentAssociationInfo_msgOwningSideManyToManyNotAllowed,
                            Message.ERROR, this, IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION));
            return;
        }

        if (!isJoinTableRequired(inverseAssociation) || !isOwnerOfManyToManyAssociation()) {
            // all join table details must be empty
            validateJoinTableDetails(msgList, true);
        }

        if (isJoinTableRequired(inverseAssociation) && isOwnerOfManyToManyAssociation()) {
            // all join table details must not be empty
            validateJoinTableDetails(msgList, false);
        }

        // validate missing owner of relationship
        if (isJoinTableRequired(inverseAssociation) && !isOwnerOfManyToManyAssociation()
                && !inverseAssociation.getPersistenceAssociatonInfo().isOwnerOfManyToManyAssociation()) {
            msgList.add(new Message(MSGCODE_OWNER_OF_ASSOCIATION_MISMATCH,
                    Messages.PersistentAssociationInfo_msgOwningSideMissing, Message.ERROR,
                    this, IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION));
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
            int maxTableNameLenght = getIpsProject().getProperties().getPersistenceOptions().getMaxTableNameLength();
            if (joinTableName.length() > maxTableNameLenght) {
                msgList.add(new Message(MSGCODE_JOIN_TABLE_NAME_INVALID, NLS.bind(
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

        int maxColumnNameLenght = getIpsProject().getProperties().getPersistenceOptions().getMaxColumnNameLenght();
        if (columnName.length() > maxColumnNameLenght) {
            msgList
                    .add(new Message(
                            messageCode,
                            NLS
                                    .bind(
                                            Messages.PersistentAssociationInfo_msgMaxLengthExceeds,
                                            new Object[] { propertyName, columnName.length(), maxColumnNameLenght }),
                            Message.ERROR, this, property));
        }
    }

    private void validateEmptyAndValidDatabaseIdentifier(MessageList msgList,
            boolean mustBeEmpty,
            String msgCodeEmpty,
            String msgCodeInValid,
            String property,
            String value,
            String propertyName) {

        String emptyText = NLS.bind(Messages.PersistentAssociationInfo_msgMustBeEmpty, new Object[] { propertyName,
                (mustBeEmpty ? "" : Messages.PersistentAssociationInfo_msgNot) }); //$NON-NLS-1$
        String invalidText = NLS.bind(Messages.PersistentAssociationInfo_msgIsInvalid, propertyName);
        if (mustBeEmpty && !StringUtils.isEmpty(value) || !mustBeEmpty && StringUtils.isEmpty(value)) {
            msgList.add(new Message(msgCodeEmpty, emptyText, Message.ERROR, this, property));
        } else if (!mustBeEmpty && !PersistenceUtil.isValidDatabaseIdentifier(value)) {
            msgList.add(new Message(msgCodeInValid, invalidText, Message.ERROR, this, property));
        }
    }

    /**
     * Only necessary for test cases.
     */
    public void resetManuallyCodeFixNecessary() {
        valueChanged(manuallyCodeFixNecessary, false);
        manuallyCodeFixNecessary = false;
    }
}
