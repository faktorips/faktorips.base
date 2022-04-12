/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

/**
 * Enumeration to specify the display type of named data type controls.
 * <ul>
 * <li>ID - display id only
 * <li>NAME - display name only
 * <li>NAME_AND_ID - display both: name and id
 * </ul>
 * 
 * @author Joerg Ortmann
 */
public enum NamedDataTypeDisplay {

    ID("id", Messages.NamedDataTypeDisplay_id), //$NON-NLS-1$

    NAME("name", Messages.NamedDataTypeDisplay_name), //$NON-NLS-1$

    NAME_AND_ID("nameAndId", Messages.NamedDataTypeDisplay_nameAndId); //$NON-NLS-1$

    private final String id;

    private final String text;

    private NamedDataTypeDisplay(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public static NamedDataTypeDisplay getValueById(String id) {
        for (NamedDataTypeDisplay value : values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }
}
