/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    NAME_AND_ID("nameAndId", Messages.EnumTypeDisplay_nameAndId); //$NON-NLS-1$

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
