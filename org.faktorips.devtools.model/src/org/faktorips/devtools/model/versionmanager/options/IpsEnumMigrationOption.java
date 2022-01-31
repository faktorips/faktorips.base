/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.versionmanager.options;

/**
 * This class is a special implementation for enums because the binder combo boxes need the enum
 * class and generics with enums has its pitfalls.
 * 
 *
 * @since 22.6
 */
public class IpsEnumMigrationOption<E extends Enum<E>> extends IpsMigrationOption<E> {

    private final Class<E> enumClass;

    public IpsEnumMigrationOption(String id, String text, E defaultValue, Class<E> enumClass) {
        super(id, text, defaultValue);
        this.enumClass = enumClass;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedEnumValue(Enum<?> enumType) {
        setSelectedValue((E)enumType);
    }

    public Class<E> getEnumClass() {
        return enumClass;
    }
}
