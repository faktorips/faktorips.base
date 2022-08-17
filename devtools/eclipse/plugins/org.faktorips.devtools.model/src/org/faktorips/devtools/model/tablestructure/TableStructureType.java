/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.tablestructure;

/**
 * This enumeration defines all possible value for the type of the table structure.
 * 
 * @author Thorsten Guenther
 */
public enum TableStructureType {

    /**
     * Single content - for this table structure only on table content is allowed.
     */
    SINGLE_CONTENT("singleContent", Messages.TableStructureType_labelSingleContent), //$NON-NLS-1$

    /**
     * Multiple contents - for this table structure one or more table contents are allowed.
     */
    MULTIPLE_CONTENTS("multipleContents", Messages.TableStructureType_lableMultipleContents); //$NON-NLS-1$

    private final String id;
    private final String name;

    TableStructureType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * @param index The index of the type.
     * 
     * @return The type defined for the given index.
     * 
     * @throws IndexOutOfBoundsException If the index is out of bounds.
     */
    public TableStructureType getType(int index) {
        return values()[index];
    }

    /**
     * @return The numer of types avaliable.
     */
    public int getNumberOfTypes() {
        return values().length;
    }

    /**
     * @param id The id defining the type
     * @return The type defined by the given id.
     * @throws IllegalArgumentException If the given id does not represent a valid type.
     */
    public static TableStructureType getTypeForId(String id) {
        for (TableStructureType type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException(id + " is no valid " + TableStructureType.class.getName()); //$NON-NLS-1$
    }

    /**
     * @return All types defined as array.
     */
    public static TableStructureType[] getAll() {
        return values();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
