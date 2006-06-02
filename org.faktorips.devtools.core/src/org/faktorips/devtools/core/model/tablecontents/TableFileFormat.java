/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablecontents;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

public class TableFileFormat extends DefaultEnumValue {
    
    private String extension;
    
    /**
     * Microsoft Excel file format
     */
    public final static TableFileFormat XLS;    
    
    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("TableFileFormat", TableFileFormat.class); //$NON-NLS-1$
        XLS = new TableFileFormat(enumType, "Excel", ".xls"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    public final static TableFileFormat getAttributeType(String id) {
        return (TableFileFormat)enumType.getEnumValue(id);
    }
    
    private TableFileFormat(DefaultEnumType type, String id, String extension) {
        super(type, id);
        this.extension = extension;
    }

    /**
     * @return Returns the extension.
     */
    public String getExtension() {
        return extension;
    }
}
