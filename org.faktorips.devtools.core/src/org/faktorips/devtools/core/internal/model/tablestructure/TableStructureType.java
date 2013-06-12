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

package org.faktorips.devtools.core.internal.model.tablestructure;

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
    MULTIPLE_CONTENTS("multipleContents", Messages.TableStructureType_lableMultipleContents), //$NON-NLS-1$

    /**
     * EnumType, values are model-defined - this table structure represents an EnumType. All values
     * of this EnumType are defined in the model.
     * 
     * @deprecated this constant is not necessary since the introduction of Faktor-IPS enumerations
     *             in version 2.3. It only remains for migration purposes.
     */
    @Deprecated
    ENUMTYPE_MODEL("enumTypeModel", Messages.TableStructureType_labelEnumTypeModel); //$NON-NLS-1$

    private final String id;
    private final String name;

    private TableStructureType(String id, String name) {
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
