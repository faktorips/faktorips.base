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
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;

/**
 * A class that holds information of a policy component type association which is relevant for
 * persistence using the JPA (Java Persistence API).
 * <p/>
 * This information is used as a hint to the code generator on how to realize the table column(s) on
 * the database side.
 * 
 * @author Roman Grutza
 */
public interface IPersistentAssociationInfo extends IIpsObjectPart {

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "PersistenceAssociation"; //$NON-NLS-1$

    /**
     * The name of the join table name property.
     */
    public final static String PROPERTY_JOIN_TABLE_NAME = "joinTableName"; //$NON-NLS-1$

    /**
     * The name of the source column name property. In a many-to-many relationship this is the name
     * of the column in the join table which references the owning side (foreign key to the owning
     * side).
     */
    public final static String PROPERTY_SOURCE_COLUMN_NAME = "sourceColumnName"; //$NON-NLS-1$

    /**
     * The name of the target column name property. In a many-to-many relationship this is the name
     * of the column in the join table which references the opposite of the owning side (foreign key
     * to the non-owning side).
     */
    public final static String PROPERTY_TARGET_COLUMN_NAME = "targetColumnName"; //$NON-NLS-1$

    /**
     * The name of the fetch type (lazy/eager) property.
     */
    public final static String PROPERTY_FETCH_TYPE = "fetchType"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PERSISTENCEASSOCIATION-"; //$NON-NLS-1$

    /**
     * Validation message code for empty join table name.
     */
    public static final String MSGCODE_JOIN_TABLE_NAME_EMPTY = MSGCODE_PREFIX + "JoinTableNameEmpty";

    /**
     * Validation message code for invalid join table name.
     */
    public static final String MSGCODE_JOIN_TABLE_NAME_INVALID = MSGCODE_PREFIX + "JoinTableNameInvalid";

    /**
     * Validation message code for invalid source column name.
     */
    public static final String MSGCODE_SOURCE_COLUMN_NAME_INVALID = MSGCODE_PREFIX + "SourceColumnNameInvalid";

    /**
     * Validation message code for invalid target column name.
     */
    public static final String MSGCODE_TARGET_COLUMN_NAME_INVALID = MSGCODE_PREFIX + "TargetColumnNameInvalid";

    /**
     * Returns the {@link IPolicyCmptTypeAssociation} this info object belongs to.
     */
    public IPolicyCmptTypeAssociation getPolicyComponentTypeAssociation();

    /**
     * Returns the join table name for this association required for n-to-m relationships. Returns
     * an empty String if it has not been set yet.
     */
    public String getJoinTableName();

    /**
     * Sets the join table name for this association.
     * <p/>
     * Note that the final column name in the database can differ from the given
     * <code>newJoinTableName</code> by means of an ITableColumnNamingStrategy which is set on a per
     * IpsProject basis.
     * 
     * @see ITableColumnNamingStrategy
     * 
     * @throws NullPointerException if <code>newJoinTableName</code> is <code>null</code>.
     */
    public void setJoinTableName(String newJoinTableName);

    /**
     * Returns the source column name for this association required for n-to-m relationships.
     * Returns an empty String if it has not been set yet.
     */
    public String getSourceColumnName();

    /**
     * Sets the source column name for this association which is required for n-to-m relationships.
     * 
     * Note that the final column name in the database can differ from the given
     * <code>newSourceColumnName</code> by means of an ITableColumnNamingStrategy which is set on a
     * per IpsProject basis.
     * 
     * @throws NullPointerException if <code>newSourceColumnName</code> is <code>null</code>.
     */
    public void setSourceColumnName(String newSourceColumnName);

    /**
     * Returns the target column name for this association required for n-to-m relationships.
     * Returns an empty String if it has not been set yet.
     */
    public String getTargetColumnName();

    /**
     * Sets the target column name for this association which is required for n-to-m relationships.
     * 
     * Note that the final column name in the database can differ from the given
     * <code>newTargetColumnName</code> by means of an ITableColumnNamingStrategy which is set on a
     * per IpsProject basis.
     * 
     * @throws NullPointerException if <code>newTargetColumnName</code> is <code>null</code>.
     */
    public void setTargetColumnName(String newTargetColumnName);

    /**
     * Returns the fetch type for this association.
     * 
     * @see FetchType
     */
    public FetchType getFetchType();

    /**
     * Sets the fetch type for this association.
     * 
     * @see FetchType
     */
    public void setFetchType(FetchType fetchType);

    /**
     * @return <code>true</code> if the corresponding association does not have an inverse relation.
     */
    public boolean isUnidirectional();

    /**
     * @return <code>true</code> if the corresponding association also has an inverse relation.
     */
    public boolean isBidirectional();

    /**
     * @return <code>true</code> when the cascading attribute is required for the corresponding
     *         association, <code>false</code> otherwise.
     */
    public boolean isCascading();

    /**
     * @return <code>true</code> when the orphan deletion attribute is required for the
     *         corresponding association, <code>false</code> otherwise.
     */
    public boolean isOrphanDeleting();

    /**
     * @return <code>true</code> for n-to-m relationships where a join table is mandatory,
     *         <code>false</code> otherwise.
     * 
     * @throws CoreException if errors occur during retrieval of the corresponding inverse relation.
     */
    public boolean isJoinTableRequired() throws CoreException;

    /**
     * Determines whether to use cascading load on the database (also fetching dependent objects of
     * the object to load instead of using deferred loading proxies).
     */
    public enum FetchType {
        FETCH_LAZY,
        FETCH_EAGER;
    }
}
