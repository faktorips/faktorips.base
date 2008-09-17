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

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.enums.EnumType;

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
        VALUE = new ProdDefPropertyType(enumType, "attribute", Messages.ProdDefPropertyType_productAttribute, 10, "ProductAttribute.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        TABLE_CONTENT_USAGE = new ProdDefPropertyType(enumType, "tableContentUsage", Messages.ProdDefPropertyType_tableUsage, 20, "TableContentsUsage.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        FORMULA = new ProdDefPropertyType(enumType, "formula", Messages.ProdDefPropertyType_fomula, 30, "Formula.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        DEFAULT_VALUE_AND_VALUESET = new ProdDefPropertyType(enumType, "config", Messages.ProdDefPropertyType_defaultValueAndValueSet, MAX_SORT_ORDER, "PolicyAttribute.gif"); //$NON-NLS-1$ //$NON-NLS-2$
        
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
