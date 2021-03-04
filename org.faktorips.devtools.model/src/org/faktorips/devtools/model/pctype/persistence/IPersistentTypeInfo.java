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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.ITableNamingStrategy;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;

/**
 * A class that holds information of a policy component type which is relevant for persistence.
 * <p>
 * This information is used as a hint to the code generator on how to realize the table(s) on the
 * database side.
 * 
 * @author Roman Grutza
 */
public interface IPersistentTypeInfo extends IIpsObjectPart, IDescribedElement {

    /** The XML tag for this IPS object part. */
    public static final String XML_TAG = "Persistence"; //$NON-NLS-1$

    /**
     * The name of the (primary) table name property.
     */
    public static final String PROPERTY_TABLE_NAME = "tableName"; //$NON-NLS-1$

    /**
     * The name of the inheritance strategy property.
     */
    public static final String PROPERTY_INHERITANCE_STRATEGY = "inheritanceStrategy"; //$NON-NLS-1$

    /**
     * The name of the discriminator column name property.
     */
    public static final String PROPERTY_DISCRIMINATOR_COLUMN_NAME = "discriminatorColumnName"; //$NON-NLS-1$

    /**
     * The name of the discriminator value property.
     */
    public static final String PROPERTY_DISCRIMINATOR_VALUE = "discriminatorValue"; //$NON-NLS-1$

    /**
     * The name of the discriminator data type property.
     */
    public static final String PROPERTY_DISCRIMINATOR_DATATYPE = "discriminatorDatatype"; //$NON-NLS-1$

    /**
     * The name of a property that indicates that the joined subclass inheritance strategy is not
     * used.
     */
    public static final String PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS = "notJoinedSubclass"; //$NON-NLS-1$

    /**
     * The name of a property that indicates the persistent type: Entity, MappedSuperclass or no
     * persistent enabled. See enumeration this#PersistentType.
     */
    public static final String PROPERTY_PERSISTENT_TYPE = "persistentType"; //$NON-NLS-1$

    /**
     * The name of a property that indicates that the discriminator column name and data type is
     * defined in this type.
     */
    public static final String PROPERTY_DEFINES_DISCRIMINATOR_COLUMN = "definesDiscriminatorColumn"; //$NON-NLS-1$

    /**
     * The name of a property that indicates that the table defined in the super type will be used.
     */
    public static final String PROPERTY_USE_TABLE_DEFINED_IN_SUPERTYPE = "useTableDefinedInSupertype"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "PERSISTENCETYPE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type has an invalid table name
     * set.
     */
    public static final String MSGCODE_PERSISTENCE_TABLE_NAME_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeTableNameInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type has an invalid
     * discriminator set.
     */
    public static final String MSGCODE_PERSISTENCE_DISCRIMINATOR_VALUE_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeDiscriminatorInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type has an invalid
     * inheritance strategy set.
     */
    public static final String MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeInheritanceStrategyInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the discriminator definition is missing on the base
     * entity.
     */
    public static final String MSGCODE_DEFINITION_OF_DISCRIMINATOR_MISSING = MSGCODE_PREFIX
            + "definitionOfDiscriminatorMissing"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the discriminator definition is not allowed here.
     * Only on the root entity.
     */
    public static final String MSGCODE_DEFINITION_OF_DISCRIMINATOR_NOT_ALLOWED = MSGCODE_PREFIX
            + "definitionOfDiscriminatorNotAllowed"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the table definition defined in the super type is
     * not allowed / possible.
     */
    public static final String MSGCODE_USE_TABLE_DEFINED_IN_SUPERTYPE_NOT_ALLOWED = MSGCODE_PREFIX
            + "msgcodeUseTableDefinedInSupertypeNotAllowed"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that persistence attribute info or persistence
     * association info has column names which is already defined. The conflicting attribute can be
     * in the same type this attribute info belongs to or in a super type if SINGLE_TABLE
     * inheritance is used.
     */
    public static final String MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME = MSGCODE_PREFIX
            + "PersistenceAttrColumnNameDuplicate"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that in case of single table inheritance strategy the
     * sub-entities must use the table name of the root entity.
     */
    public static final String MSGCODE_MUST_USE_TABLE_FROM_ROOT_ENTITY = MSGCODE_PREFIX + "MustUseTableFromRootEntity"; //$NON-NLS-1$

    /**
     * Returns the policy component type this persistent type info belongs to.
     */
    public IPolicyCmptType getPolicyCmptType();

    /**
     * Returns the persistence type: entity, mapped superclass, or none if the persistent is
     * disabled.
     */
    public PersistentType getPersistentType();

    /**
     * Returns <code>true</code> if the persistent is enabled. In this case the persistent type is
     * equal to entity or mapped superclass. If the persistent type is none then return
     * <code>false</code>.
     */
    public boolean isEnabled();

    /**
     * Sets the persistent type to enable/disable the persistent or annotate the appropriate policy
     * component type as mapped superclass.
     */
    public void setPersistentType(PersistentType persistentType);

    /**
     * Returns <code>true</code> if this persistent type defines the discriminator column. Not that
     * the discriminator column can only be defined at the root entity. If the current persistent
     * type defines the discriminator column then the column name and data type must given in this
     * type. The column name and data type of the discriminator can't be different or overwritten in
     * one of the subclasses.
     */
    public boolean isDefinesDiscriminatorColumn();

    /**
     * Set to <code>true</code> if this type defines the discriminator column name and data type.
     * Note that the discriminator column name and data type can only be specified in the base
     * entity.
     */
    public void setDefinesDiscriminatorColumn(boolean definesDiscriminatorColumn);

    /**
     * Return <code>true</code> if the table defined in the super type will be used. Or
     * <code>false</code> if this type defines the table name, in which the type will be persist.
     * Note that if the directly associate super type also used the table definition of its super
     * type then this super types table definition will be used and so on, thus the table definition
     * can be defined in one of the super types in the inheritance hierarchy.
     */
    public boolean isUseTableDefinedInSupertype();

    /**
     * Set to <code>true</code> if the table definition of the super type will be used.
     */
    public void setUseTableDefinedInSupertype(boolean useTableDefinedInSupertype);

    /**
     * Search the root entity of this persistent type.
     * 
     * @see #isDefinesDiscriminatorColumn
     */
    public IPolicyCmptType findRootEntity() throws CoreException;

    /**
     * Returns the name of database table. Returns an empty string if the table name has not been
     * set yet.
     */
    public String getTableName();

    /**
     * Sets the database table name to use for the {@link IPolicyCmptType} this object is part of.
     * <p>
     * The table name be must unique for each entity in a {@link IPolicyCmptType} inheritance
     * hierarchy if the JOINED_SUBCLASS inheritance strategy is used. In contrast when using MIXED
     * or SINGLE_TABLE inheritance strategies each part of the hierarchy must use the same primary
     * table name.
     * <p>
     * Note that the final table name in the database can differ from the given
     * <code>newTableName</code> by means of an ITableNamingStrategy which is set on a per
     * IpsProject basis.
     * 
     * @param newTableName The name of the table, must not be <code>null</code>.
     * 
     * @see InheritanceStrategy
     * @see ITableNamingStrategy
     */
    public void setTableName(String newTableName);

    /**
     * Returns the inheritance strategy to use for the {@link IPolicyCmptType} this object is part
     * of.
     */
    public InheritanceStrategy getInheritanceStrategy();

    /**
     * Sets the inheritance strategy to use for the {@link IPolicyCmptType} this object is part of.
     * 
     * @param newStrategy The inheritance strategy to use, must not be <code>null</code>.
     */
    public void setInheritanceStrategy(InheritanceStrategy newStrategy);

    /**
     * Returns the discriminator column name. Returns an empty string if the discriminator column
     * name has not been set yet.
     */
    public String getDiscriminatorColumnName();

    /**
     * Sets the discriminator column name. This only makes sense if the SINGLE_TABLE inheritance
     * strategy is used.
     * 
     * @param newDiscriminatorColumnName The name of the discriminator column, must not be
     *            <code>null</code>.
     * 
     * @see InheritanceStrategy
     */
    public void setDiscriminatorColumnName(String newDiscriminatorColumnName);

    /**
     * Returns the discriminator value. When using the SINGLE_TABLE inheritance strategy the
     * concrete policy component type of a tuple (row) in the database table is determined by this
     * value.
     * 
     * @see InheritanceStrategy
     */
    public String getDiscriminatorValue();

    /**
     * Sets the discriminator value of the {@link IPolicyCmptType} this object is part of. This only
     * makes sense in conjunction with SINGLE_TABLE inheritance strategy where the concrete type of
     * an entity is determined by storing this value in a the discriminator column of the table.
     * 
     * @param newDiscriminatorValue The value of the discriminator which has to match the currently
     *            set discriminator data type. Must not be <code>null</code>.
     */
    public void setDiscriminatorValue(String newDiscriminatorValue);

    /**
     * Returns the discriminator data type. Discriminator data types / values / columns are used
     * only in conjunction with the SINGLE_TABLE inheritance strategy.
     * 
     * @see DiscriminatorDatatype
     * @see InheritanceStrategy
     */
    public DiscriminatorDatatype getDiscriminatorDatatype();

    /**
     * Sets the discriminator data type. Discriminator data types / values / columns are used only
     * in conjunction with the SINGLE_TABLE inheritance strategy.
     * 
     * @param newDiscriminatorDatatype A discriminator data type, must not be <code>null</code>.
     * 
     * @see DiscriminatorDatatype
     * @see InheritanceStrategy
     */
    public void setDiscriminatorDatatype(DiscriminatorDatatype newDiscriminatorDatatype);

    /**
     * An inheritance strategy for mapping a class hierarchy to database tables as defined in the
     * JPA 2 standard.
     * <p>
     * Note:
     * <ul>
     * <li>The strategy TABLE_PER_CONCRETE_CLASS is not supported as it is an optional part of the
     * standard.</li>
     * <li>A new strategy named MIXED is introduced to map a subset of a class hierarchy to a
     * dedicated table (meaning that there must also exist a super entity using the strategy
     * SINGLE_TABLE)</li>
     * </ul>
     * The use of the strategy MIXED enables support for a dedicated (secondary) table which can be
     * used in practice to map a line of business to its own table.
     */
    public enum InheritanceStrategy {
        SINGLE_TABLE,
        JOINED_SUBCLASS
    }

    /**
     * Constrains the possible discriminator value.
     */
    public enum DiscriminatorDatatype {

        /**
         * Use this in conjunction with inheritance strategies JOINED_SUBCLASS and MIXED.
         */
        VOID,
        STRING,
        CHAR,
        INTEGER;

        /**
         * @return <code>true</code> if the given value can be parsed into an instance of this
         *         {@link DiscriminatorDatatype}, <code>false</code> otherwise.
         */
        public boolean isParsableToDiscriminatorDatatype(String value) {
            switch (this) {
                case STRING:
                    return (!StringUtils.isEmpty(value));
                case CHAR:
                    return (!StringUtils.isEmpty(value) && value.length() == 1);
                case INTEGER:
                    return (!StringUtils.isEmpty(value)) && StringUtils.isNumeric(value);

                default:
                    return false;
            }
        }
    }

    /**
     * Defines the possible persistent types. If the persistent type is 'none' then persistent is
     * disabled. The type mapped superclass specifies a special behavior that all subclasses of this
     * type inherits the attributes (and also the column definitions) of their supertype.
     */
    public static enum PersistentType {
        NONE,
        ENTITY,
        MAPPED_SUPERCLASS
    }

}
