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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPersistableTypeConverter;

/**
 * 
 * @author Roman Grutza
 */
public class PersistentAttributeInfo implements IPersistentAttributeInfo {

    private String tableColumnName;
    private int tableColumnSize;
    private boolean tableColumnUnique;
    private boolean tableColumnNullable;
    private int tableColumnScale;
    private int tableColumnPrecision;

    /**
     * {@inheritDoc}
     */
    // FIXME RG: implement
    public IPersistableTypeConverter getTableColumnConverter() {
        throw new NotImplementedException();
    }

    public String getTableColumnName() {
        return tableColumnName;
    }

    public boolean getTableColumnNullable() {
        return tableColumnNullable;
    }

    public int getTableColumnPrecision() {
        return tableColumnPrecision;
    }

    public int getTableColumnScale() {
        return tableColumnScale;
    }

    public int getTableColumnSize() {
        return tableColumnSize;
    }

    public boolean getTableColumnUnique() {
        return tableColumnUnique;
    }

    /**
     * {@inheritDoc}
     */
    public void setTableColumnConverter(IPersistableTypeConverter newConverter) {
        throw new NotImplementedException();
    }

    public void setTableColumnName(String newTableColumnName) {
        if (StringUtils.isEmpty(newTableColumnName)) {
            throw new RuntimeException("Table column name must not be null or empty.");
        }
        tableColumnName = newTableColumnName;
    }

    public void setTableColumnNullable(boolean nullable) {
        tableColumnNullable = nullable;
    }

    public void setTableColumnPrecision(int precision) {
        tableColumnPrecision = precision;
    }

    public void setTableColumnScale(int scale) {
        tableColumnScale = scale;
    }

    public void setTableColumnSize(int newTableColumnSize) {
        tableColumnSize = newTableColumnSize;
    }

    public void setTableColumnUnique(boolean unique) {
        tableColumnUnique = unique;
    }

}
