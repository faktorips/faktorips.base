/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.List;

import org.faktorips.runtime.internal.Table;
import org.faktorips.runtime.test.IpsTestCaseBase;
import org.faktorips.values.DefaultInternationalString;
import org.faktorips.values.InternationalString;

/**
 * An extension of the {@link IRuntimeRepository} that allows to put files in the repository.
 *
 * @since 24.1
 */
public interface IModifiableRuntimeRepository extends IRuntimeRepository {

    /**
     * Puts the runtimeObject into the repository.
     */
    <T extends IRuntimeObject> void putCustomRuntimeObject(Class<T> type,
            String ipsObjectQualifiedName,
            T runtimeObject);

    /**
     * Puts the given enum values and description in the repository replacing all existing values
     * for the given enumType.
     *
     * @param enumTypeClass the Java class representing the enumeration type
     * @param enumValues the value of the enumeration type as list
     * @param description the description of the enumeration
     */
    <T> void putEnumValues(Class<T> enumTypeClass, List<T> enumValues, InternationalString description);

    /**
     * Puts the given enum values in the repository replacing all existing values for the given
     * enumType, removing any description.
     *
     * @see #putEnumValues(Class, List, InternationalString)
     *
     * @param enumTypeClass The Java class representing the enumeration type.
     * @param enumValues The value of the enumeration type as list.
     */
    default <T> void putEnumValues(Class<T> enumTypeClass, List<T> enumValues) {
        putEnumValues(enumTypeClass, enumValues, DefaultInternationalString.EMPTY);
    }

    /**
     * Puts the test case into the repository.
     */
    void putIpsTestCase(IpsTestCaseBase test);

    /**
     * Puts the product component generation and its product component into the repository. If the
     * repository already contains a generation with the same id, the new component replaces the old
     * one. The same applies for the product component.
     *
     * @throws IllegalRepositoryModificationException if this repository does not allows to modify
     *             its contents.
     * @throws NullPointerException if generation is {@code null}
     *
     * @see IRuntimeRepository#isModifiable()
     */
    void putProductCmptGeneration(IProductComponentGeneration generation);

    /**
     * Puts the product component into the repository. If the repository already contains a
     * component with the same id, the new component replaces the old one.
     *
     * @throws NullPointerException if productCmpt is {@code null}.
     *
     * @see IRuntimeRepository#isModifiable()
     */
    void putProductComponent(IProductComponent productCmpt);

    /**
     * Puts the table into the repository. Replaces any table instance of the same class or any of
     * its superclasses. The latter check is needed to replace tables with mock implementations.
     *
     * @throws NullPointerException if table is {@code null} or has no {@link Table#getName() name}.
     */
    void putTable(ITable<?> table);
}
