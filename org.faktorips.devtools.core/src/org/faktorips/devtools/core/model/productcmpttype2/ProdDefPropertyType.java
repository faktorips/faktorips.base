/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype2;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * 
 * @author Jan Ortmann
 */
public class ProdDefPropertyType extends DefaultEnumValue {

    public final static ProdDefPropertyType VALUE;
    
    public final static ProdDefPropertyType FORMULA;

    public final static ProdDefPropertyType TABLE_CONTENT_USAGE;
    
    public final static ProdDefPropertyType DEFAULT_VALUE_AND_VALUESET;
    
    public final static int MAX_SORT_ORDER = 40;
    
    public final static DefaultEnumType enumType; 

    public final static ProdDefPropertyType[] ALL_TYPES;
    
    static {
        enumType = new DefaultEnumType("ProdDefPropertyType", ProdDefPropertyType.class); //$NON-NLS-1$
        VALUE = new ProdDefPropertyType(enumType, "attribute", "Product property", 10, "ProductAttribute.gif");
        TABLE_CONTENT_USAGE = new ProdDefPropertyType(enumType, "tableContentUsage", "Table usage", 20, "TableContentsUsage.gif");
        FORMULA = new ProdDefPropertyType(enumType, "formula", "Formula", 30, "Formula.gif");
        DEFAULT_VALUE_AND_VALUESET = new ProdDefPropertyType(enumType, "config", "Default&ValueSet Configuration", MAX_SORT_ORDER, "PolicyAttribute.gif");
        
        ALL_TYPES = new ProdDefPropertyType[]{VALUE, TABLE_CONTENT_USAGE, FORMULA, DEFAULT_VALUE_AND_VALUESET};
    }

    public final static EnumType getEnumType() {
        return enumType;
    }
    
    private int sortOrder;
    private String imageName;
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public Image getImage() {
        return IpsPlugin.getDefault().getImage(imageName);
    }
    
    private ProdDefPropertyType(DefaultEnumType type, String id, String name, int sortOrder, String imageName) {
        super(type, id, name);
        this.sortOrder = sortOrder;
        this.imageName = imageName;
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        ProdDefPropertyType otherType = (ProdDefPropertyType)o;
        return sortOrder - otherType.sortOrder;
    }
    
}
