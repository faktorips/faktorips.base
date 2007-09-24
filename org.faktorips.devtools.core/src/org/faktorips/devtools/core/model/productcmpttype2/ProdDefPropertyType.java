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
    
    public final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("ProdDefPropertyType", ProdDefPropertyType.class); //$NON-NLS-1$
        VALUE = new ProdDefPropertyType(enumType, "attribute");
        FORMULA = new ProdDefPropertyType(enumType, "formula");
        TABLE_CONTENT_USAGE = new ProdDefPropertyType(enumType, "tableContentUsage");
        DEFAULT_VALUE_AND_VALUESET = new ProdDefPropertyType(enumType, "config");
    }

    public final static EnumType getEnumType() {
        return enumType;
    }
    
    private ProdDefPropertyType(DefaultEnumType type, String id) {
        super(type, id);
    }

    
}
