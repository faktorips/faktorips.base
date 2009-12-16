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

package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;

/**
 * A class that holds information of a policy component type association which is relevant for
 * persistence using the JPA (Java Persistence API).
 * <p/>
 * This information can be used to act as a hint to the code generator on how to realize the table
 * column(s) on the database side.
 * 
 * @author Roman Grutza
 */
public interface IPersistentAssociationInfo {

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "PersistenceAssociation"; //$NON-NLS-1$

    public final static String PROPERTY_JOIN_TABLE_NAME = "jpaJoinTableName";
    public final static String PROPERTY_SOURCE_COLUMN_NAME = "jpaSourceColumnName";
    public final static String PROPERTY_TARGET_COLUMN_NAME = "jpaTargetColumnName";
    public final static String PROPERTY_FETCH_TYPE = "jpaFetchType";

    public IPolicyCmptTypeAssociation getPolicyComponentTypeAssociation();

    public String getJoinTableName();

    public void setJoinTableName(String newJoinTableName);

    public String getSourceColumnName();

    public void setSourceColumnName(String newSourceColumnName);

    public String getTargetColumnName();

    public void setTargetColumnName(String newTargetColumnName);

    public FetchType getFetchType();

    public void setFetchType(FetchType fetchType);

    public boolean isUnidirectional();

    public boolean isBidirectional();

    public boolean isCascading();

    public boolean isOrphanDeleting();

    public boolean isJoinTableRequired() throws CoreException;

    /**
     * To determine whether to use cascading load on the database (also fetching dependent objects
     * of the object to load).
     */
    public enum FetchType {
        FETCH_LAZY,
        FETCH_EAGER;
    }

}
