/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core;

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
public enum EnumTypeDisplay {

    ID("id", Messages.EnumTypeDisplay_id), //$NON-NLS-1$

    NAME("name", Messages.EnumTypeDisplay_name), //$NON-NLS-1$

    NAME_AND_ID("nameAndId", Messages.EnumTypeDisplay_nameAndId), //$NON-NLS-1$

    DEFAULT("nameAndId", Messages.EnumTypeDisplay_nameAndId); //$NON-NLS-1$

    private final String id;

    private final String text;

    private EnumTypeDisplay(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public static EnumTypeDisplay getValueById(String id) {
        for (EnumTypeDisplay value : values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
