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

package org.faktorips.devtools.core.ui.controller.fields;

import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;

/**
 * Enum class, to specify the display type of enum type controls.
 * <ul>
 * <li>ID - display id only
 * <li>NAME - display name only
 * <li>NAME_AND_ID - display both: name and id
 * </ul>
 * @author Joerg Ortmann
 */
public class EnumTypeDisplay extends DefaultEnumValue {

    public final static EnumTypeDisplay ID;

    public final static EnumTypeDisplay NAME;
    
    public final static EnumTypeDisplay NAME_AND_ID;
    
    public final static EnumTypeDisplay DEFAULT;
    
    private final static DefaultEnumType enumType; 

    static {
        enumType = new DefaultEnumType("EnumTypeDisplay", EnumTypeDisplay.class); //$NON-NLS-1$
        ID = new EnumTypeDisplay(enumType, "id", Messages.getString("EnumTypeDisplay.nameId")); //$NON-NLS-1$ //$NON-NLS-2$
        NAME = new EnumTypeDisplay(enumType, "name", Messages.getString("EnumTypeDisplay.nameName")); //$NON-NLS-1$ //$NON-NLS-2$
        NAME_AND_ID = new EnumTypeDisplay(enumType, "nameAndId", Messages.getString("EnumTypeDisplay.nameNameAndId")); //$NON-NLS-1$ //$NON-NLS-2$
        
        DEFAULT = NAME_AND_ID;
    }
    
    public EnumTypeDisplay(DefaultEnumType type, String id, String name){
        super(type, id, name);
    }
    
    public static DefaultEnumType getEnumType(){
        return enumType;
    }
}
