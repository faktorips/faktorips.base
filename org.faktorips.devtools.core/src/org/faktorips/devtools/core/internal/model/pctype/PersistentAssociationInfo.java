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
import org.faktorips.devtools.core.model.pctype.IPersistentAssociationInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;

/**
 * 
 * @author Roman Grutza
 */
public class PersistentAssociationInfo implements IPersistentAssociationInfo {

    private String targetColumnName;
    private String sourceColumnName;
    private String joinTableName;
    private FetchType fetchType;
    private final PolicyCmptTypeAssociation policyCmptTypeAssociation;

    /**
     * @param policyCmptTypeAssociation
     */
    public PersistentAssociationInfo(PolicyCmptTypeAssociation policyCmptTypeAssociation) {
        this.policyCmptTypeAssociation = policyCmptTypeAssociation;
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
        return policyCmptTypeAssociation.hasInverseAssociation();
    }

    public boolean isCascading() {
        return policyCmptTypeAssociation.isAssoziation() || policyCmptTypeAssociation.isComposition();
    }

    public boolean isJoinTableRequired() throws CoreException {
        boolean isUnidirectional1ToManyComposition = isUnidirectional() && policyCmptTypeAssociation.isComposition()
                && policyCmptTypeAssociation.is1ToMany();

        boolean isOneToManyAssociation = policyCmptTypeAssociation.isAssoziation()
                && policyCmptTypeAssociation.is1ToMany();

        IPolicyCmptTypeAssociation inverseAssociation = policyCmptTypeAssociation
                .findInverseAssociation(policyCmptTypeAssociation.getIpsProject());

        boolean isInverseAssociationOneToMany = (inverseAssociation != null) && inverseAssociation.is1ToMany();

        boolean isManyToManyAssociation = isOneToManyAssociation && isInverseAssociationOneToMany;

        return isUnidirectional1ToManyComposition || isManyToManyAssociation;
    }

    public boolean isOrphanDeleting() {
        return isUnidirectional() && policyCmptTypeAssociation.isComposition() && policyCmptTypeAssociation.is1ToMany();
    }

    public boolean isUnidirectional() {
        return policyCmptTypeAssociation.hasInverseAssociation();
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

}
