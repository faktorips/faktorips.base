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

package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
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
    private String joinTableName = "";
    private String targetColumnName = "";
    private String sourceColumnName = "";
    private String joinColumnName = "";
    private FetchType fetchType = FetchType.LAZY;

    private IIpsObjectPart policyComponentTypeAssociation;

    private static enum RELATIONSHIP_TYPE {
        UNKNOWN,
        ONE_TO_MANY,
        ONE_TO_ONE,
        MANY_TO_MANY,
        MANY_TO_ONE
    }

    /**
     */
    public PersistentAssociationInfo(IIpsObjectPart ipsObject, String id) {
        super(ipsObject, id);
        policyComponentTypeAssociation = ipsObject;

        ITableColumnNamingStrategy tableColumnNamingStrategy = getIpsProject().getTableColumnNamingStrategy();

        sourceColumnName = tableColumnNamingStrategy.getTableColumnName(getPolicyComponentTypeAssociation().getName());
        targetColumnName = tableColumnNamingStrategy.getTableColumnName(getPolicyComponentTypeAssociation().getName());
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public String getJoinTableName() {
        return joinTableName;
    }

    public String getSourceColumnName() {
        return sourceColumnName;
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    public String getJoinColumnName() {
        return joinColumnName;
    }

    public boolean isTransient() {
        return transientAssociation;
    }

    public boolean isOwnerOfManyToManyAssociation() {
        return ownerOfManyToManyAssociation;
    }

    public boolean isBidirectional() {
        return getPolicyComponentTypeAssociation().hasInverseAssociation();
    }

    public boolean isCascading() {
        return getPolicyComponentTypeAssociation().isAssoziation()
                || getPolicyComponentTypeAssociation().isComposition();
    }

    public boolean isJoinTableRequired() throws CoreException {
        return isJoinTableRequired(getPolicyComponentTypeAssociation().findInverseAssociation(
                getPolicyComponentTypeAssociation().getIpsProject()));
    }

    public boolean isJoinTableRequired(IPolicyCmptTypeAssociation inverseAssociation) {
        boolean isOneToManyAssociation = getPolicyComponentTypeAssociation().is1ToMany();
        if (isUnidirectional()) {
            // force no need of join table if unidirectional on-to-many association
            // if we add the attribute joinColumn then the column will be created on the target side
            // without corresponding field on the target side
            return false;
        }

        boolean isInverseAssociationOneToMany = (inverseAssociation != null) && inverseAssociation.is1ToMany();
        boolean isManyToManyAssociation = isOneToManyAssociation && isInverseAssociationOneToMany;

        return isManyToManyAssociation;
    }

    public boolean isOrphanDeleting() {
        return isUnidirectional() && getPolicyComponentTypeAssociation().isComposition()
                && getPolicyComponentTypeAssociation().is1ToMany();
    }

    public boolean isUnidirectional() {
        return !getPolicyComponentTypeAssociation().hasInverseAssociation();
    }

    public void setFetchType(FetchType fetchType) {
        ArgumentCheck.notNull(fetchType);
        FetchType oldValue = this.fetchType;
        this.fetchType = fetchType;

        valueChanged(oldValue, fetchType);
    }

    public void setJoinTableName(String newJoinTableName) {
        ArgumentCheck.notNull(newJoinTableName);
        String oldValue = joinTableName;
        joinTableName = newJoinTableName;

        valueChanged(oldValue, joinTableName);
    }

    public void setSourceColumnName(String newSourceColumnName) {
        ArgumentCheck.notNull(newSourceColumnName);
        String oldValue = sourceColumnName;
        sourceColumnName = newSourceColumnName;

        valueChanged(oldValue, sourceColumnName);
    }

    public void setTargetColumnName(String newTargetColumnName) {
        ArgumentCheck.notNull(newTargetColumnName);
        String oldValue = targetColumnName;
        targetColumnName = newTargetColumnName;

        valueChanged(oldValue, targetColumnName);
    }

    public void setJoinColumnName(String newJoinColumnName) {
        ArgumentCheck.notNull(newJoinColumnName);
        String oldValue = joinColumnName;
        joinColumnName = newJoinColumnName;

        valueChanged(oldValue, joinColumnName);
    }

    public void setTransient(boolean transientAssociation) {
        boolean oldValue = this.transientAssociation;
        this.transientAssociation = transientAssociation;
        valueChanged(oldValue, transientAssociation);
    }

    public void setOwnerOfManyToManyAssociation(boolean ownerOfManyToManyAssociation) {
        // clear other columns to hold a consistent state
        if (!ownerOfManyToManyAssociation) {
            setJoinTableName("");
            setTargetColumnName("");
            setSourceColumnName("");
        } else {
            setJoinColumnName("");
        }
        boolean oldValue = this.ownerOfManyToManyAssociation;
        this.ownerOfManyToManyAssociation = ownerOfManyToManyAssociation;
        valueChanged(oldValue, ownerOfManyToManyAssociation);
    }

    /**
     * Returns <code>true</code> if the column to foreign key will be created on the target side.
     */
    public boolean isForeignKeyColumnCreatedOnTargetSide(IPolicyCmptTypeAssociation inverseAssociation) {
        return inverseAssociation == null && getPolicyComponentTypeAssociation().is1ToManyIgnoringQualifier();
    }

    /**
     * Returns <code>true</code> if the foreign key is defined on the target side.
     */
    public boolean isForeignKeyColumnDefinedOnTargetSide(IPolicyCmptTypeAssociation inverseAssociation) {
        if (inverseAssociation == null) {
            return false;
        }

        // special case in one-to-one association the side if the join column is given on the target
        // side then the foreign key column is not necessary
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
     * always <code>false</code>.
     */
    public boolean isJoinColumnRequired(IPolicyCmptTypeAssociation inverseAssociation) {
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
        RELATIONSHIP_TYPE relType = evalRelationShipType(inverseAssociation);
        if (relType == RELATIONSHIP_TYPE.ONE_TO_ONE) {
            if (getPolicyComponentTypeAssociation().isCompositionMasterToDetail()) {
                return false;
            }
            if (getPolicyComponentTypeAssociation().isAssoziation()
                    && StringUtils.isNotEmpty(inverseAssociation.getPersistenceAssociatonInfo().getJoinColumnName())) {
                // target join column is defined on the target side
                return false;
            }
            return true;
        }
        if (relType == RELATIONSHIP_TYPE.MANY_TO_MANY) {
            return false;
        }
        if (relType == RELATIONSHIP_TYPE.ONE_TO_MANY) {
            return false;
        }
        if (relType == RELATIONSHIP_TYPE.MANY_TO_ONE) {
            return true;
        }
        throw new RuntimeException("'Unsupported relationship type: " + relType.toString());
    }

    private RELATIONSHIP_TYPE evalRelationShipType(IPolicyCmptTypeAssociation inverseAssociation) {
        int sourceCardinality = getPolicyComponentTypeAssociation().getMaxCardinality();
        int targetCardinality = inverseAssociation.getMaxCardinality();

        // special case max 1 but is qualified, thus child's are stored in a list
        if (getPolicyComponentTypeAssociation().isQualified()) {
            sourceCardinality = Integer.MAX_VALUE;
        }
        if (inverseAssociation.isQualified()) {
            targetCardinality = Integer.MAX_VALUE;
        }

        if (sourceCardinality > 1 && targetCardinality == 1) {
            return RELATIONSHIP_TYPE.ONE_TO_MANY;
        }
        if (sourceCardinality > 1 && targetCardinality > 1) {
            return RELATIONSHIP_TYPE.MANY_TO_MANY;
        }
        if (sourceCardinality == 1 && targetCardinality == 1) {
            return RELATIONSHIP_TYPE.ONE_TO_ONE;
        }
        if (sourceCardinality == 1 && targetCardinality > 1) {
            return RELATIONSHIP_TYPE.MANY_TO_ONE;
        }
        return RELATIONSHIP_TYPE.UNKNOWN;
    }

    public IPolicyCmptTypeAssociation getPolicyComponentTypeAssociation() {
        return (IPolicyCmptTypeAssociation)policyComponentTypeAssociation;
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
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TRANSIENT, "" + Boolean.toString(transientAssociation));
        element.setAttribute(PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION, ""
                + Boolean.toString(ownerOfManyToManyAssociation));
        element.setAttribute(PROPERTY_SOURCE_COLUMN_NAME, "" + sourceColumnName);
        element.setAttribute(PROPERTY_TARGET_COLUMN_NAME, "" + targetColumnName);
        element.setAttribute(PROPERTY_JOIN_TABLE_NAME, "" + joinTableName);
        element.setAttribute(PROPERTY_FETCH_TYPE, "" + fetchType);
        element.setAttribute(PROPERTY_JOIN_COLUMN_NAME, "" + joinColumnName);
    }

    @Override
    protected void validateThis(MessageList msgList, IIpsProject ipsProject) throws CoreException {
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
            // different error, skip join table validation
            return;
        }

        if (isTransient() || !getPolicyComponentTypeAssociation().getPolicyCmptType().isPersistentEnabled()) {
            if (inverseAssociation.getPersistenceAssociatonInfo().isTransient()) {
                return;
            }

            if (inverseAssociation.getPolicyCmptType().getPersistenceTypeInfo().isEnabled()) {
                transientMismatch = true;
            }
        } else {
            if (inverseAssociation.getPersistenceAssociatonInfo().isTransient()
                    || !inverseAssociation.getPolicyCmptType().isPersistentEnabled()) {
                transientMismatch = true;
            }
        }
        if (transientMismatch) {
            msgList
                    .add(new Message(
                            MSGCODE_TRANSIENT_MISMATCH,
                            "In case of transient association, the target side must also be marked as transient and vise versa.",
                            Message.ERROR, this, IPersistentAssociationInfo.PROPERTY_TRANSIENT));
            return;
        }
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
                            "Must not be marked as owning side of many-to-many association because an join table is not required.",
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
                    "At least one assocition must be marked as the owning side of the relationship.", Message.ERROR,
                    this, IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION));
        } else if (isJoinTableRequired(inverseAssociation) && isOwnerOfManyToManyAssociation()
                && inverseAssociation.getPersistenceAssociatonInfo().isOwnerOfManyToManyAssociation()) {
            msgList.add(new Message(MSGCODE_OWNER_OF_ASSOCIATION_MISMATCH,
                    "The owning side of the relationship is marked on both sides.", Message.ERROR, this,
                    IPersistentAssociationInfo.PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION));
        }
    }

    private void validateJoinColumn(MessageList msgList, boolean mustBeEmpty) {
        validateEmptyAndValidDatabaseIdentifier(msgList, mustBeEmpty, MSGCODE_JOIN_COLUMN_NAME_EMPTY,
                MSGCODE_JOIN_COLUMN_NAME_INVALID, IPersistentAssociationInfo.PROPERTY_JOIN_COLUMN_NAME, joinColumnName,
                "join column name");
    }

    private void validateJoinTableDetails(MessageList msgList, boolean mustBeEmpty) {
        validateEmptyAndValidDatabaseIdentifier(msgList, mustBeEmpty, MSGCODE_JOIN_TABLE_NAME_EMPTY,
                MSGCODE_JOIN_TABLE_NAME_INVALID, IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME, joinTableName,
                "join table name");
        validateEmptyAndValidDatabaseIdentifier(msgList, mustBeEmpty, MSGCODE_SOURCE_COLUMN_NAME_EMPTY,
                MSGCODE_SOURCE_COLUMN_NAME_INVALID, IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME,
                sourceColumnName, "source column name");
        validateEmptyAndValidDatabaseIdentifier(msgList, mustBeEmpty, MSGCODE_TARGET_COLUMN_NAME_EMPTY,
                MSGCODE_TARGET_COLUMN_NAME_INVALID, IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME,
                targetColumnName, "target column name");
    }

    private void validateEmptyAndValidDatabaseIdentifier(MessageList msgList,
            boolean mustBeEmpty,
            String msgCodeEmpty,
            String msgCodeInValid,
            String property,
            String value,
            String propertyName) {
        String emptyText = "The " + propertyName + " must" + (mustBeEmpty ? "" : " not") + " be empty";
        String invalidText = propertyName + " is invalid.";
        if (mustBeEmpty && !StringUtils.isEmpty(value) || !mustBeEmpty && StringUtils.isEmpty(value)) {
            msgList.add(new Message(msgCodeEmpty, emptyText, Message.ERROR, this, property));
        } else if (!mustBeEmpty && !PersistenceUtil.isValidDatabaseIdentifier(value)) {
            msgList.add(new Message(msgCodeInValid, invalidText, Message.ERROR, this, property));
        }
    }
}
