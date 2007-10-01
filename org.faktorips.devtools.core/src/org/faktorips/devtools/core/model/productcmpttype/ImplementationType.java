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

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;

/**
 * 
 * @author Jan Ortmann
 */
public class ImplementationType extends DefaultEnumValue {

    public final static ImplementationType IN_PRODUCT_CMPT_GENERATION; 
    
    public final static ImplementationType IN_TYPE; 

    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("ImplementationType", ImplementationType.class); //$NON-NLS-1$
        IN_PRODUCT_CMPT_GENERATION = new ImplementationType(enumType, "generation", "Implemented in product compontent generation");
        IN_TYPE = new ImplementationType(enumType, "type", "Implemented in the type");
    }
    
    public final static ImplementationType getType(String id) {
        return (ImplementationType)enumType.getEnumValue(id);
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    private ImplementationType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

}
