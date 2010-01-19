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

/**
 * A class that holds information of a policy component type which is relevant for persistence.
 * <p/>
 * This information can be used to act as a hint to the code generator on how to realize the
 * table(s) on the database side.
 * 
 * @author Roman Grutza
 */
public interface IPersistentTypeInfo extends IIpsObjectPart {

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "Persistence"; //$NON-NLS-1$

    public final static String PROPERTY_TABLE_NAME = "tableName";

    public final static String PROPERTY_SECONDARY_TABLE_NAME = "secondaryTableName";

    public final static String PROPERTY_INHERITANCE_STRATEGY = "inheritanceStrategy";

    public final static String PROPERTY_DESCRIMINATOR_COLUMN_NAME = "descriminatorColumnName";

    public final static String PROPERTY_DESCRIMINATOR_VALUE = "descriminatorValue";

    public final static String PROPERTY_DESCRIMINATOR_DATATYPE = "descriminatorDatatype";

    public final static String PROPERTY_INHERITANCE_NOT_JOINEDSUBCLASS = "notJoinedSubclass";

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
     * descriminator column name set.
     */
    public final static String MSGCODE_PERSISTENCE_DESCRIMINATOR_COLUMN_NAME_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeDescriminatorColumnNameInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type cannot be parsed to
     * currently set descriminator datatype.
     */
    public final static String MSGCODE_PERSISTENCE_DESCRIMINATOR_VALUE_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeDescriminatorValueInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence info type cannot be parsed to
     * currently set descriminator datatype.
     */
    public final static String MSGCODE_PERSISTENCE_INHERITANCE_STRATEGY_INVALID = MSGCODE_PREFIX
            + "PersistenceTypeInheritanceStrategyInvalid"; //$NON-NLS-1$

    public String getTableName();

    public void setTableName(String newTableName);

    public String getSecondaryTableName();

    public void setSecondaryTableName(String newSecondaryTableName);

    public InheritanceStrategy getInheritanceStrategy();

    public void setInheritanceStrategy(InheritanceStrategy newStrategy);

    public String getDescriminatorColumnName();

    public void setDescriminatorColumnName(String newDescriminatorColumnName);

    public String getDescriminatorValue();

    public void setDescriminatorValue(String newDescriminatorValue);

    public DescriminatorDatatype getDescriminatorDatatype();

    public void setDescriminatorDatatype(DescriminatorDatatype newDescriminatorDatatype);

    /**
     * For now only the strategies SINGLE_TABLE and JOINED_SUBCLASS are supported.
     */
    public enum InheritanceStrategy {
        SINGLE_TABLE,
        JOINED_SUBCLASS,
        MIXED;
    }

    /**
     * Constrains the possible Descriminator Value.
     */
    public enum DescriminatorDatatype {
        VOID, // no type in case of JOINED_SUBCLASS
        STRING,
        CHAR,
        INTEGER;

        public boolean isParsableToDatatype(String value) {
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
