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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * A class that holds information of a policy component type attribute which is relevant for
 * persistence using the JPA (Java Persistence API).
 * <p/>
 * This information can be used to act as a hint to the code generator on how to realize the table
 * column(s) on the database side.
 * 
 * @author Roman Grutza
 */
public interface IPersistentAttributeInfo extends IIpsObjectPart {

    public static final int MAX_TABLE_COLUMN_SCALE = 255;
    public static final int MIN_TABLE_COLUMN_SCALE = 1;
    public static final int MAX_TABLE_COLUMN_PRECISION = 255;
    public static final int MIN_TABLE_COLUMN_PRECISION = 1;
    public static final int MAX_TABLE_COLUMN_SIZE = 255;
    public static final int MIN_TABLE_COLUMN_SIZE = 1;

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "PersistenceAttribute"; //$NON-NLS-1$

    /**
     * The name of the column name property.
     */
    public final static String PROPERTY_TABLE_COLUMN_NAME = "tableColumnName";

    /**
     * The name of the column size property.
     */
    public final static String PROPERTY_TABLE_COLUMN_SIZE = "tableColumnSize";

    /**
     * The name of the "is unique column" property (in the sense that any two tuples cannot have the
     * same value in this column).
     */
    public final static String PROPERTY_TABLE_COLUMN_UNIQE = "tableColumnUnique";

    /**
     * The name of the "column is nullable" property, allowing NULL values in the database.
     */
    public final static String PROPERTY_TABLE_COLUMN_NULLABLE = "tableColumnNullable";

    /**
     * The name of the column scale property.
     */
    public final static String PROPERTY_TABLE_COLUMN_SCALE = "tableColumnScale";

    /**
     * The name of the column precision property.
     */
    public final static String PROPERTY_TABLE_COLUMN_PRECISION = "tableColumnPrecision";

    /**
     * The name of the column converter property.
     */
    public final static String PROPERTY_TABLE_COLUMN_CONVERTER = "tableColumnConverter";

    /**
     * Returns the {@link IPolicyCmptTypeAttribute} this info object belongs to.
     */
    public IPolicyCmptTypeAttribute getPolicyComponentTypeAttribute();

    /**
     * Returns the column name for this attribute. Returns an empty String if it has not been set
     * yet.
     */
    public String getTableColumnName();

    /**
     * Sets the table column name for this attribute.
     */
    public void setTableColumnName(String newTableColumnName);

    /**
     * Returns the column size.
     */
    public int getTableColumnSize();

    /**
     * Sets the table column size, which must be in the range
     * [MIN_TABLE_COLUMN_SIZE..MAX_TABLE_COLUMN_SIZE].
     */
    public void setTableColumnSize(int newTableColumnSize);

    /**
     * Returns if this column is unique (no duplicate values allowed for any two rows).
     */
    public boolean getTableColumnUnique();

    /**
     * Sets the unique property for the table corresponding to this column.
     */
    public void setTableColumnUnique(boolean unique);

    /**
     * Return if this column is nullable.
     */
    public boolean getTableColumnNullable();

    /**
     * Sets the nulllable property for the table corresponding to this column.
     */
    public void setTableColumnNullable(boolean nullable);

    /**
     * Returns the column scale.
     */
    public int getTableColumnScale();

    /**
     * Sets the table column size, which must be in the range
     * [MIN_TABLE_COLUMN_SIZE..MAX_TABLE_COLUMN_SIZE].
     */
    public void setTableColumnScale(int scale);

    /**
     * Returns the column precision.
     */
    public int getTableColumnPrecision();

    /**
     * Sets the table column precision, which must be in the range
     * [MIN_TABLE_COLUMN_PRECISION..MAX_TABLE_COLUMN_PRECISION].
     */
    public void setTableColumnPrecision(int precision);

    /**
     * Returns a converter for this column.
     */
    public IPersistableTypeConverter getTableColumnConverter();

    /**
     * Sets a converter for this column;
     */
    public void setTableColumnConverter(IPersistableTypeConverter newConverter);
}
