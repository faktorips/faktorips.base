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

package org.faktorips.devtools.core;

import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;

/**
 * Enumeration to specify the display type of enumeration type controls.
 * <ul>
 * <li>ID - display id only
 * <li>NAME - display name only
 * <li>NAME_AND_ID - display both: name and id
 * </ul>
 * 
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
        ID = new EnumTypeDisplay(enumType, "id", Messages.EnumTypeDisplay_id); //$NON-NLS-1$
        NAME = new EnumTypeDisplay(enumType, "name", Messages.EnumTypeDisplay_name); //$NON-NLS-1$
        NAME_AND_ID = new EnumTypeDisplay(enumType, "nameAndId", Messages.EnumTypeDisplay_nameAndId); //$NON-NLS-1$

        DEFAULT = NAME_AND_ID;
    }

    public EnumTypeDisplay(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

    public static DefaultEnumType getEnumType() {
        return enumType;
    }

}
