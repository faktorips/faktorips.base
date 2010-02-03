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
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Default implementation of {@link IPersistentAssociationInfo}.
 * 
 * @author Roman Grutza
 */
public class PersistentAssociationInfo extends AtomicIpsObjectPart implements IPersistentAssociationInfo {

    private String targetColumnName = "";
    private String sourceColumnName = "";
    private String joinTableName = "";
    private FetchType fetchType = FetchType.FETCH_LAZY;

    private IIpsObjectPart policyComponentTypeAssociation;

    /**
     */
    public PersistentAssociationInfo(IIpsObjectPart ipsObject, int id) {
        super(ipsObject, id);
        policyComponentTypeAssociation = ipsObject;

        ITableColumnNamingStrategy tableColumnNamingStrategy = getIpsProject().getTableColumnNamingStrategy();

        sourceColumnName = tableColumnNamingStrategy.getTableColumnName(getPolicyComponentTypeAssociation()
                .getName());
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

    public boolean isBidirectional() {
        return getPolicyComponentTypeAssociation().hasInverseAssociation();
    }

    public boolean isCascading() {
        return getPolicyComponentTypeAssociation().isAssoziation()
                || getPolicyComponentTypeAssociation().isComposition();
    }

    public boolean isJoinTableRequired() throws CoreException {
        boolean isUnidirectional1ToManyComposition = isUnidirectional()
                && getPolicyComponentTypeAssociation().isComposition()
                && getPolicyComponentTypeAssociation().is1ToMany();

        boolean isOneToManyAssociation = getPolicyComponentTypeAssociation().isAssoziation()
                && getPolicyComponentTypeAssociation().is1ToMany();

        IPolicyCmptTypeAssociation inverseAssociation = getPolicyComponentTypeAssociation().findInverseAssociation(
                getPolicyComponentTypeAssociation().getIpsProject());

        boolean isInverseAssociationOneToMany = (inverseAssociation != null) && inverseAssociation.is1ToMany();

        boolean isManyToManyAssociation = isOneToManyAssociation && isInverseAssociationOneToMany;

        return isUnidirectional1ToManyComposition || isManyToManyAssociation;
    }

    public boolean isOrphanDeleting() {
        return isUnidirectional() && getPolicyComponentTypeAssociation().isComposition()
                && getPolicyComponentTypeAssociation().is1ToMany();
    }

    public boolean isUnidirectional() {
        return getPolicyComponentTypeAssociation().hasInverseAssociation();
    }

    public void setFetchType(FetchType fetchType) {
        this.fetchType = fetchType;
    }

    public void setJoinTableName(String newJoinTableName) {
        if (StringUtils.isEmpty(newJoinTableName)) {
            throw new RuntimeException("Join table name must not be null or empty.");
        }
        joinTableName = newJoinTableName;
    }

    public void setSourceColumnName(String newSourceColumnName) {
        if (StringUtils.isEmpty(newSourceColumnName)) {
            throw new RuntimeException("Source column name must not be null or empty.");
        }
        sourceColumnName = newSourceColumnName;
    }

    public void setTargetColumnName(String newTargetColumnName) {
        if (StringUtils.isEmpty(newTargetColumnName)) {
            throw new RuntimeException("Target column name must not be null or empty.");
        }
        targetColumnName = newTargetColumnName;
    }

    public IPolicyCmptTypeAssociation getPolicyComponentTypeAssociation() {
        return (IPolicyCmptTypeAssociation)policyComponentTypeAssociation;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        sourceColumnName = element.getAttribute(PROPERTY_SOURCE_COLUMN_NAME);
        targetColumnName = element.getAttribute(PROPERTY_TARGET_COLUMN_NAME);
        joinTableName = element.getAttribute(PROPERTY_JOIN_TABLE_NAME);
        fetchType = FetchType.valueOf(element.getAttribute(PROPERTY_FETCH_TYPE));
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_SOURCE_COLUMN_NAME, "" + sourceColumnName);
        element.setAttribute(PROPERTY_TARGET_COLUMN_NAME, "" + targetColumnName);
        element.setAttribute(PROPERTY_JOIN_TABLE_NAME, "" + joinTableName);
        element.setAttribute(PROPERTY_FETCH_TYPE, "" + fetchType);
    }

    public Image getImage() {
        return policyComponentTypeAssociation.getImage();
    }

}
