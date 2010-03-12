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

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.ITableNamingStrategy;

/**
 * A class that holds information of a policy component type which is relevant for persistence.
 * <p/>
 * This information is used as a hint to the code generator on how to realize the table(s) on the
 * database side.
 * 
 * @author Roman Grutza
 */
public interface IPersistentTypeInfo extends IIpsObjectPart {

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "Persistence"; //$NON-NLS-1$

    /**
     * The name of the (primary) table name property.
     */
    public final static String PROPERTY_TABLE_NAME = "tableName"; //$NON-NLS-1$

    /**
     * The name of the secondary table name property.
     */
    public final static String PROPERTY_SECONDARY_TABLE_NAME = "secondaryTableName"; //$NON-NLS-1$

    /**
     * The name of the inheritance strategy property.
     */
    public final static String PROPERTY_INHERITANCE_STRATEGY = "inheritanceStrategy"; //$NON-NLS-1$

    /**
     * The name of the discriminator column name property.
     */
    public final static String PROPERTY_DISCRIMINATOR_COLUMN_NAME = "discriminatorColumnName"; //$NON-NLS-1$

    /**
     * The name of the discriminator value property.
     */
    public final static String PROPERTY_DISCRIMINATOR_VALUE = "discriminatorValue"; //$NON-NLS-1$

    /**
     * The name of the discriminator datatype property.
     */
    public final static String PROPERTY_DISCRIMINATOR_DATATYPE = "discriminatorDatatype"; //$NON-NLS-1$

    /**
     * The name of a property that indicates that the joines sublass inheritance strategy is not
     * used.
     */
    public final static String PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS = "notJoinedSubclass"; //$NON-NLS-1$

    /**
     * The name of a property that indicates that the persistent is enabled for the policy component
     * type
     */
    public final static String PROPERTY_ENABLED = "enabled"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PERSISTENCETYPE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type has an invalid table name
     * set.
     */
    public final static String MSGCODE_PERSISTENCE_TABLE_NAME_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeTableNameInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type has an invalid secondary
     * table name set.
     */
    public final static String MSGCODE_PERSISTENCE_SECONDARY_TABLE_NAME_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeSecondaryTableNameInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type has an invalid
     * discriminator set.
     */
    public final static String MSGCODE_PERSISTENCE_DISCRIMINATOR_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeDiscriminatorInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type has an invalid
     * inheritance strategy set.
     */
    public final static String MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeInheritanceStrategyInvalid"; //$NON-NLS-1$

    /**
     * Return <code>true</code> if the persistence type info in enabled. Returns <code>false</code>
     * if the associate policy component type should not be persist.
     */
    public boolean isEnabled();

    /**
     * Set to <code>true</code> if the associate policy component type should persist. Set to
     * <code>false</code> if the policy component type doesn't need persistent type info.
     */
    public void setEnabled(boolean enabled);

    /**
     * Returns the name of database table. Returns an empty string if the table name has not been
     * set yet.
     */
    public String getTableName();

    /**
     * Sets the database table name to use for the {@link IPolicyCmptType} this object is part of.
     * <p/>
     * The table name be must unique for each entity in a {@link IPolicyCmptType} inheritance
     * hierarchy if the JOINED_SUBCLASS inheritance strategy is used. In contrast when using MIXED
     * or SINGLE_TABLE inheritance strategies each part of the hierarchy must use the same primary
     * table name.
     * <p/>
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
     * Returns the name of the secondary table name. This is only required if the MIXED inheritance
     * strategy is used for the entity this object belongs to.
     * 
     * @return Returns an empty string if the secondary table name has not been set yet.
     * @see {@link InheritanceStrategy}
     */
    public String getSecondaryTableName();

    /**
     * Sets the secondary table name to use for the {@link IPolicyCmptType} this object is part of.
     * <p/>
     * Since there can only exist only one unique secondary table for a {@link IPolicyCmptType}
     * sub-hierarchy one must ensure that the entities making up the sub-hierarchy use the same
     * secondary table name.
     * <p/>
     * Note that the final table name in the database can differ from the given
     * <code>newSecondaryTableName</code> by means of an ITableNamingStrategy which is set on a per
     * IpsProject basis.
     * 
     * @param The name of the secondary table, must not be <code>null</code>.
     * 
     * @see ITableNamingStrategy
     */
    public void setSecondaryTableName(String newSecondaryTableName);

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
     *            set discriminator datatype. Must not be <code>null</code>.
     */
    public void setDiscriminatorValue(String newDiscriminatorValue);

    /**
     * Returns the discriminator datatype. Discriminator datatypes/values/columns are used only in
     * conjunction with the SINGLE_TABLE inheritance strategy.
     * 
     * @see DiscriminatorDatatype
     * @see InheritanceStrategy
     */
    public DiscriminatorDatatype getDiscriminatorDatatype();

    /**
     * Sets the discriminator datatype. Discriminator datatypes/values/columns are used only in
     * conjunction with the SINGLE_TABLE inheritance strategy.
     * 
     * @param newDiscriminatorDatatype A discriminator datatype, must not be <code>null</code>.
     * 
     * @see DiscriminatorDatatype
     * @see InheritanceStrategy
     */
    public void setDiscriminatorDatatype(DiscriminatorDatatype newDiscriminatorDatatype);

    /**
     * An inheritance strategy for mapping a class hierarchy to database tables as defined in the
     * JPA 2 standard.
     * <p/>
     * Note:
     * <ul>
     * <li>The strategy TABLE_PER_CONCRETE_CLASS is not supported as it is an optional part of the
     * standard.</li>
     * <li>A new strategy named MIXED is introduced to map a subset of a class hierarchy to a
     * dedicated table (meaning that there must also exist a superentity using the strategy
     * SINGLE_TABLE)</li>
     * </ul>
     * The use of the strategy MIXED enables support for a dedicated (secondary) table which can be
     * used in practice to map a line of business to its own table.
     */
    public enum InheritanceStrategy {
        SINGLE_TABLE,
        JOINED_SUBCLASS,
        MIXED;
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

}
