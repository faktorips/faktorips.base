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

    /** The XML tag for this IPS object part. */
    public final static String XML_TAG = "PersistenceAttribute"; //$NON-NLS-1$

    public final static String PROPERTY_TABLE_COLUMN_NAME = "tableColumnName";

    public final static String PROPERTY_TABLE_COLUMN_SIZE = "tableColumnSize";

    public final static String PROPERTY_TABLE_COLUMN_UNIQE = "tableColumnUnique";

    public final static String PROPERTY_TABLE_COLUMN_NULLABLE = "tableColumnNullable";

    public final static String PROPERTY_TABLE_COLUMN_SCALE = "tableColumnScale";

    public final static String PROPERTY_TABLE_COLUMN_PRECISION = "tableColumnPrecision";

    public final static String PROPERTY_TABLE_COLUMN_CONVERTER = "tableColumnConverter";

    public IPolicyCmptTypeAttribute getPolicyComponentTypeAttribute();

    public String getTableColumnName();

    public void setTableColumnName(String newTableColumnName);

    public int getTableColumnSize();

    public void setTableColumnSize(int newTableColumnSize);

    public boolean getTableColumnUnique();

    public void setTableColumnUnique(boolean unique);

    public boolean getTableColumnNullable();

    public void setTableColumnNullable(boolean nullable);

    public int getTableColumnScale();

    public void setTableColumnScale(int scale);

    public int getTableColumnPrecision();

    public void setTableColumnPrecision(int precision);

    public IPersistableTypeConverter getTableColumnConverter();

    public void setTableColumnConverter(IPersistableTypeConverter newConverter);
}
