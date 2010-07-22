/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablestructure;

import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.enums.EnumType;

/**
 * This enumeration defines all possible value for the type of the table structure.
 * 
 * @author Thorsten Guenther
 */
public class TableStructureType extends DefaultEnumValue {

    /**
     * Single content - for this table structure only on table content is allowed.
     */
    public static final TableStructureType SINGLE_CONTENT;

    /**
     * Multiple contents - for this table structure one or more table contents are allowed.
     */
    public static final TableStructureType MULTIPLE_CONTENTS;

    /**
     * EnumType, values are model-defined - this table structure represents an EnumType. All values
     * of this EnumType are defined in the model.
     * 
     * @deprecated this constant is not necessary since the introduction of Faktor-IPS enumerations
     *             in version 2.3. It only remains for migration purposes.
     */
    @Deprecated
    public static final TableStructureType ENUMTYPE_MODEL;

    private final static DefaultEnumType enumType;

    static {
        enumType = new DefaultEnumType("TableStructureType", TableStructureType.class); //$NON-NLS-1$
        SINGLE_CONTENT = new TableStructureType(enumType,
                "singleContent", Messages.TableStructureType_labelSingleContent); //$NON-NLS-1$
        MULTIPLE_CONTENTS = new TableStructureType(enumType,
                "multipleContents", Messages.TableStructureType_lableMultipleContents); //$NON-NLS-1$
        ENUMTYPE_MODEL = new TableStructureType(enumType,
                "enumTypeModel", Messages.TableStructureType_labelEnumTypeModel); //$NON-NLS-1$
        // this value is currently disabled since the builder doesn't support this feature yet. pk
        // 2007-03-30
        //        ENUMTYPE_PRODUCTDEFINTION = new TableStructureType(enumType, "enumTypeProductDefinition", Messages.TableStructureType_labelEnumTypeProductDefinition); //$NON-NLS-1$
    }

    private TableStructureType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

    /**
     * @param index The index of the type.
     * 
     * @return The type defined for the given index.
     * 
     * @throws IndexOutOfBoundsException If the index is out of bounds.
     */
    public TableStructureType getType(int index) throws IndexOutOfBoundsException {
        return (TableStructureType)enumType.getEnumValue(index);
    }

    /**
     * @return The numer of types avaliable.
     */
    public int getNumberOfTypes() {
        return enumType.getNumOfValues();
    }

    /**
     * @param id The id defining the type
     * @return The type defined by the given id.
     * @throws IllegalArgumentException If the given id does not represent a valid type.
     */
    public static TableStructureType getTypeForId(String id) throws IllegalArgumentException {
        return (TableStructureType)enumType.getEnumValue(id);
    }

    /**
     * @return All types defined as array.
     */
    public static TableStructureType[] getAll() {
        return (TableStructureType[])enumType.getValues();
    }

    /**
     * @return The datatype these values are based on.
     */
    public final static EnumType getEnumType() {
        return enumType;
    }
}
