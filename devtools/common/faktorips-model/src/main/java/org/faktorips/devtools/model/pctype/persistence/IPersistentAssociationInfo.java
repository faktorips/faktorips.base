/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype.persistence;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;

/**
 * A class that holds information of a policy component type association which is relevant for
 * persistence using the JPA (Java Persistence API).
 * <p>
 * This information is used as a hint to the code generator on how to realize the table column(s) on
 * the database side.
 * 
 * @author Roman Grutza
 */
public interface IPersistentAssociationInfo extends IPersistentTypePartInfo {

    public enum RelationshipType {
        UNKNOWN,
        ONE_TO_MANY,
        ONE_TO_ONE,
        MANY_TO_MANY,
        MANY_TO_ONE
    }

    /** The XML tag for this IPS object part. */
    String XML_TAG = "PersistenceAssociation"; //$NON-NLS-1$

    /**
     * The name of a property that indicates that the association is transient.
     */
    String PROPERTY_OWNER_OF_MANY_TO_MANY_ASSOCIATION = "ownerOfManyToManyAssociation"; //$NON-NLS-1$

    /**
     * The name of the join table name property.
     */
    String PROPERTY_JOIN_TABLE_NAME = "joinTableName"; //$NON-NLS-1$

    /**
     * The name of the source column name property. In a many-to-many relationship this is the name
     * of the column in the join table which references the owning side (foreign key to the owning
     * side).
     */
    String PROPERTY_SOURCE_COLUMN_NAME = "sourceColumnName"; //$NON-NLS-1$

    /**
     * The name of the target column name property. In a many-to-many relationship this is the name
     * of the column in the join table which references the opposite of the owning side (foreign key
     * to the non-owning side).
     */
    String PROPERTY_TARGET_COLUMN_NAME = "targetColumnName"; //$NON-NLS-1$

    /**
     * The name of the fetch type (lazy / eager) property.
     */
    String PROPERTY_FETCH_TYPE = "fetchType"; //$NON-NLS-1$

    /**
     * The name of the join column name property. In a one-to-many relationship this is the name of
     * the column which references the opposite of the owning side (foreign key to the non-owning
     * side).
     */
    String PROPERTY_JOIN_COLUMN_NAME = "joinColumnName"; //$NON-NLS-1$

    /**
     * The name of the "join column is nullable" property, allowing NULL values in the database.
     */
    String PROPERTY_JOIN_COLUMN_NULLABLE = "joinColumnNullable"; //$NON-NLS-1$

    /**
     * Specifies if the orphan removal (private owned) annotation should be used or not. The
     * property make only sense on the master to detail side of an one to many composition.
     */
    String PROPERTY_ORPHAN_REMOVAL = "orphanRemoval"; //$NON-NLS-1$

    /**
     * Specifies if the default cascade should be ignored and the other cascade type propertied
     * should be used instead. The default for compositions is ALL for all other types the cascade
     * type will not be generated.
     */
    String PROPERTY_CASCADE_TYPE_OVERWRITE_DEFAULT = "cascadeTypeOverwriteDefault"; //$NON-NLS-1$

    /**
     * Specifies if the cascade type PERSIST should be added.
     */
    String PROPERTY_CASCADE_TYPE_PERSIST = "cascadeTypePersist"; //$NON-NLS-1$

    /**
     * Specifies if the cascade type MERGE should be added.
     */
    String PROPERTY_CASCADE_TYPE_MERGE = "cascadeTypeMerge"; //$NON-NLS-1$

    /**
     * Specifies if the cascade type REMOVE should be added.
     */
    String PROPERTY_CASCADE_TYPE_REMOVE = "cascadeTypeRemove"; //$NON-NLS-1$

    /**
     * Specifies if the cascade type REFRESH should be added.
     */
    String PROPERTY_CASCADE_TYPE_REFRESH = "cascadeTypeRefresh"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "PERSISTENCEASSOCIATION-"; //$NON-NLS-1$

    /**
     * Validation message code for empty join table name.
     */
    String MSGCODE_JOIN_TABLE_NAME_EMPTY = MSGCODE_PREFIX + "JoinTableNameEmpty"; //$NON-NLS-1$

    /**
     * Validation message code for invalid join table name.
     */
    String MSGCODE_JOIN_TABLE_NAME_INVALID = MSGCODE_PREFIX + "JoinTableNameInvalid"; //$NON-NLS-1$

    /**
     * Validation message code for empty source column name.
     */
    String MSGCODE_SOURCE_COLUMN_NAME_EMPTY = MSGCODE_PREFIX + "SourceColumnNameEmpty"; //$NON-NLS-1$

    /**
     * Validation message code for empty target column name.
     */
    String MSGCODE_TARGET_COLUMN_NAME_EMPTY = MSGCODE_PREFIX + "TargteColumnNameEmpty"; //$NON-NLS-1$

    /**
     * Validation message code for invalid source column name.
     */
    String MSGCODE_SOURCE_COLUMN_NAME_INVALID = MSGCODE_PREFIX + "SourceColumnNameInvalid"; //$NON-NLS-1$

    /**
     * Validation message code for invalid target column name.
     */
    String MSGCODE_TARGET_COLUMN_NAME_INVALID = MSGCODE_PREFIX + "TargetColumnNameInvalid"; //$NON-NLS-1$

    /**
     * Validation message code for empty or not empty join column name.
     */
    String MSGCODE_JOIN_COLUMN_NAME_EMPTY = MSGCODE_PREFIX + "JoinColumnNameEmpty"; //$NON-NLS-1$

    /**
     * Validation message code for not valid join column name.
     */
    String MSGCODE_JOIN_COLUMN_NAME_INVALID = MSGCODE_PREFIX + "JoinColumnNameInvalid"; //$NON-NLS-1$

    /**
     * Validation message code indicate that one side is not and one side is marked as transient.
     */
    String MSGCODE_TRANSIENT_MISMATCH = MSGCODE_PREFIX + "TransientMismatch"; //$NON-NLS-1$

    /**
     * Validation message code indicates that the owning side of the relationship is missing or
     * marked on both sides.
     */
    String MSGCODE_OWNER_OF_ASSOCIATION_MISMATCH = MSGCODE_PREFIX + "OwnerOfAssociationMismatch"; //$NON-NLS-1$

    /**
     * Validation message code indicates that the owning side must not be given.
     */
    String MSGCODE_OWNER_OF_ASSOCIATION_MUST_NOT_GIVEN = MSGCODE_PREFIX
            + "OwnerOfAssociationMustNotGiven"; //$NON-NLS-1$

    /**
     * Validation message code indicates that the lazy fetching is not allowed for single valued
     * associations (to-one association).
     */
    String MSGCODE_LAZY_FETCH_FOR_SINGLE_VALUED_ASSOCIATIONS_NOT_ALLOWED = MSGCODE_PREFIX
            + "LazyFetchForSingleValuedAssociationsAllowed"; //$NON-NLS-1$

    /**
     * Validation message code indicates that due to a problem in jmerge the java code must be fixed
     * manually.
     */
    String MSGCODE_MANUALLY_CODE_FIX_NECESSARY = MSGCODE_PREFIX + "ManuallyCodeFixNecessary"; //$NON-NLS-1$

    /**
     * Validation message code indicates that a child to parent composition has a cascade type
     * enabled, which is not allowed.
     */
    String MSGCODE_CHILD_TO_PARENT_CASCADE_TYPE = MSGCODE_PREFIX + "ChildToParentCascadeType"; //$NON-NLS-1$

    /**
     * Returns the {@link IPolicyCmptTypeAssociation} this info object belongs to.
     */
    IPolicyCmptTypeAssociation getPolicyComponentTypeAssociation();

    /**
     * Returns the join table name for this association required for n-to-m relationships. Returns
     * an empty String if it has not been set yet.
     */
    String getJoinTableName();

    /**
     * Sets the join table name for this association.
     * <p>
     * Note that the final column name in the database can differ from the given
     * <code>newJoinTableName</code> by means of an ITableColumnNamingStrategy which is set on a per
     * IpsProject basis.
     * 
     * @see ITableColumnNamingStrategy
     * 
     * @throws NullPointerException if <code>newJoinTableName</code> is <code>null</code>.
     */
    void setJoinTableName(String newJoinTableName);

    /**
     * Returns the source column name for this association required for n-to-m relationships.
     * Returns an empty String if it has not been set yet.
     */
    String getSourceColumnName();

    /**
     * Sets the source column name for this association which is required for n-to-m relationships.
     * <p>
     * Note that the final column name in the database can differ from the given
     * <code>newSourceColumnName</code> by means of an ITableColumnNamingStrategy which is set on a
     * per IpsProject basis.
     * 
     * @throws NullPointerException if <code>newSourceColumnName</code> is <code>null</code>.
     */
    void setSourceColumnName(String newSourceColumnName);

    /**
     * Returns the target column name for this association required for n-to-m relationships.
     * Returns an empty String if it has not been set yet.
     */
    String getTargetColumnName();

    /**
     * Sets the target column name for this association which is required for n-to-m relationships.
     * <p>
     * Note that the final column name in the database can differ from the given
     * <code>newTargetColumnName</code> by means of an ITableColumnNamingStrategy which is set on a
     * per IpsProject basis.
     * 
     * @throws NullPointerException if <code>newTargetColumnName</code> is <code>null</code>.
     */
    void setTargetColumnName(String newTargetColumnName);

    /**
     * Returns the join column name for this association required for one-to-many relationships.
     * Returns an empty String if it has not been set yet.
     */
    String getJoinColumnName();

    /**
     * Sets the join column name for this association which is required for one-to-many
     * relationships.
     * 
     * @throws NullPointerException if <code>joinColumnName</code> is <code>null</code>.
     */
    void setJoinColumnName(String joinColumnName);

    /**
     * Returns the fetch type for this association.
     * 
     * @see FetchType
     */
    FetchType getFetchType();

    /**
     * Sets the fetch type for this association.
     * 
     * @see FetchType
     */
    void setFetchType(FetchType fetchType);

    /**
     * @return <code>true</code> if the corresponding association does not have an inverse relation.
     */
    boolean isUnidirectional();

    /**
     * @return <code>true</code> if the corresponding association also has an inverse relation.
     */
    boolean isBidirectional();

    /**
     * @return <code>true</code> when the cascading attribute is required for the corresponding
     *             association, <code>false</code> otherwise.
     */
    boolean isCascading();

    /**
     * @return <code>true</code> when the orphan removal annotation is required (default) for the
     *             corresponding association, <code>false</code> otherwise. The orphan removal
     *             annotation is required in case of master to detail compositions.
     */
    boolean isOrphanRemovalRequired();

    /**
     * @return <code>true</code> for n-to-m relationships where a join table is mandatory,
     *             <code>false</code> otherwise.
     * 
     * @throws IpsException if errors occur during retrieval of the corresponding inverse relation.
     */
    boolean isJoinTableRequired() throws IpsException;

    /**
     * @return <code>true</code> if the foreign key column is defined on the target side.
     *             <code>false</code> otherwise.
     * 
     * @throws IpsException if errors occur during retrieval of the corresponding inverse relation.
     */
    boolean isForeignKeyColumnDefinedOnTargetSide() throws IpsException;

    /**
     * Set to <code>true</code> if the association should be the owner of am many-to-many
     * association. set to <code>false</code> if the target side is the owner.
     */
    void setOwnerOfManyToManyAssociation(boolean ownerOfManyToManyAssociation);

    /**
     * Returns true if the association is the owner of am many-to-many association.
     */
    boolean isOwnerOfManyToManyAssociation();

    /**
     * Set to <code>true</code> if the association should be annotated as orphan removal. Note that
     * this property make only sense on the master to detail side of an one to many composition.
     */
    void setOrphanRemoval(boolean orphanRemoval);

    /**
     * Returns <code>true</code> if the orphan removal feature is used otherwise <code>false</code>.
     */
    boolean isOrphanRemoval();

    /**
     * Returns <code>true</code> if the default cascade type should be ignored and the other cascade
     * type properties should be used instead.
     */
    boolean isCascadeTypeOverwriteDefault();

    /**
     * Set to <code>true</code> if the default cascade type should be ignored and the other cascade
     * type properties should be used instead.
     */
    void setCascadeTypeOverwriteDefault(boolean cascadeTypeOverwriteDefault) throws IpsException;

    /**
     * Returns <code>true</code> if the corresponding cascade type should be used.
     */
    boolean isCascadeTypeRefresh();

    /**
     * Set to <code>true</code> if the corresponding cascade type should be used.
     */
    void setCascadeTypeRefresh(boolean cascadeTypeRefresh);

    /**
     * Returns <code>true</code> if the corresponding cascade type should be used.
     */
    boolean isCascadeTypeRemove();

    /**
     * Set to <code>true</code> if the corresponding cascade type should be used.
     */
    void setCascadeTypeRemove(boolean cascadeTypeRemove);

    /**
     * Returns <code>true</code> if the corresponding cascade type should be used.
     */
    boolean isCascadeTypeMerge();

    /**
     * Set to <code>true</code> if the corresponding cascade type should be used.
     */
    void setCascadeTypeMerge(boolean cascadeTypeMerge);

    /**
     * Returns <code>true</code> if the corresponding cascade type should be used.
     */
    boolean isCascadeTypePersist();

    /**
     * Set to <code>true</code> if the corresponding cascade type should be used.
     */
    void setCascadeTypePersist(boolean cascadeTypePersist);

    /**
     * Initialize the default properties
     */
    void initDefaults() throws IpsException;

    /**
     * Evaluates the relationship type for bidirectional associations. If the association is
     * unidirectional then the RelationshipType.UNKNOWN is returned.
     */
    RelationshipType evalBidirectionalRelationShipType(IPolicyCmptTypeAssociation inverseAssociation);

    /**
     * Evaluates the relationship type for unidirectional associations.
     */
    RelationshipType evalUnidirectionalRelationShipType();

    /**
     * Determines whether to use cascading load on the database (also fetching dependent objects of
     * the object to load instead of using deferred loading proxies).
     */
    public enum FetchType {
        LAZY,
        EAGER;
    }

    /**
     * Determines the join fetch type in case of eager fetch type.
     */
    public enum JoinFetchType {
        INNER,
        OUTER;
    }

    /**
     * Initialize the default cascade type properties
     */
    void initDefaultsCascadeTypes() throws IpsException;

    /**
     * Returns <code>true</code> if the join column / foreign key column is nullable.
     */
    boolean isJoinColumnNullable();

    /**
     * Sets the nullable property for the join column / foreign key column.
     */
    void setJoinColumnNullable(boolean nullable);
}
