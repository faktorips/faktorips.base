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

import org.faktorips.devtools.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;

/**
 * A class that holds information of a policy component type attribute which is relevant for
 * persistence using the JPA (Java Persistence API).
 * <p>
 * This information is used as a hint to the code generator on how to realize the table column(s) on
 * the database side.
 * 
 * @author Roman Grutza
 */
public interface IPersistentAttributeInfo extends IPersistentTypePartInfo {

    /** The XML tag for this IPS object part. */
    String XML_TAG = "PersistenceAttribute"; //$NON-NLS-1$

    /**
     * The name of the column name property.
     */
    String PROPERTY_TABLE_COLUMN_NAME = "tableColumnName"; //$NON-NLS-1$

    /**
     * The name of the column size property.
     */
    String PROPERTY_TABLE_COLUMN_SIZE = "tableColumnSize"; //$NON-NLS-1$

    /**
     * The name of the "is unique column" property (in the sense that any two tuples cannot have the
     * same value in this column).
     */
    String PROPERTY_TABLE_COLUMN_UNIQE = "tableColumnUnique"; //$NON-NLS-1$

    /**
     * The name of the "column is nullable" property, allowing NULL values in the database.
     */
    String PROPERTY_TABLE_COLUMN_NULLABLE = "tableColumnNullable"; //$NON-NLS-1$

    /**
     * The name of the column scale property.
     */
    String PROPERTY_TABLE_COLUMN_SCALE = "tableColumnScale"; //$NON-NLS-1$

    /**
     * The name of the column precision property.
     */
    String PROPERTY_TABLE_COLUMN_PRECISION = "tableColumnPrecision"; //$NON-NLS-1$

    /**
     * The name of the column converter property.
     */
    String PROPERTY_TABLE_COLUMN_CONVERTER = "tableColumnConverter"; //$NON-NLS-1$

    /**
     * The name of the property which maps this attribute to an temporal type (date, time,
     * timestamp) if applicable.
     */
    String PROPERTY_TEMPORAL_MAPPING = "temporalMapping"; //$NON-NLS-1$

    /**
     * The name of a property that indicates that the attribute is transient.
     */
    String PROPERTY_TRANSIENT = "transient"; //$NON-NLS-1$

    /**
     * The name of a property that indicates the qualified class name of the converter if used.
     */
    String PROPERTY_CONVERTER_QUALIFIED_CLASS_NAME = "converterQualifiedClassName"; //$NON-NLS-1$

    /**
     * The name of a property that indicates the SQL column definition.
     */
    String PROPERTY_SQL_COLUMN_DEFINITION = "sqlColumnDefinition"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "PERSISTENCEATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence attribute info has invalid column
     * size/precision/scale values set.
     */
    String MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS = MSGCODE_PREFIX
            + "PersistenceAttrColumnOutOfBounds"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that this persistence attribute info has invalid column
     * name set.
     */
    String MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME = MSGCODE_PREFIX
            + "PersistenceAttrColumnNameEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the column name exceeds the max column size
     */
    String MSGCODE_COLUMN_NAME_EXCEEDS_MAX_LENGTH = MSGCODE_PREFIX + "ColumnNameExceedsMaxLength"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the column name must be empty if the attribute is
     * derived.
     */
    String MSGCODE_PERSISTENCEATTR_COLNAME_MUST_BE_EMPTY = MSGCODE_PREFIX
            + "PersistenceattrColnameMustBeEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the column name must not contain whitespace.
     */
    String MSGCODE_PERSISTENCEATTR_COLNAME_MUST_NOT_CONTAIN_WHITESPACE_CHARACTERS = MSGCODE_PREFIX
            + "PersistenceattrColnameMustNotContainWhitespaceCharacters"; //$NON-NLS-1$

    /**
     * Validation message code to warn about a String-Attribute with persistence limits that aren't
     * represented in the model
     */
    String MSGCODE_PERSISTENCEATTR_MODEL_CONTAINS_NO_LENGTH_RESTRICTION = MSGCODE_PREFIX
            + "PersistenceattrModelContainsNoLengthRestriction"; //$NON-NLS-1$

    /**
     * Validation message code to warn about a String-Attribute's model configuration that exceeds
     * the persistence size limit
     */
    String MSGCODE_PERSISTENCEATTR_MODEL_EXCEEDS_COLUMN_SIZE = MSGCODE_PREFIX
            + "PersistenceAttrModelExceedsColumnSize"; //$NON-NLS-1$

    /**
     * Validation message code to warn about null-values being allowed in the StringLengthValueSet
     * model, but not in the persistence settings
     */
    String MSGCODE_PERSISTENCEATTR_COLUMN_NULLABLE_DOES_NOT_MATCH_MODEL = MSGCODE_PREFIX
            + "PersistenceAttrColumnNullableDoesNotMatchModel"; //$NON-NLS-1$

    /**
     * Returns the {@link IPolicyCmptTypeAttribute} this info object belongs to.
     */
    IPolicyCmptTypeAttribute getPolicyComponentTypeAttribute();

    /**
     * Returns the column name for this attribute. Returns an empty String if it has not been set
     * yet.
     */
    String getTableColumnName();

    /**
     * Sets the table column name for this attribute.
     * <p>
     * Note that the final column name in the database can differ from the given
     * <code>newTableColumnName</code> by means of an ITableColumnNamingStrategy which is set on a
     * per IpsProject basis.
     * 
     * @see ITableColumnNamingStrategy
     */
    void setTableColumnName(String newTableColumnName);

    /**
     * Returns the column size.
     */
    int getTableColumnSize();

    /**
     * Sets the table column size, which must be in the range
     * [MIN_TABLE_COLUMN_SIZE..MAX_TABLE_COLUMN_SIZE].
     */
    void setTableColumnSize(int newTableColumnSize);

    /**
     * Returns if this column is unique (no duplicate values allowed for any two rows).
     */
    boolean getTableColumnUnique();

    /**
     * Sets the unique property for the table corresponding to this column.
     */
    void setTableColumnUnique(boolean unique);

    /**
     * Return if this column is nullable.
     */
    boolean getTableColumnNullable();

    /**
     * Sets the nullable property for the table corresponding to this column.
     */
    void setTableColumnNullable(boolean nullable);

    /**
     * Returns the column scale.
     */
    int getTableColumnScale();

    /**
     * Sets the table column size, which must be in the range
     * [MIN_TABLE_COLUMN_SIZE..MAX_TABLE_COLUMN_SIZE].
     */
    void setTableColumnScale(int scale);

    /**
     * Returns the column precision.
     */
    int getTableColumnPrecision();

    /**
     * Sets the table column precision, which must be in the range
     * [MIN_TABLE_COLUMN_PRECISION..MAX_TABLE_COLUMN_PRECISION].
     */
    void setTableColumnPrecision(int precision);

    /**
     * Sets a converter for this column;
     */
    void setTableColumnConverter(IPersistableTypeConverter newConverter);

    /**
     * Returns the SQL column definition.
     */
    String getSqlColumnDefinition();

    /**
     * Sets the SQL column definition. If the column should be created with more specialized
     * options, the columnDefinition contains the String SQL fragment that the JPA provider uses
     * when generating the DDL for the column.
     */
    void setSqlColumnDefinition(String sqlColumnDefinition);

    /**
     * Returns the qualified class name of the converter for this column.
     */
    String getConverterQualifiedClassName();

    /**
     * Sets the qualified class name of the converter for this column.
     */
    void setConverterQualifiedClassName(String converterQualifiedClassName);

    /**
     * Returns true if the underlying attribute is persisted. Constants and fields which are
     * computed on the fly need not be persisted and <code>false</code> is returned for these types
     * of attributes.
     * 
     * @see AttributeType
     */
    boolean isPersistentAttribute();

    /**
     * If this attribute corresponds to a temporal type then this method returns the temporal
     * information (time, date or both) that should be taken into account when dealing with this
     * attribute.
     * 
     * @see DateTimeMapping
     * 
     * @return the temporal mapping if any, <code>null</code> if this attribute is not of temporal
     *             type.
     */
    DateTimeMapping getTemporalMapping();

    /**
     * Sets the temporal mapping on this attribute, that is whether the date, the time or both date
     * and time information will be considered when dealing with this attribute.
     */
    void setTemporalMapping(DateTimeMapping temporalType);

    /**
     * Tags a temporal attribute for date only, time only or time-stamp (date and time) usage.
     */
    public enum DateTimeMapping {
        DATE_ONLY,
        TIME_ONLY,
        DATE_AND_TIME;

        public String toJpaTemporalType() {
            switch (this) {
                case DATE_ONLY:
                    return "DATE"; //$NON-NLS-1$
                case TIME_ONLY:
                    return "TIME"; //$NON-NLS-1$
                case DATE_AND_TIME:
                    return "TIMESTAMP"; //$NON-NLS-1$
                default:
                    throw new RuntimeException("Error converting IPS Temporal Datatype to JPA Temporal Type."); //$NON-NLS-1$
            }
        }
    }

}
