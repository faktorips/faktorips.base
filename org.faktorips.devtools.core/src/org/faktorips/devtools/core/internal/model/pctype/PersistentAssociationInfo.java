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
    private String targetColumnName = "";
    private String sourceColumnName = "";
    private String joinTableName = "";
    private FetchType fetchType = FetchType.LAZY;

    private IIpsObjectPart policyComponentTypeAssociation;

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

    public boolean isTransient() {
        return transientAssociation;
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

    private boolean isJoinTableRequired(IPolicyCmptTypeAssociation inverseAssociation) {
        boolean isOneToManyAssociation = getPolicyComponentTypeAssociation().is1ToMany();
        if (isUnidirectional()) {
            return isOneToManyAssociation;
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

    public void setTransient(boolean transientAssociation) {
        boolean oldValue = this.transientAssociation;
        this.transientAssociation = transientAssociation;
        valueChanged(oldValue, transientAssociation);
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
        sourceColumnName = element.getAttribute(PROPERTY_SOURCE_COLUMN_NAME);
        targetColumnName = element.getAttribute(PROPERTY_TARGET_COLUMN_NAME);
        joinTableName = element.getAttribute(PROPERTY_JOIN_TABLE_NAME);
        fetchType = FetchType.valueOf(element.getAttribute(PROPERTY_FETCH_TYPE));
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TRANSIENT, Boolean.toString(transientAssociation));
        element.setAttribute(PROPERTY_SOURCE_COLUMN_NAME, "" + sourceColumnName);
        element.setAttribute(PROPERTY_TARGET_COLUMN_NAME, "" + targetColumnName);
        element.setAttribute(PROPERTY_JOIN_TABLE_NAME, "" + joinTableName);
        element.setAttribute(PROPERTY_FETCH_TYPE, "" + fetchType);
    }

    @Override
    protected void validateThis(MessageList msgList, IIpsProject ipsProject) throws CoreException {
        IPolicyCmptTypeAssociation inverseAssociation = null;
        if (isBidirectional()) {
            inverseAssociation = getPolicyComponentTypeAssociation().findInverseAssociation(
                    getPolicyComponentTypeAssociation().getIpsProject());
            boolean transientMismatch = false;
            if (isTransient() || !getPolicyComponentTypeAssociation().getPolicyCmptType().isPersistentEnabled()) {
                if (inverseAssociation == null) {
                    return; // => different error, bidirectional with missing inverse
                }
                if (inverseAssociation.getPersistenceAssociatonInfo().isTransient()) {
                    return;
                }

                if (inverseAssociation.getPolicyCmptType().getPersistenceTypeInfo().isEnabled()) {
                    transientMismatch = true;
                }
            } else {
                if (inverseAssociation != null
                        && (inverseAssociation.getPersistenceAssociatonInfo().isTransient() || !inverseAssociation
                                .getPolicyCmptType().isPersistentEnabled())) {
                    transientMismatch = true;
                }
            }
            if (transientMismatch) {
                msgList.add(new Message(MSGCODE_TARGET_SIDE_NOT_TRANSIENT,
                        "In case of transient association, the target side must also be marked as transient.",
                        Message.ERROR, this, IPersistentAssociationInfo.PROPERTY_TRANSIENT));
                return;
            }
        }

        if (isJoinTableRequired(inverseAssociation)) {
            // validate join table name
            if (StringUtils.isBlank(joinTableName)) {
                msgList.add(new Message(MSGCODE_JOIN_TABLE_NAME_EMPTY, "The join table name is empty.", Message.ERROR,
                        this, IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME));
            } else {
                if (!PersistenceUtil.isValidDatabaseIdentifier(joinTableName)) {
                    msgList.add(new Message(MSGCODE_JOIN_TABLE_NAME_INVALID, "The join table name is invalid.",
                            Message.ERROR, this, IPersistentAssociationInfo.PROPERTY_JOIN_TABLE_NAME));
                }
            }
            // validate column names of join table if they are not blank
            if (!StringUtils.isBlank(sourceColumnName)) {
                if (!PersistenceUtil.isValidDatabaseIdentifier(sourceColumnName)) {
                    msgList.add(new Message(MSGCODE_SOURCE_COLUMN_NAME_INVALID, "The source column name is invalid.",
                            Message.ERROR, this, IPersistentAssociationInfo.PROPERTY_SOURCE_COLUMN_NAME));
                }
            }
            if (!StringUtils.isBlank(sourceColumnName)) {
                if (!PersistenceUtil.isValidDatabaseIdentifier(targetColumnName)) {
                    msgList.add(new Message(MSGCODE_TARGET_COLUMN_NAME_INVALID, "The target column name is invalid.",
                            Message.ERROR, this, IPersistentAssociationInfo.PROPERTY_TARGET_COLUMN_NAME));
                }
            }
        }
    }
}
